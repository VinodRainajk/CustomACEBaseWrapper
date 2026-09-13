package com.qa.framework.declarative;

import com.qa.framework.exceptions.WrapperException;
import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.datatable.DataTable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The atomic steps a recipe can call: every {@code @Given} / {@code @When} / {@code @Then} method in
 * the glue packages the runner already loads.
 * <p>
 * This registry only ever contains Java step definitions. Declarative sentences live in
 * {@link DeclarativeBackend} and are never visible here, so a recipe can never call another
 * declarative sentence and recursion is impossible by construction.
 * </p>
 */
final class AtomicStepRegistry {

    private static final Set<String> STEP_ANNOTATIONS = Set.of(
            "io.cucumber.java.en.Given",
            "io.cucumber.java.en.When",
            "io.cucumber.java.en.Then",
            "io.cucumber.java.en.And",
            "io.cucumber.java.en.But");

    /** One Java step definition, with its expression compiled once. */
    record Entry(String expressionText, Expression expression, Method method) {

        String location() {
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }

        @Override
        public String toString() {
            return "'" + expressionText + "' (" + location() + ")";
        }
    }

    /** A resolved atomic step: which method to call and the arguments to call it with. */
    record Match(Entry entry, Object[] arguments) {
    }

    private final List<Entry> entries;
    private final ThreadLocal<Map<Class<?>, Object>> instances = ThreadLocal.withInitial(LinkedHashMap::new);

    private AtomicStepRegistry(List<Entry> entries) {
        this.entries = entries;
    }

    /** Builds a registry by scanning the given packages for Cucumber annotated methods. */
    static AtomicStepRegistry scan(Collection<String> packages) {
        ParameterTypeRegistry parameterTypes = new ParameterTypeRegistry(Locale.ENGLISH);
        ExpressionFactory expressions = new ExpressionFactory(parameterTypes);
        List<Entry> found = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (String packageName : new LinkedHashSet<>(packages)) {
            for (Class<?> type : ClasspathScanner.classesInPackage(packageName)) {
                if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                    continue;
                }
                for (Method method : type.getMethods()) {
                    for (Annotation annotation : method.getAnnotations()) {
                        String annotationName = annotation.annotationType().getName();
                        if (!STEP_ANNOTATIONS.contains(annotationName)) {
                            continue;
                        }
                        String expressionText = expressionOf(annotation);
                        if (expressionText == null || !seen.add(method.getDeclaringClass().getName()
                                + "#" + method.getName() + "#" + expressionText)) {
                            continue;
                        }
                        found.add(new Entry(expressionText, expressions.createExpression(expressionText), method));
                    }
                }
            }
        }
        found.sort(Comparator.comparing(Entry::expressionText));
        return new AtomicStepRegistry(found);
    }

    /** Every registered atomic step, for reports and validation messages. */
    List<Entry> entries() {
        return List.copyOf(entries);
    }

    /** True when some atomic step matches this text, used by startup collision checks. */
    boolean matchesAny(String text) {
        for (Entry entry : entries) {
            if (entry.expression().match(text, entry.method().getGenericParameterTypes()) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the single atomic step that runs this recipe line.
     *
     * @throws WrapperException when nothing matches or more than one thing matches
     */
    Match resolve(RecipeLine line) {
        List<Entry> matched = new ArrayList<>();
        List<List<Argument<?>>> matchedArguments = new ArrayList<>();

        for (Entry entry : entries) {
            List<Argument<?>> arguments = entry.expression()
                    .match(line.text(), entry.method().getGenericParameterTypes());
            if (arguments != null) {
                matched.add(entry);
                matchedArguments.add(arguments);
            }
        }

        if (matched.isEmpty()) {
            throw new WrapperException("No atomic step matches: " + line.text()
                    + System.lineSeparator() + "  Check the wording, or check the runner glue includes"
                    + " the package that defines it.");
        }
        if (matched.size() > 1) {
            StringBuilder message = new StringBuilder("Atomic step is ambiguous: ").append(line.text());
            for (Entry entry : matched) {
                message.append(System.lineSeparator()).append("  - ").append(entry);
            }
            throw new WrapperException(message.toString());
        }

        Entry entry = matched.get(0);
        List<Argument<?>> arguments = matchedArguments.get(0);
        return new Match(entry, argumentsFor(entry, arguments, line));
    }

    /** Invokes a resolved match, reusing one instance per step definition class per thread. */
    void invoke(Match match) throws Throwable {
        Class<?> type = match.entry().method().getDeclaringClass();
        Object target = instances.get().computeIfAbsent(type, AtomicStepRegistry::instantiate);
        try {
            match.entry().method().invoke(target, match.arguments());
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IllegalAccessException e) {
            throw new WrapperException("Cannot call atomic step " + match.entry().location(), e);
        }
    }

    /** Drops the per thread step definition instances. Called when a scenario ends. */
    void clearInstances() {
        instances.remove();
    }

    private Object[] argumentsFor(Entry entry, List<Argument<?>> matchedArguments, RecipeLine line) {
        Type[] parameterTypes = entry.method().getGenericParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        for (int i = 0; i < matchedArguments.size() && i < arguments.length; i++) {
            arguments[i] = matchedArguments.get(i).getValue();
        }

        int expectedExtra = parameterTypes.length - matchedArguments.size();
        if (expectedExtra == 0) {
            if (line.table() != null || line.docString() != null) {
                throw new WrapperException("Atomic step does not take a data table or doc string: " + line.text());
            }
            return arguments;
        }
        if (expectedExtra > 1) {
            throw new WrapperException("Atomic step " + entry.location() + " takes " + parameterTypes.length
                    + " parameters but its expression captures only " + matchedArguments.size());
        }

        int last = parameterTypes.length - 1;
        arguments[last] = tableOrDocString(parameterTypes[last], line, entry);
        return arguments;
    }

    private static Object tableOrDocString(Type parameterType, RecipeLine line, Entry entry) {
        String typeName = parameterType.getTypeName();
        if (line.docString() != null && String.class.getName().equals(typeName)) {
            return line.docString();
        }
        if (line.table() == null) {
            throw new WrapperException("Atomic step " + entry.location() + " expects a data table"
                    + " but the recipe line has none: " + line.text());
        }
        List<List<String>> rows = line.table();

        if (DataTable.class.getName().equals(typeName)) {
            return DataTable.create(rows);
        }
        if ("java.util.List<java.lang.String>".equals(typeName)) {
            List<String> flattened = new ArrayList<>();
            for (List<String> row : rows) {
                flattened.addAll(row);
            }
            return flattened;
        }
        if ("java.util.List<java.util.List<java.lang.String>>".equals(typeName)) {
            return rows;
        }
        if ("java.util.List<java.util.Map<java.lang.String, java.lang.String>>".equals(typeName)) {
            return asMaps(rows);
        }
        if ("java.util.Map<java.lang.String, java.lang.String>".equals(typeName)) {
            Map<String, String> map = new LinkedHashMap<>();
            for (List<String> row : rows) {
                map.put(row.get(0), row.size() > 1 ? row.get(1) : null);
            }
            return map;
        }
        throw new WrapperException("Recipe lines cannot pass a data table to " + entry.location()
                + ", which expects " + typeName + "."
                + " Supported table parameters are DataTable, List<String>, List<List<String>>,"
                + " List<Map<String,String>> and Map<String,String>.");
    }

    private static List<Map<String, String>> asMaps(List<List<String>> rows) {
        List<Map<String, String>> maps = new ArrayList<>();
        if (rows.size() < 2) {
            return maps;
        }
        List<String> header = rows.get(0);
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            Map<String, String> map = new LinkedHashMap<>();
            for (int column = 0; column < header.size(); column++) {
                map.put(header.get(column), column < row.size() ? row.get(column) : null);
            }
            maps.add(map);
        }
        return maps;
    }

    private static String expressionOf(Annotation annotation) {
        try {
            Object value = annotation.annotationType().getMethod("value").invoke(annotation);
            return value instanceof String text && !text.isBlank() ? text : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static Object instantiate(Class<?> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new WrapperException("Step definition class " + type.getName()
                    + " needs a no argument constructor to be callable from a bundle recipe", e);
        }
    }
}

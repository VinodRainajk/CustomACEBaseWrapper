package com.qa.framework.declarative;

import com.qa.framework.exceptions.WrapperException;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * A declarative sentence registered with Cucumber as a real step definition.
 * <p>
 * There is no Java method behind it. The pattern is the sentence from the {@code # BA:} marker and
 * executing it runs the atomic steps beneath that marker, so the report shows the business sentence
 * with the atomic steps logged underneath it.
 * </p>
 */
final class DeclarativeStepDefinition implements StepDefinition {

    private final BundleSegment segment;
    private final AtomicStepInvoker invoker;
    private final List<ParameterInfo> parameterInfos;

    DeclarativeStepDefinition(BundleSegment segment, AtomicStepInvoker invoker) {
        this.segment = segment;
        this.invoker = invoker;
        List<ParameterInfo> infos = new ArrayList<>();
        for (int i = 0; i < segment.parameterCount(); i++) {
            infos.add(new DeclarativeParameterInfo());
        }
        this.parameterInfos = List.copyOf(infos);
    }

    BundleSegment segment() {
        return segment;
    }

    @Override
    public void execute(Object[] args) {
        Object[] arguments = args == null ? new Object[0] : args;
        if (arguments.length > parameterInfos.size()) {
            throw new WrapperException("Declarative step '" + segment.sentence()
                    + "' does not take a data table or doc string."
                    + " Pass the values inside the sentence, or put the table on an atomic step in "
                    + segment.location() + ".");
        }
        invoker.run(segment, arguments);
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return parameterInfos;
    }

    @Override
    public String getPattern() {
        return segment.sentence();
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getLocation() {
        return segment.location();
    }
}

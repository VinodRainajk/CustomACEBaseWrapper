package com.qa.framework.declarative;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.TypeResolver;

import java.lang.reflect.Type;

/**
 * Parameter description for one argument of a declarative sentence.
 * <p>
 * Declarative steps have no Java method, so there is no declared type to report. Every argument is
 * described as {@link Object}, which Cucumber treats as "no type information": {@code {string}} still
 * yields a String and {@code {int}} still yields an Integer, because the parameter type in the
 * expression decides, not the method signature.
 * </p>
 */
final class DeclarativeParameterInfo implements ParameterInfo {

    @Override
    public Type getType() {
        return Object.class;
    }

    @Override
    public boolean isTransposed() {
        return false;
    }

    @Override
    public TypeResolver getTypeResolver() {
        return () -> Object.class;
    }
}

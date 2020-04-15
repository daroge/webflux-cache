package de.daroge.docdemo.infrastructure.util;

import de.daroge.docdemo.domain.util.IValidator;

import java.util.function.Function;

public class Validator<T> implements IValidator<T> {

    private T entity;

    public Validator(T entity){
        this.entity = entity;
    }

    @Override
    public <U> IValidator<U> map(Function<T, U> mapping) {
        return new Validator<>(mapping.apply(entity));
    }

    @Override
    public T get() {
        return entity;
    }
}

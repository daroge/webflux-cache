package de.daroge.docdemo.domain.util;

import java.util.function.Function;

public interface IValidator<T> {
     <U> IValidator<U> map(Function<T, U> mapping);
     T get();
}

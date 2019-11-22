package com.overstreamapp.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public abstract class ArrayUtils {

    public static <R, E> Iterator<R> multiIterator(Iterator<E> iterators, Function<E, Iterable<R>> function) {
        return new Iterator<R>() {
            Iterator<R> current = iterators.hasNext() ? function.apply(iterators.next()).iterator() : null;

            @Override
            public boolean hasNext() {
                if (current == null) return false;

                if (!current.hasNext()) {
                    if (!iterators.hasNext()) {
                        return false;
                    } else {
                        current = function.apply(iterators.next()).iterator();
                        return current.hasNext();
                    }
                } else {
                    return true;
                }
            }

            @Override
            public R next() {
                if (!hasNext()) throw new NoSuchElementException();

                return current.next();
            }
        };
    }
}

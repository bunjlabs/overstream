/*
 * Copyright 2019 Bunjlabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

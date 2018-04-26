/*
 * Copyright (C) 2018 Sergej Shafarenka, www.halfbit.de
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

package magnet.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import magnet.InstanceFactory;

/* Subject to change. For internal use only. */
public final class Instance<T> {

    private final int scopeDepth;
    private Object value;

    private Instance(int scopeDepth) {
        this.scopeDepth = scopeDepth;
    }

    public static <V> Instance<V> create(V object, InstanceFactory<V> factory, int scopeDepth) {
        Instance<V> instance = new Instance<>(scopeDepth);
        instance.addValue(object, factory);
        return instance;
    }

    @SuppressWarnings("unchecked")
    public void addValue(T object, InstanceFactory<T> factory) {
        if (this.value == null) {
            this.value = new SingleValue<>(object, factory);
            return;
        }

        if (this.value instanceof SingleValue) {
            SingleValue<T> value = (SingleValue<T>) this.value;

            Map<InstanceFactory<T>, T> values = new HashMap<>();
            values.put(value.factory, value.value);
            values.put(factory, object);
            this.value = values;
            return;
        }

        Map<InstanceFactory<T>, T> values = (Map<InstanceFactory<T>, T>) this.value;
        values.put(factory, object);
    }

    @SuppressWarnings("unchecked")
    public T getValue() {
        if (value instanceof SingleValue) {
            return ((SingleValue<T>) value).value;
        }
        throw new IllegalStateException(
                String.format(
                        "Single instance requested, while many instances are stored: %s",
                        value));
    }

    @SuppressWarnings("unchecked")
    public List<T> getValues() {
        if (this.value instanceof SingleValue) {
            SingleValue<T> value = (SingleValue<T>) this.value;
            return Collections.singletonList(value.value);
        }
        Map<InstanceFactory<T>, T> values = (Map<InstanceFactory<T>, T>) this.value;
        return new ArrayList<>(values.values());
    }

    @SuppressWarnings("unchecked")
    public T getValue(InstanceFactory factory) {
        if (value instanceof SingleValue) {
            SingleValue<T> singleValue = (SingleValue<T>) value;
            return Objects.equals(singleValue.factory, factory) ? singleValue.value : null;
        }
        Map<InstanceFactory<T>, T> values = (Map<InstanceFactory<T>, T>) this.value;
        return values.get(factory);
    }

    public int getScopeDepth() {
        return scopeDepth;
    }

    private static final class SingleValue<T> {
        final T value;
        final InstanceFactory<T> factory;

        SingleValue(T object, InstanceFactory<T> factory) {
            this.value = object;
            this.factory = factory;
        }
    }

}

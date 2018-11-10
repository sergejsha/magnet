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

interface RuntimeInstance2<T> {

    String getKey();
    int getScopeDepth();

    interface Value {}

    interface SingleValue<T> extends Value {
        Class<InstanceFactory> getFactoryType();
        T getInstance();
    }

    interface ManyValues<T> extends Value {
        T getInstances();
        T getInstance(Class<InstanceFactory<T>> factoryType);
    }

}



/* Subject to change. For internal use only. */
final class RuntimeInstance<T> {

    private final int scopeDepth;
    private Object value;

    private RuntimeInstance(int scopeDepth) {
        this.scopeDepth = scopeDepth;
    }

    public static <V> RuntimeInstance<V> create(V object, Class<InstanceFactory> factoryClass, int scopeDepth) {
        RuntimeInstance<V> instance = new RuntimeInstance<>(scopeDepth);
        instance.addValue(object, factoryClass);
        return instance;
    }

    @SuppressWarnings("unchecked")
    void addValue(T object, Class<InstanceFactory> factoryClass) {
        if (this.value == null) {
            this.value = new SingleValue<>(object, factoryClass);
            return;
        }

        if (this.value instanceof SingleValue) {
            SingleValue<T> value = (SingleValue<T>) this.value;

            Map<Class<InstanceFactory>, T> values = new HashMap<>();
            values.put(value.factoryClass, value.value);
            values.put(factoryClass, object);
            this.value = values;
            return;
        }

        Map<Class<InstanceFactory>, T> values = (Map<Class<InstanceFactory>, T>) this.value;
        values.put(factoryClass, object);
    }

    void addInstance(RuntimeInstance instance) {
        if (instance.value instanceof SingleValue) {
            @SuppressWarnings("unchecked") SingleValue<T> sv = (SingleValue<T>) instance.value;
            addValue(sv.value, sv.factoryClass);
        } else {
            throw new IllegalStateException(
                String.format("Cannot add instance: %s",
                    instance));
        }
    }

    @SuppressWarnings("unchecked")
    T getValue() {
        if (value instanceof SingleValue) {
            return ((SingleValue<T>) value).value;
        }
        throw new IllegalStateException(
            String.format(
                "Single instance requested, while many instances are stored: %s",
                value));
    }

    @SuppressWarnings("unchecked")
    List<T> getValues() {
        if (this.value instanceof SingleValue) {
            SingleValue<T> value = (SingleValue<T>) this.value;
            return Collections.singletonList(value.value);
        }
        Map<InstanceFactory<T>, T> values = (Map<InstanceFactory<T>, T>) this.value;
        return new ArrayList<>(values.values());
    }

    @SuppressWarnings("unchecked")
    T getValue(Class<InstanceFactory> factoryClass) {
        if (value instanceof SingleValue) {
            SingleValue<T> singleValue = (SingleValue<T>) value;
            return singleValue.factoryClass == factoryClass ? singleValue.value : null;
        }
        Map<Class<InstanceFactory>, T> values = (Map<Class<InstanceFactory>, T>) this.value;
        return values.get(factoryClass);
    }

    int getScopeDepth() {
        return scopeDepth;
    }

    private static final class SingleValue<T> {
        final T value;
        final Class<InstanceFactory> factoryClass;

        SingleValue(T object, Class<InstanceFactory> factory) {
            this.value = object;
            this.factoryClass = factory;
        }
    }

}

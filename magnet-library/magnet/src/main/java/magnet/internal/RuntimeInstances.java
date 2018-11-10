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

/* Subject to change. For internal use only. */
@SuppressWarnings("unchecked")
final class RuntimeInstances<T> {

    private final int scopeDepth;
    private Instance instance;

    RuntimeInstances(int scopeDepth, Class<InstanceFactory<T>> factoryType, T instance) {
        this.scopeDepth = scopeDepth;
        this.instance = new SingleValueInstance<>(factoryType, instance);
    }

    int getScopeDepth() { return scopeDepth; }

    T getSingleInstance() {
        if (instance instanceof SingleValueInstance) {
            SingleValueInstance singleValueInstance = (SingleValueInstance) instance;
            return (T) singleValueInstance.instance;
        } else {
            ManyValueInstance manyValueInstance = (ManyValueInstance) instance;
            throw new IllegalStateException(
                String.format(
                    "Single instance requested, while many instances are stored: %s",
                    manyValueInstance.instances
                )
            );
        }
    }

    T getOptionalInstance(Class<InstanceFactory<T>> factoryType) {
        if (instance instanceof SingleValueInstance) {
            SingleValueInstance singleValueInstance = (SingleValueInstance) instance;
            if (singleValueInstance.factoryType == factoryType) {
                return (T) singleValueInstance.instance;
            }
        } else {
            ManyValueInstance manyValueInstance = (ManyValueInstance) instance;
            return (T) manyValueInstance.getInstance(factoryType);
        }
        return null;
    }

    List<T> getInstances() {
        if (instance instanceof SingleValueInstance) {
            SingleValueInstance single = (SingleValueInstance) instance;
            return (List<T>) Collections.singletonList(single.instance);
        } else {
            ManyValueInstance manyValueInstance = (ManyValueInstance) instance;
            return (List<T>) manyValueInstance.getInstances();
        }
    }

    RuntimeInstances<T> registerInstance(Class<InstanceFactory<T>> factoryType, T instance) {
        if (this.instance == null) {
            this.instance = new SingleValueInstance<>(factoryType, instance);

        } else if (this.instance instanceof SingleValueInstance) {
            SingleValueInstance<T> single = (SingleValueInstance<T>) this.instance;
            ManyValueInstance<T> many = new ManyValueInstance<>(single.factoryType, single.instance);
            many.putInstance(factoryType, instance);
            this.instance = many;

        } else {
            ManyValueInstance<T> many = (ManyValueInstance<T>) this.instance;
            many.putInstance(factoryType, instance);
        }

        return this;
    }

    private interface Instance {}

    private static class SingleValueInstance<T> implements Instance {
        /* Nullable */ private final Class<InstanceFactory<T>> factoryType;
        private final T instance;

        SingleValueInstance(Class<InstanceFactory<T>> factoryType, T instance) {
            this.factoryType = factoryType;
            this.instance = instance;
        }
    }

    private static class ManyValueInstance<T> implements Instance {
        private HashMap<Class<InstanceFactory<T>>, T> instances = new HashMap<>(8);

        ManyValueInstance(Class<InstanceFactory<T>> factoryType, T instance) {
            instances.put(factoryType, instance);
        }

        List<T> getInstances() { return new ArrayList<>(this.instances.values()); }
        T getInstance(Class<InstanceFactory<T>> factoryType) { return instances.get(factoryType); }

        void putInstance(Class<InstanceFactory<T>> factoryType, T instance) {
            instances.put(factoryType, instance);
        }
    }

}
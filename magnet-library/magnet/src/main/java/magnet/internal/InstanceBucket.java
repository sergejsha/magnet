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
final class InstanceBucket<T> {

    private final int scopeDepth;
    private final OnInstanceListener listener;
    /* NonNull */ private Instance instance;

    InstanceBucket(
        int scopeDepth,
        InstanceFactory<T> factory,
        Class<T> instanceType,
        T instance,
        String classifier,
        OnInstanceListener listener
    ) {
        this.scopeDepth = scopeDepth;
        this.listener = listener;
        this.instance = createSingleValueInstance(factory, instanceType, instance, classifier);
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
            if (singleValueInstance.factory.getClass() == factoryType) {
                return (T) singleValueInstance.instance;
            }
        } else {
            ManyValueInstance manyValueInstance = (ManyValueInstance) instance;
            return (T) manyValueInstance.getOptionalInstance(factoryType);
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

    void registerInstance(
        InstanceFactory<T> factory,
        Class<T> instanceType,
        T instance,
        String classifier
    ) {
        if (this.instance instanceof SingleValueInstance) {
            SingleValueInstance<T> single = (SingleValueInstance<T>) this.instance;
            ManyValueInstance<T> many = new ManyValueInstance<>(single);
            many.putInstance(createSingleValueInstance(factory, instanceType, instance, classifier));
            this.instance = many;

        } else {
            ManyValueInstance<T> many = (ManyValueInstance<T>) this.instance;
            many.putInstance(createSingleValueInstance(factory, instanceType, instance, classifier));
        }
    }

    private SingleValueInstance<T> createSingleValueInstance(
        InstanceFactory<T> factory, Class<T> instanceType, T instance, String classifier
    ) {
        SingleValueInstance<T> single = new SingleValueInstance<>(factory, instanceType, instance, classifier);
        if (factory != null && factory.isDisposable()) {
            listener.onInstanceRegistered(single);
        }
        return single;
    }

    interface Instance {}

    static class SingleValueInstance<T> implements Instance {
        /* Nullable */ final InstanceFactory<T> factory;
        final Class<T> instanceType;
        final T instance;
        final String classifier;

        SingleValueInstance(
            InstanceFactory<T> factory,
            Class<T> instanceType,
            T instance,
            String classifier
        ) {
            this.factory = factory;
            this.instanceType = instanceType;
            this.instance = instance;
            this.classifier = classifier;
        }
    }

    private static class ManyValueInstance<T> implements Instance {
        private final HashMap<Class<InstanceFactory<T>>, SingleValueInstance<T>> instances = new HashMap<>(8);

        ManyValueInstance(SingleValueInstance<T> single) {
            instances.put(
                (Class<InstanceFactory<T>>) single.factory.getClass(),
                single
            );
        }

        List<T> getInstances() {
            List<T> result = new ArrayList<>(this.instances.size());
            for (SingleValueInstance<T> single : this.instances.values()) {
                result.add(single.instance);
            }
            return result;
        }

        T getOptionalInstance(Class<InstanceFactory<T>> factoryType) {
            SingleValueInstance<T> single = instances.get(factoryType);
            if (single == null) return null;
            return single.instance;
        }

        void putInstance(SingleValueInstance<T> single) {
            instances.put(
                (Class<InstanceFactory<T>>) single.factory.getClass(),
                single
            );
        }
    }

    interface OnInstanceListener {
        <T> void onInstanceRegistered(SingleValueInstance<T> instance);
    }

    /*
    void accept(InstanceVisitor visitor) {
        if (instance instanceof SingleValueInstance) {
            SingleValueInstance<T> single = (SingleValueInstance<T>) this.instance;
            visitor.visit(
                single.factory,
                single.instanceType,
                single.instance
            );
        } else {
            ManyValueInstance<T> many = (ManyValueInstance<T>) this.instance;
            Collection<SingleValueInstance<T>> singles = many.instances.values();
            for (SingleValueInstance<T> single : singles) {
                visitor.visit(
                    single.factory,
                    single.instanceType,
                    single.instance
                );
            }
        }
    }

    interface InstanceVisitor {
        <T> void visit(
            InstanceFactory<T> factory,
            Class<T> instanceType,
            T instance
        );
    }
    */

}
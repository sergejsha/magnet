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

import magnet.Scope;
import magnet.Scoping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/* Subject to change. For internal use only. */
@SuppressWarnings("unchecked") final class InstanceBucket<T> {

    @NotNull private final OnInstanceListener listener;
    @NotNull private InstanceBucket.Instance instance;
    private final int scopeDepth;

    InstanceBucket(
        int scopeDepth,
        @Nullable InstanceFactory<T> factory,
        @NotNull Class<T> objectType,
        @NotNull T object,
        @NotNull String classifier,
        @NotNull OnInstanceListener listener
    ) {
        this.scopeDepth = scopeDepth;
        this.listener = listener;
        this.instance = createSingleInstance(factory, objectType, object, classifier);
    }

    int getScopeDepth() { return scopeDepth; }

    @NotNull
    T getSingleInstance() {
        if (instance instanceof InjectedInstance) {
            return ((InjectedInstance<T>) instance).object;
        } else if (instance instanceof BoundInstance) {
            return ((BoundInstance<T>) instance).object;
        }

        MultiObjectInstance multiObjectInstance = (MultiObjectInstance) instance;
        throw new IllegalStateException(
            String.format(
                "Single instance requested, while many instances are stored: %s",
                multiObjectInstance.instances
            )
        );
    }

    @Nullable
    T getOptional(@Nullable Class<InstanceFactory<T>> factoryType) {
        if (instance instanceof InjectedInstance) {
            InjectedInstance<T> single = (InjectedInstance<T>) instance;
            if (single.factory.getClass() == factoryType) {
                return single.object;
            }
            return null;
        } else if (instance instanceof BoundInstance) {
            BoundInstance<T> single = (BoundInstance<T>) instance;
            if (factoryType == null) {
                return single.object;
            }
            return null;
        }
        return (T) ((MultiObjectInstance) instance).getOptional(factoryType);
    }

    @NotNull
    List<T> getMany() {
        if (instance instanceof InjectedInstance) {
            return Collections.singletonList(((InjectedInstance<T>) instance).object);
        } else if (instance instanceof BoundInstance) {
            return Collections.singletonList(((BoundInstance<T>) instance).object);
        }
        return ((MultiObjectInstance<T>) instance).getMany();
    }

    void registerObject(
        @Nullable InstanceFactory<T> factory,
        @NotNull Class<T> objectType,
        @NotNull T object,
        @NotNull String classifier
    ) {
        if (this.instance instanceof InstanceBucket.MultiObjectInstance) {
            MultiObjectInstance<T> many = (MultiObjectInstance<T>) this.instance;
            many.putSingle(createSingleInstance(factory, objectType, object, classifier));

        } else {
            MultiObjectInstance<T> many = new MultiObjectInstance<>((SingleObjectInstance<T>) this.instance);
            many.putSingle(createSingleInstance(factory, objectType, object, classifier));
            this.instance = many;
        }
    }

    boolean hasInstanceWithFactory(InstanceFactory<T> factory) {
        return instance.hasObjectWithFactory(factory);
    }

    @NotNull private InstanceBucket.SingleObjectInstance<T> createSingleInstance(
        @Nullable InstanceFactory<T> factory,
        @NotNull Class<T> objectType,
        @NotNull T object,
        @NotNull String classifier
    ) {
        SingleObjectInstance single;
        if (factory == null) {
            single = new BoundInstance<>(objectType, object, classifier);
        } else {
            single = new InjectedInstance<>(factory, objectType, object, classifier);
        }
        listener.onInstanceCreated(single);
        return single;
    }

    public boolean accept(Scope.Visitor visitor) {
        if (instance instanceof SingleObjectInstance) {
            return ((SingleObjectInstance) instance).accept(visitor);
        } else {
            return ((MultiObjectInstance) instance).accept(visitor);
        }
    }

    interface Instance<T> {
        boolean hasObjectWithFactory(InstanceFactory<T> factory);
    }

    static abstract class SingleObjectInstance<T> implements Instance<T> {
        @NotNull final Class<T> objectType;
        @NotNull final T object;
        @NotNull final String classifier;

        SingleObjectInstance(
            @NotNull Class<T> objectType,
            @NotNull T object,
            @NotNull String classifier
        ) {
            this.objectType = objectType;
            this.object = object;
            this.classifier = classifier;
        }

        public boolean accept(Scope.Visitor visitor) {
            if (this instanceof InjectedInstance) {
                return visitor.onInstance((InjectedInstance) this);
            } else {
                return visitor.onInstance((BoundInstance) this);
            }
        }
    }

    static class BoundInstance<T> extends SingleObjectInstance<T> implements Scope.Visitor.Instance {
        BoundInstance(
            @NotNull Class<T> objectType,
            @NotNull T object,
            @NotNull String classifier
        ) {
            super(objectType, object, classifier);
        }

        @Override public boolean hasObjectWithFactory(InstanceFactory<T> factory) {
            return factory == null;
        }

        @Override public @NotNull Scoping getScoping() {
            return Scoping.DIRECT;
        }

        @Override public @NotNull String getClassifier() {
            return classifier;
        }

        @Override public @NotNull Class<?> getType() {
            return objectType;
        }

        @Override public @NotNull Object getValue() {
            return object;
        }

        @Override public @NotNull Provision getProvision() {
            return Provision.BOUND;
        }
    }

    static class InjectedInstance<T> extends SingleObjectInstance<T> implements Scope.Visitor.Instance {
        @NotNull final InstanceFactory<T> factory;

        InjectedInstance(
            @NotNull InstanceFactory<T> factory,
            @NotNull Class<T> objectType,
            @NotNull T object,
            @NotNull String classifier
        ) {
            super(objectType, object, classifier);
            this.factory = factory;
        }

        @Override public boolean hasObjectWithFactory(InstanceFactory<T> factory) {
            return factory == this.factory;
        }

        @Override public @NotNull Scoping getScoping() {
            return factory.getScoping();
        }

        @Override public @NotNull String getClassifier() {
            return classifier;
        }

        @Override public @NotNull Class<?> getType() {
            return objectType;
        }

        @Override public @NotNull Object getValue() {
            return object;
        }

        @Override public @NotNull Provision getProvision() {
            return Provision.INJECTED;
        }
    }

    private static class MultiObjectInstance<T> implements Instance<T> {
        @NotNull private final HashMap<Class<InstanceFactory<T>>, SingleObjectInstance<T>> instances;

        MultiObjectInstance(@NotNull InstanceBucket.SingleObjectInstance<T> single) {
            instances = new HashMap<>(8);
            putSingle(single);
        }

        @NotNull List<T> getMany() {
            List<T> result = new ArrayList<>(this.instances.size());
            for (SingleObjectInstance<T> single : this.instances.values()) {
                result.add(single.object);
            }
            return result;
        }

        @Nullable T getOptional(@Nullable Class<InstanceFactory<T>> factoryType) {
            SingleObjectInstance<T> single = instances.get(factoryType);
            if (single == null) return null;
            return single.object;
        }

        void putSingle(@NotNull InstanceBucket.SingleObjectInstance<T> single) {
            @Nullable final Class<InstanceFactory<T>> factoryType;
            if (single instanceof InjectedInstance) {
                factoryType = (Class<InstanceFactory<T>>) ((InjectedInstance) single).factory.getClass();
            } else if (single instanceof BoundInstance) {
                factoryType = null;
            } else {
                throw new IllegalStateException("Unsupported SingleObjectInstance type.");
            }
            instances.put(factoryType, single);
        }

        @Override public boolean hasObjectWithFactory(InstanceFactory<T> factory) {
            //noinspection SuspiciousMethodCalls
            return instances.containsKey(factory.getClass());
        }

        public boolean accept(Scope.Visitor visitor) {
            Collection<SingleObjectInstance<T>> singleObjectInstances = instances.values();
            boolean takeNext = true;
            for (SingleObjectInstance<T> instance : singleObjectInstances) {
                takeNext = instance.accept(visitor);
                if (!takeNext) break;
            }
            return takeNext;
        }
    }

    interface OnInstanceListener {
        <T> void onInstanceCreated(SingleObjectInstance<T> instance);
    }

}
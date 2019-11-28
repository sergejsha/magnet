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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import magnet.Scoping;
import magnet.Visitor;

/* Subject to change. For internal use only. */
@SuppressWarnings("unchecked")
final class InstanceBucket<T> {

    @NotNull private final OnInstanceListener listener;
    @NotNull private InstanceBucket.Instance instance;
    @NotNull private MagnetScope scope;

    InstanceBucket(
        @NotNull MagnetScope scope,
        @Nullable InstanceFactory<T> factory,
        @NotNull Class<T> objectType,
        @NotNull T object,
        @NotNull String classifier,
        @NotNull OnInstanceListener listener
    ) {
        this.scope = scope;
        this.listener = listener;
        this.instance = createSingleInstance(factory, objectType, object, classifier);
    }

    @NotNull MagnetScope getScope() { return scope; }

    @NotNull T getSingleInstance() {
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

    @Nullable T getOptional(@Nullable Class<InstanceFactory<T>> factoryType) {
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

    @NotNull List<T> getMany() {
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

    boolean hasInstanceWithFactory(@Nullable InstanceFactory<T> factory) {
        return instance.hasObjectWithFactory(factory);
    }

    private @NotNull InstanceBucket.SingleObjectInstance<T> createSingleInstance(
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

    public boolean accept(Visitor visitor) {
        if (instance instanceof SingleObjectInstance) {
            return ((SingleObjectInstance) instance).accept(visitor);
        } else {
            return ((MultiObjectInstance) instance).accept(visitor);
        }
    }

    interface Instance<T> {
        boolean hasObjectWithFactory(@Nullable InstanceFactory<T> factory);
    }

    static abstract class SingleObjectInstance<T> implements Instance<T> {
        final @NotNull Class<T> objectType;
        final @NotNull T object;
        final @NotNull String classifier;

        SingleObjectInstance(
            @NotNull Class<T> objectType,
            @NotNull T object,
            @NotNull String classifier
        ) {
            this.objectType = objectType;
            this.object = object;
            this.classifier = classifier;
        }

        public boolean accept(Visitor visitor) {
            if (this instanceof InjectedInstance) {
                return visitor.onInstance((InjectedInstance) this);
            } else {
                return visitor.onInstance((BoundInstance) this);
            }
        }
    }

    static class BoundInstance<T> extends SingleObjectInstance<T> implements Visitor.Instance {
        BoundInstance(@NotNull Class<T> objectType, @NotNull T object, @NotNull String classifier) {
            super(objectType, object, classifier);
        }

        @Override public boolean hasObjectWithFactory(@Nullable InstanceFactory<T> factory) {
            return true;
        }
        @Override public @NotNull Scoping getScoping() { return Scoping.DIRECT; }
        @Override public @NotNull String getClassifier() { return classifier; }
        @Override public @NotNull String getLimit() { return ""; }
        @Override public @NotNull Class<?> getType() { return objectType; }
        @Override public @NotNull Object getValue() { return object; }
        @Override public @NotNull Visitor.Provision getProvision() {
            return Visitor.Provision.BOUND;
        }
    }

    static class InjectedInstance<T> extends SingleObjectInstance<T> implements Visitor.Instance {
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

        @Override public boolean hasObjectWithFactory(@Nullable InstanceFactory<T> factory) {
            return factory == this.factory;
        }
        @Override public @NotNull Scoping getScoping() { return factory.getScoping(); }
        @Override public @NotNull String getClassifier() { return classifier; }
        @Override public @NotNull String getLimit() { return factory.getLimit(); }
        @Override public @NotNull Class<?> getType() { return objectType; }
        @Override public @NotNull Object getValue() { return object; }
        @Override public @NotNull Visitor.Provision getProvision() {
            return Visitor.Provision.INJECTED;
        }
    }

    private static class MultiObjectInstance<T> implements Instance<T> {
        private final @NotNull HashMap<Class<InstanceFactory<T>>, SingleObjectInstance<T>> instances;

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

        @Override public boolean hasObjectWithFactory(@Nullable InstanceFactory<T> factory) {
            return instances.containsKey(factory == null ? null : factory.getClass());
        }

        public boolean accept(Visitor visitor) {
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
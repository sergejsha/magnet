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

package magnet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import magnet.internal.Range;

final class MagnetInstanceManager implements InstanceManager {

    private InstanceFactory[] factories;
    private Map<Class, Object> index;

    MagnetInstanceManager() {
        registerImplementations();
    }

    private void registerImplementations() {
        try {
            Class<?> magnetClass = Class.forName("magnet.MagnetIndexer");
            Method registerFactories = magnetClass.getMethod("register", MagnetInstanceManager.class);
            registerFactories.invoke(magnetClass, this);
        } catch (Exception e) {
            System.err.println(
                    String.format(
                            "MagnetIndexer cannot be found. Add a @%s-annotated class to the application module.",
                            Magnetizer.class
                    )
            );
        }
    }

    // called by generated index class
    void register(InstanceFactory[] factories, Map<Class, Object> index) {
        this.factories = factories;
        this.index = index;
    }

    @Override
    public <T> List<T> getMany(Class<T> type, Scope scope) {
        return getMany(type, Classifier.NONE, scope);
    }

    @Override
    public <T> List<T> getMany(Class<T> type, String classifier, Scope scope) {
        Object indexed = index.get(type);

        if (indexed instanceof Range) {
            Range range = (Range) indexed;
            if (range.getClassifier().equals(classifier)) {
                return createFromRange(range, scope);
            }
            return Collections.emptyList();
        }

        if (indexed instanceof Map) {
            //noinspection unchecked
            Map<String, Range> ranges = (Map<String, Range>) indexed;
            Range range = ranges.get(classifier);
            if (range != null) {
                return createFromRange(range, scope);
            }
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    @Override
    public <T> T getSingle(Class<T> type, Scope scope) {
        return getSingle(type, Classifier.NONE, scope);
    }

    @Override
    public <T> T getSingle(Class<T> type, String classifier, Scope scope) {
        List<T> instances = getMany(type, classifier, scope);
        if (instances.size() > 1) {
            throw new IllegalStateException(
                    String.format("Expect zero or one instance type: %s, classifier: %s, but found %s: %s",
                            type, classifier, instances.size(), instances));
        }
        return instances.size() == 0 ? null : instances.get(0);
    }

    @Override
    public <T> T requireSingle(Class<T> type, Scope scope) {
        return requireSingle(type, Classifier.NONE, scope);
    }

    @Override
    public <T> T requireSingle(Class<T> type, String classifier, Scope scope) {
        List<T> instances = getMany(type, classifier, scope);
        if (instances.size() != 1) {
            throw new IllegalStateException(
                    String.format("Expect exactly one instance type: %s, classifier: %s, but found: %s: %s",
                            type, classifier, instances.size(), instances));
        }
        return instances.get(0);
    }

    @SuppressWarnings("unchecked") @Override
    public <T> InstanceFactory<T> getOptionalFactory(Class<T> type, String classifier) {
        Range range = getOptionalRange(type, classifier);
        if (range == null) {
            return null;
        }
        if (range.getCount() > 1) {
            throw new IllegalStateException("Kaboom");
        }
        return factories[range.getFrom()];
    }

    @SuppressWarnings("unchecked")
    private Range getOptionalRange(Class<?> type, String classifier) {
        Object indexed = index.get(type);

        if (indexed instanceof Range) {
            if (classifier.equals(Classifier.NONE)) {
                return (Range) indexed;
            }
            return null;
        }

        if (indexed instanceof Map) {
            Map<String, Range> ranges = (Map<String, Range>) indexed;
            return ranges.get(classifier);
        }

        throw new IllegalStateException(
                String.format(
                        "Unsupported index type: %s", indexed.getClass()));
    }

    private <T> List<T> createFromRange(Range range, Scope scope) {
        List<T> impls = new ArrayList<>();
        for (int i = range.getFrom(), to = range.getFrom() + range.getCount(); i < to; i++) {
            //noinspection unchecked
            impls.add((T) factories[i].create(scope));
        }
        return impls;
    }

}

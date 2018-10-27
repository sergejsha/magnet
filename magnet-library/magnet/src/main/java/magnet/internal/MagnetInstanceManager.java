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

import magnet.Magnetizer;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/* Subject to change. For internal use only. */
@SuppressWarnings("unchecked")
final class MagnetInstanceManager implements InstanceManager {

    private InstanceFactory[] factories;
    private Map<Class, Object> index;

    MagnetInstanceManager() {
        registerInstanceFactories();
    }

    private void registerInstanceFactories() {
        try {
            Class<?> magnetClass = Class.forName("magnet.internal.MagnetIndexer");
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
    public <T> InstanceFactory<T> getOptionalFactory(Class<T> type, String classifier) {
        Range range = getOptionalRange(type, classifier);
        if (range == null) {
            return null;
        }
        if (range.getCount() > 1) {
            throw new IllegalStateException(
                String.format(
                    "Multiple factories for type '%s' and classifier '%s' found," +
                        " while only one was required", type, classifier));
        }
        return factories[range.getFrom()];
    }

    @Override
    public <T> List<InstanceFactory<T>> getManyFactories(Class<T> type, String classifier) {
        Object indexed = index.get(type);

        if (indexed instanceof Range) {
            Range range = (Range) indexed;
            if (range.getClassifier().equals(classifier)) {
                return factoriesFromRange(range);
            }
            return Collections.emptyList();
        }

        if (indexed instanceof Map) {
            Map<String, Range> ranges = (Map<String, Range>) indexed;
            Range range = ranges.get(classifier);
            if (range != null) {
                return factoriesFromRange(range);
            }
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    private Range getOptionalRange(Class<?> type, String classifier) {
        Object indexed = index.get(type);

        if (indexed == null) {
            return null;
        }

        if (indexed instanceof Range) {
            Range range = (Range) indexed;
            if (classifier.equals(range.getClassifier())) {
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

    private <T> List<InstanceFactory<T>> factoriesFromRange(Range range) {
        InstanceFactory<T>[] factories = new InstanceFactory[range.getCount()];
        System.arraycopy(this.factories, range.getFrom(), factories, 0, range.getCount());
        return new ImmutableArrayList<>(factories);
    }

}

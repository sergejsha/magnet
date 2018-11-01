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

/* Subject to change. For internal use only. */
public abstract class ScopeFactory<T> {
    public abstract T create(Class<T> scopeType);
}

/*
@Scope
interface GlobalScope {}

@Scope
interface AppScope {
    void bind(GlobalScope parent);
    void bind(Object obj);
    void bind(@Classifier("timer") Long obj);
    String getString();
    List<String> getStrings();
}

// generated
class MagnetAppScope extends InstanceScope implements AppScope {

    MagnetAppScope() {
        super(true);
    }

    @Override public void bind(GlobalScope parent) {
        bindParentScope((InstanceScope) parent);
    }

    @Override public void bind(Object object) {
        bindInstance(Object.class, object, Classifier.NONE);
    }

    @Override public void bind(Long obj) {
        bindInstance(Long.class, obj, "timer");
    }

    @Override public String getString() {
        return getSingle(String.class, Classifier.NONE);
    }

    @Override public List<String> getStrings() {
        return getMany(String.class, Classifier.NONE);
    }

}

// generated
class AppScopeMagnetFactory extends ScopeFactory<AppScope> {

    @Override public AppScope create(Class<AppScope> scopeType) {
        return new MagnetAppScope();
    }

    public static Class getType() {
        return AppScope.class;
    }

}

class Tester {

    void test() {
        AppScope appScope = Magnet.createScope(AppScope.class);
    }

}
*/
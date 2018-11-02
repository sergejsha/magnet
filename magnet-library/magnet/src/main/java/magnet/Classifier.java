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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Magnet can bind multiple instances of the same type into a scope. Use classifier
 * with a unique value to differentiate between those instances.
 *
 * <pre>
 *      // bind instances
 *      scope.bind(Context.class, application, "app-context");
 *      scope.bind(Context.class, activity, "activity-context");
 *
 *      // get instances directly
 *      Context app = scope.getSingle(Context.class, "app-context");
 *      Context activity = scope.getSingle(Context.class, "activity-context");
 *
 *      // get instances via constructor injection
 *      &#64;Instance(type = MyImplementation.class)
 *      public MyImplementation(
 *          &#64;Classifier("app-context") Context app,
 *          &#64;Classifier("activity-context") Context activity,
 *      ) {
 *          ...
 *      }
 * </pre>
 */
@Retention(CLASS)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface Classifier {

    String NONE = "";
    String value();

}
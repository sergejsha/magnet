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

package magnetx

import magnet.Instance

/**
 * Interface to be implemented by extensions of `android.app.Application` class.
 * Application implementation class should query the extensions using `getMany()`
 * operator with `AppExtension` type from the application scope.
 *
 * ### Scope dependencies
 * `application: Application`
 *
 * ### Scoping
 * any scoping
 *
 * ### Usage example
 * ```kotlin
 * @Instance(
 *   type = AppExtension::class,
 *   scoping = Scoping.UNSCOPED
 * )
 * internal class LeakCanaryAppExtension(
 *   private val app: Application
 * ) : AppExtension {
 *   override fun onCreate() {
 *     if (LeakCanary.isInAnalyzerProcess(context)) {
 *       return
 *     }
 *     LeakCanary.install(context as Application)
 *   }
 * }
 * ```
 */
interface AppExtension {

    /**
     * Called on each registered extension when `Application.onCreate()` is called.
     *
     * For more information see
     * [https://developer.android.com/reference/android/app/Application.html#onCreate()]
     */
    fun onCreate()

    /**
     * Called on each registered extension when `Application.onTrimMemory()` is called.
     *
     * For more information see
     * [https://developer.android.com/reference/android/app/Application.html#onTrimMemory(int)]
     */
    fun onTrimMemory(level: Int) {}

    /**
     * Delegate to be used in Application for dispatching application events to
     * all registered application extensions.
     */
    @Instance(type = AppExtension.Delegate::class)
    class Delegate(private val appExtensions: List<AppExtension>) {
        fun onCreate() {
            for (appExtension in appExtensions) {
                appExtension.onCreate()
            }
        }

        fun onTrimMemory(level: Int) {
            for (appExtension in appExtensions) {
                appExtension.onTrimMemory(level)
            }
        }
    }

}

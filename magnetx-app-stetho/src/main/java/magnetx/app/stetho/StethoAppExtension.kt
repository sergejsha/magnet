/*
 * Copyright (C) 2019 Sergej Shafarenka, www.halfbit.de
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

package magnetx.app.stetho

import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho
import magnet.Instance
import magnet.Scoping
import magnetx.AppExtension

@Instance(
    type = AppExtension::class,
    scoping = Scoping.UNSCOPED
)
class StethoAppExtension(
    private val application: Application,
    private val stethoInitializers: List<Initializer>
) : AppExtension {

    override fun onCreate() {
        Stetho.initialize(
            Stetho
                .newInitializerBuilder(application)
                .also {
                    for (stethoInitializer in stethoInitializers) {
                        stethoInitializer.initialize(it, application)
                    }
                }
                .build()
        )
    }

    /**
     * Implement this interface and annotate in with unscoped {@link Instance}
     * to participate on initializing Stetho on application launch.
     */
    interface Initializer {
        fun initialize(builder: Stetho.InitializerBuilder, context: Context)
    }
}

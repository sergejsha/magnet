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

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import magnet.Instance
import magnet.Scoping

@Instance(
    type = AppExtension::class,
    scoping = Scoping.UNSCOPED
)
class LeakCanaryAppExtension(
    private val application: Application
) : AppExtension {

    override fun onCreate() {
        if (!LeakCanary.isInAnalyzerProcess(application)) {
            LeakCanary.install(application)
        }
    }

}

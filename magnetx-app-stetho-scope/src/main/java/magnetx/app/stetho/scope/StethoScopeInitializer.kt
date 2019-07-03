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

package magnetx.app.stetho.scope

import android.content.Context
import com.facebook.stetho.Stetho
import magnet.Instance
import magnet.Scope
import magnet.Scoping
import magnetx.app.stetho.StethoAppExtension

@Instance(
    type = StethoAppExtension.Initializer::class,
    scoping = Scoping.UNSCOPED
)
internal class StethoScopeInitializer(
    private val scope: Scope
) : StethoAppExtension.Initializer {
    override fun initialize(builder: Stetho.InitializerBuilder, context: Context) {
        builder.enableDumpapp {
            Stetho.DefaultDumperPluginsBuilder(context)
                .provide(StethoScopeDumpPlugin(scope))
                .finish()
        }
    }
}

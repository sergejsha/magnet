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

package magnet.sample.app

import android.app.Application
import android.content.Context
import magnet.Magnet
import magnet.Scope
import magnet.bind

const val APPLICATION_CONTEXT = "application-context"

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        scope.bind<Context>(this, APPLICATION_CONTEXT)
    }

    companion object {
        val scope: Scope = Magnet.createScope()
    }

}
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

package magnet.sample.app.main.home

import android.content.res.Resources
import magnet.Implementation
import magnet.sample.app.main.Page

@Implementation(type = Page::class)
class HomePage(
    // mandatory dependency available through Scope (see MainActivity.kt)
    private val resources: Resources
) : Page {

    override fun id(): Int {
        return R.id.home_page
    }

    override fun order(): Int {
        return 1
    }

    override fun menuIconId(): Int {
        return R.drawable.ic_home_black_24dp
    }

    override fun menuTitleId(): Int {
        return R.string.title_home
    }

    override fun message(): String {
        return resources.getString(R.string.title_home)
    }

}
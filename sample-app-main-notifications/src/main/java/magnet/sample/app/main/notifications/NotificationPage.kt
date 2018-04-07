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

package magnet.sample.app.main.notifications

import android.content.res.Resources
import magnet.Implementation
import magnet.sample.app.main.Page

@Implementation(type = Page::class)
class NotificationPage(
    // optional (nullable) dependency available through DependencyScope (see MainActivity.kt)
    private val resource: Resources?
) : Page {

    override fun id(): Int {
        return R.id.notifications_page
    }

    override fun order(): Int {
        return 20
    }

    override fun menuTitleId(): Int {
        return R.string.title_notifications
    }

    override fun menuIconId(): Int {
        return R.drawable.ic_notifications_black_24dp
    }

    override fun message(): String {
        return resource?.getString(R.string.title_notifications) ?: "Notifications"
    }

}
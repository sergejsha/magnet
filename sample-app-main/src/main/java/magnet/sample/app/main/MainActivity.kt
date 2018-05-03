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

package magnet.sample.app.main

import android.arch.lifecycle.LifecycleOwner
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.SparseArray
import android.view.Menu
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.message
import kotlinx.android.synthetic.main.activity_main.navigation
import magnet.Scope
import magnet.bind
import magnet.createSubscope
import magnet.getMany
import magnet.sample.app.App

class MainActivity : FragmentActivity() {

    private lateinit var scope: Scope

    private val pageBinders = SparseArray<PageBinder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create activity subscope and bind some instances into it
        scope = App.scope.createSubscope {
            bind<Resources>(resources)
            bind<LifecycleOwner>(this@MainActivity)
        }

        // query all implementations of Page interface
        val pages = scope.getMany<Page>()

        // use pages for creating ui
        pages.forEach { page ->
            PageBinder(page).register(navigation.menu, message, pageBinders)
        }

        // select initial message
        if (savedInstanceState == null) {
            pageBinders[navigation.selectedItemId].updateMessage(message)
        }

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // restore message for selected page
        pageBinders[navigation.selectedItemId].updateMessage(message)
    }

}

internal class PageBinder(
    private val page: Page
) {

    fun register(menu: Menu, message: TextView, pageBinders: SparseArray<PageBinder>) {
        menu.add(0, page.id(), page.order(), page.menuTitleId())
            .setIcon(page.menuIconId())
            .setOnMenuItemClickListener {
                updateMessage(message)
                false
            }

        pageBinders.put(page.id(), this)
    }

    fun updateMessage(message: TextView) {
        message.text = page.message()
    }

}
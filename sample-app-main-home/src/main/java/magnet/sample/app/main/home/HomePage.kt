package magnet.sample.app.main.home

import android.content.res.Resources
import magnet.Implementation
import magnet.sample.app.main.Page

@Implementation(forType = Page::class)
class HomePage(
        // mandatory dependency available through DependencyScope (see MainActivity.kt)
        val resources: Resources
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
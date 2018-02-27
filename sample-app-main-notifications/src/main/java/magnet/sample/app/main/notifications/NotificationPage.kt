package magnet.sample.app.main.notifications

import android.content.res.Resources
import magnet.Implementation
import magnet.sample.app.main.Page

@Implementation(forType = Page::class)
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
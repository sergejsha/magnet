package magnet.sample.app.main.dashboard

import magnet.Implementation
import magnet.sample.app.main.Page

@Implementation(forType = Page::class)
class DashboardPage : Page {

    override fun id(): Int {
        return R.id.dashboard_page
    }

    override fun order(): Int {
        return 10
    }

    override fun menuIconId(): Int {
        return R.drawable.ic_dashboard_black_24dp
    }

    override fun menuTitleId(): Int {
        return R.string.title_dashboard
    }

    override fun message(): String {
        return "Dashboard"
    }

}
package magnet.sample.app

import android.app.Application
import magnet.DependencyScope
import magnet.ImplementationManager
import magnet.Magnet

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        implManager = Magnet.getImplementationManager()
        appScope = Magnet.createDependencyScope()
    }

    companion object {
        lateinit var implManager: ImplementationManager
            private set

        lateinit var appScope: DependencyScope
            private set
    }

}
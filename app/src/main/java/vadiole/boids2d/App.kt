package vadiole.boids2d

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Preferences.init(this)
    }
}

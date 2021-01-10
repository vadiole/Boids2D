package vadiole.boids2d

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    override fun onCreate() {
        try {
            context = applicationContext
        } catch (e: Exception) {
        }
        super.onCreate()
        context = applicationContext

        Config.init(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    companion object {
        lateinit var context: Context
    }
}

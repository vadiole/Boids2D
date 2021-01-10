package vadiole.boids2d.global.extensions

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import vadiole.boids2d.App

fun Throwable.log(message: String? = null) {
    var firebase: FirebaseApp? = null
    try {
        firebase = FirebaseApp.getInstance()
    } catch (e: Exception) {
        if (firebase == null) {
            FirebaseApp.initializeApp(App.context) ?: return
        }
    }
    with(FirebaseCrashlytics.getInstance()) {
        message?.let { log(message) }
        recordException(this@log)
    }
    Log.e("FIREBASE", message, this)
}

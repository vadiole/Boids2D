package vadiole.boids2d

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.namaztime.qibla.tools.extensions.getDouble
import com.namaztime.qibla.tools.extensions.putDouble


object Preferences {
    private const val SHARED_PREFERENCES_NAME = "NamazTime"

    //  region preferences operators
    private lateinit var preferences: SharedPreferences
    private lateinit var context: Context

    /**
     * call on start up to init preferences
     */
    @Suppress("DEPRECATION")
    fun init(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        this.context = context
    }

    /**
     * puts a value for the given [key].
     */
    operator fun SharedPreferences.set(key: String, value: Any?) = when (value) {
        is String? -> edit { putString(key, value) }
        is Int -> edit { putInt(key, value) }
        is Boolean -> edit { putBoolean(key, value) }
        is Float -> edit { putFloat(key, value) }
        is Long -> edit { putLong(key, value) }
        is Double -> edit { putDouble(key, value) }
        else -> throw UnsupportedOperationException("Not yet implemented")
    }

    /**
     * finds a preference based on the given [key].
     * [T] is the type of value
     * @param default optional defaultValue - will take a default defaultValue if it is not specified
     */
    inline operator fun <reified T : Any> SharedPreferences.get(
        key: String,
        default: T? = null
    ): T = when (T::class) {
        String::class -> getString(key, default as? String ?: "") as T
        Int::class -> getInt(key, default as? Int ?: -1) as T
        Boolean::class -> getBoolean(key, default as? Boolean ?: false) as T
        Float::class -> getFloat(key, default as? Float ?: -1f) as T
        Long::class -> getLong(key, default as? Long ?: -1) as T
        Double::class -> getDouble(key, default as? Double ?: -1.0) as T
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
    // endregion


    //  template

    private const val KEY = "preferenceKey"
    var template: String
        get() = preferences[KEY, null]
        set(value) = run { preferences[KEY] = value }

    private const val TUTORIAL_SETTINGS_SHOWN_KEY = "tutorialSettingsShown"
    var tutorialSettingsShown: Boolean
        get() = preferences[TUTORIAL_SETTINGS_SHOWN_KEY, false]
        set(value) = run { preferences[TUTORIAL_SETTINGS_SHOWN_KEY] = value }

    private const val BOIDS_COUNT_KEY = "boidsCountKey"
    var boidsCount: Int
        get() = preferences[BOIDS_COUNT_KEY, 500]
        set(value) = run { preferences[BOIDS_COUNT_KEY] = value }


    private const val BOIDS_SIZE_KEY = "boidsSizeKey"
    var boidsSize: Int
        get() = preferences[BOIDS_SIZE_KEY, 8]
        set(value) = run { preferences[BOIDS_SIZE_KEY] = value }
}

package vadiole.boids2d

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.core.content.edit
import vadiole.boids2d.global.DevicePerformance
import vadiole.boids2d.global.extensions.getDouble
import vadiole.boids2d.global.extensions.putDouble
import vadiole.colorpicker.ColorModel


@SuppressLint("StaticFieldLeak")
object Config {
    private const val SHARED_PREFERENCES_NAME = "boids2d_config"

    //  region preferences operators
    private lateinit var preferences: SharedPreferences
    private lateinit var context: Context

    /**
     * call on start up to init preferences
     */
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
        is DevicePerformance -> edit { putString(key, value.code) }
        is ColorModel -> edit { putString(key, value.name) }
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
        DevicePerformance::class -> DevicePerformance.lookupByCode(
            getString(key, null) ?: (default as DevicePerformance).code
        ) as T
        ColorModel::class -> ColorModel.fromName(getString(key, (default as ColorModel).name)) as T
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
    // endregion


    //  template

    private const val KEY = "preferenceKey"
    var template: String
        get() = preferences[KEY, null]
        set(value) = preferences.set(KEY, value)

    private const val TUTORIAL_SETTINGS_SHOWN_KEY = "tutorialSettingsShown"
    var tutorialSettingsShown: Boolean
        get() = preferences[TUTORIAL_SETTINGS_SHOWN_KEY, false]
        set(value) = preferences.set(TUTORIAL_SETTINGS_SHOWN_KEY, value)

    private const val TUTORIAL_EXIT_SETTINGS_SHOWN_KEY = "tutorialExitSettingsShown"
    var tutorialExitSettingsShown: Boolean
        get() = preferences[TUTORIAL_EXIT_SETTINGS_SHOWN_KEY, false]
        set(value) = preferences.set(TUTORIAL_EXIT_SETTINGS_SHOWN_KEY, value)


    //  colors
    private const val BOIDS_COLOR_KEY = "boidsColorKey"
    var boidsColor: Int
        get() = preferences[BOIDS_COLOR_KEY, Color.WHITE]
        set(value) = preferences.set(BOIDS_COLOR_KEY, value)

    private const val BACKGROUND_COLOR_KEY = "backgroundColorKey"
    var backgroundColor: Int
        get() = preferences[BACKGROUND_COLOR_KEY, Color.BLACK]
        set(value) = preferences.set(BACKGROUND_COLOR_KEY, value)

    private const val COLOR_PICKER_MODEL_KEY = "colorPickerModel"
    var colorPickerModel: ColorModel
        get() = preferences[COLOR_PICKER_MODEL_KEY, ColorModel.HSV]
        set(value) = preferences.set(COLOR_PICKER_MODEL_KEY, value)

    //  settings
    private const val BOIDS_COUNT_KEY = "boidsCountKey"
    var boidsCount: Int
        get() = preferences[BOIDS_COUNT_KEY, 500]
        set(value) = preferences.set(BOIDS_COUNT_KEY, value)

    private const val BOIDS_SIZE_KEY = "boidsSizeKey"
    var boidsSize: Int
        get() {
            val size = preferences[BOIDS_SIZE_KEY, 3]
            return if (size >= 16) 16 else size
        }
        set(value) = preferences.set(BOIDS_SIZE_KEY, value)

    //  advanced settings
    private const val ADVANCED_EXTENDED_KEY = "advanced_extended"
    var advancedExtended: Boolean
        get() = preferences[ADVANCED_EXTENDED_KEY, false]
        set(value) = preferences.set(ADVANCED_EXTENDED_KEY, value)

    private const val USER_SEPARATION_KEY = "userSeparation"
    var userSeparation: Int
        get() = preferences[USER_SEPARATION_KEY, 10]
        set(value) = preferences.set(USER_SEPARATION_KEY, value)


    private const val USER_ALIGNMENT_KEY = "userAlignment"
    var userAlignment: Int
        get() = preferences[USER_ALIGNMENT_KEY, 10]
        set(value) = preferences.set(USER_ALIGNMENT_KEY, value)

    private const val USER_COHESION_KEY = "userCohesion"
    var userCohesion: Int
        get() = preferences[USER_COHESION_KEY, 10]
        set(value) = preferences.set(USER_COHESION_KEY, value)


    private const val USER_TARGET_KEY = "userTarget"
    var userTarget: Int
        get() = preferences[USER_TARGET_KEY, 10]
        set(value) = preferences.set(USER_TARGET_KEY, value)

    private const val DEVICE_PERFORMANCE_KEY = "devicePerformanceKey"
    var devicePerformance: DevicePerformance
        get() = preferences[DEVICE_PERFORMANCE_KEY, DevicePerformance.getDevicePerformance()]
        set(value) = preferences.set(DEVICE_PERFORMANCE_KEY, value)

}

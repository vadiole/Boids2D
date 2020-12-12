package vadiole.boids2d.global.extensions

import android.content.SharedPreferences

fun SharedPreferences.Editor.putDouble(key: String, double: Double): SharedPreferences.Editor {
    return putLong(key, java.lang.Double.doubleToRawLongBits(double))
}

fun SharedPreferences.getDouble(key: String, default: Double): Double {
    return java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))
}

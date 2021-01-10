package vadiole.boids2d.model

import android.util.Log
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

class Vector(var x: Float, var y: Float, var z: Float) {
    fun add(that: Vector): Vector {
        x += that.x
        y += that.y
        z += that.z
        return this
    }

    fun subtract(that: Vector): Vector {
        x -= that.x
        y -= that.y
        z -= that.z
        return this
    }

    fun limit(limit: Float): Vector {
        val max = abs(x).coerceAtLeast(abs(y).coerceAtLeast(abs(z)))
        if (max > limit) {
            val scaleFactor = limit / max
            x *= scaleFactor
            y *= scaleFactor
            z *= scaleFactor
        }
        return this
    }

    fun multiply(c: Float): Vector {
        x *= c
        y *= c
        z *= c
        return this
    }

    fun divide(c: Float): Vector {
        x /= c
        y /= c
        z /= c
        return this
    }

    fun magnitude(): Float {
        return sqrt(magnitude2())
    }

    fun magnitude2(): Float {
        return x * x + y * y + z * z
    }

    fun multiply(that: Vector): Float {
        return x * that.x + y * that.y + z * that.z
    }

    fun normalize(): Vector {
        return divide(magnitude())
    }

    fun copyFrom(that: Vector): Vector {
        x = that.x
        y = that.y
        z = that.z
        return this
    }

    fun init(): Vector {
        x = 0f
        y = 0f
        z = 0f
        return this
    }

    fun log(TAG: String = "VECTOR") {
        Log.d(TAG, "x - ${x.roundTo(5)}\t| y - ${y.roundTo(5)}")
    }
}

fun Float.roundTo(n: Int): Float {
    return "%.${n}f".format(Locale.ENGLISH, this).toFloat()
}

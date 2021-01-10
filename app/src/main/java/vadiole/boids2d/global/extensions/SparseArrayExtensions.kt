package vadiole.boids2d.global.extensions

import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.isEmpty

inline fun <T, R : Comparable<R>> SparseArray<out T>.minByOrNull(selector: (T) -> R): T? {
    if (isEmpty()) return null
    if (size() == 1) return this.valueAt(0)
    var minElem: T? = null
    var minValue: R? = null
    forEach { _, e ->
        val v = selector(e)
        if (minValue.let { it == null || it < v }) {
            minElem = e
            minValue = v
        }
    }
    return minElem
}

package vadiole.boids2d.base

import android.view.View
import java.util.concurrent.atomic.AtomicBoolean

class DebouncingOnClickListener(
    private val intervalMillis: Long,
    private val doClick: ((View) -> Unit)
) : View.OnClickListener {

    override fun onClick(v: View) {
        if (enabled.getAndSet(false)) {
            v.postDelayed(ENABLE_AGAIN, intervalMillis)
            doClick(v)
        }
    }

    companion object {
        @JvmStatic
        var enabled = AtomicBoolean(true)
        private val ENABLE_AGAIN = { enabled.set(true) }
    }
}

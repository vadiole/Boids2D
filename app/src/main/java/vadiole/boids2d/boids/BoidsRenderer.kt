package vadiole.boids2d.boids

import android.content.Context
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.util.Log
import android.util.SparseArray
import android.view.Surface
import android.view.WindowManager
import androidx.core.util.forEach
import com.google.firebase.analytics.FirebaseAnalytics
import vadiole.boids2d.Config
import vadiole.boids2d.model.Boid
import vadiole.boids2d.model.Vector
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.floor

class BoidsRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val TAG: String = "BoidsRenderer"
    private val NEIGHBOURS = 7
    private var DISTANCE = 0f
    var boids = emptyArray<Boid>()
    var newBoids = emptyArray<Boid>()
    private var rotation = Surface.ROTATION_0
    private var width = 0
    private var height = 0
    private val tempVector: Vector = Vector(0f, 0f, 0f)
    var distances: Array<FloatArray> = emptyArray()
    private var neigbours: Array<IntArray> = emptyArray()

    private val pointsMaxSize = 10
    private val targets: Array<Vector> = Array(pointsMaxSize) { Vector(1000f, 1000f, 0f) }

    private var grid: Array<Array<BitSet?>> = emptyArray()
    private val temp_dists = FloatArray(NEIGHBOURS)
    private var ratio = 0f
    private var r = 0f
    private var g = 0f
    private var b = 0f


    /*
     * Timestamp of previous frame.  Used for animation.  We cap the maximum inter-frame delta
     * at 0.5 seconds, so that a major hiccup won't cause things to behave too crazily.
     */
    private val NANOS_PER_SECOND = 1000000000.0
    private val MAX_FRAME_DELTA_SEC = 0.5
    private var mPrevFrameWhenNsec: Long = 0

    /*
     * Pause briefly on certain transitions, e.g. before launching a new ball after one was lost.
     */
    private val mPauseDuration = 0f

    /*
     * Debug feature: do the next N frames in slow motion.  Useful when examining collisions.
     * The speed will ramp up to normal over the last 60 frames.  (This is a debug feature, not
     * part of the game, so we just count frames and assume the panel is somewhere near 60fps.)
     * See DEBUG_COLLISIONS for example usage.
     */
    private val mDebugSlowMotionFrames = 0

    // If FRAME_RATE_SMOOTHING is true, then the rest of these fields matter.
    private val FRAME_RATE_SMOOTHING = true
    private val RECENT_TIME_DELTA_COUNT = 5
    var mRecentTimeDelta = DoubleArray(RECENT_TIME_DELTA_COUNT)
    var mRecentTimeDeltaNext = 0

    private var lastFpsLogTime = 0L
    private var frameCounter = 0


    override fun onDrawFrame(gl: GL10) {
        logFps()
        calculateScene()
        gl.glClearColor(r, g, b, 1.0f)
        gl.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL11.GL_COLOR_ARRAY)

        for (boid in boids) {
            gl.glLoadIdentity()
            gl.glTranslatef(boid.location.x, boid.location.y, -DISTANCE + boid.location.z)
            tempVector.copyFrom(boid.velocity).normalize()
            val theta = atan2(tempVector.y, tempVector.x) * 57.3f
            val fi = acos(tempVector.z / tempVector.magnitude()) * 57.3f
            gl.glRotatef(-90f, 0f, 0f, 1f)
            gl.glRotatef(theta, 0f, 0f, 1f)
            gl.glRotatef(fi, 0f, 1f, 0f)
            boid.draw(gl)
        }
    }

    private fun logFps() {
        frameCounter++
        val currentTime = System.nanoTime()
        if (currentTime - lastFpsLogTime > 1000000000) {
            Log.i(TAG, "fps: $frameCounter")
            frameCounter = 0
            lastFpsLogTime = currentTime
        }
    }


    private fun index(_x: Float): Int {
        var x = _x
        if (abs(x) > 4f) x = (if (x > 0) 1 else 1) * 4f
        x += 4f
        x *= 3f
        if (x >= 24) x = 23f
        return floor(x.toDouble()).toInt()
    }

    private fun calculateScene() {
        // First frame has no time delta, so make it a no-op.
        if (mPrevFrameWhenNsec == 0L) {
            mPrevFrameWhenNsec = System.nanoTime();     // use monotonic clock
            mRecentTimeDeltaNext = -1;                  // reset saved values
            return;
        }

        val nowNsec = System.nanoTime()
        var curDeltaSec = (nowNsec - mPrevFrameWhenNsec) / NANOS_PER_SECOND
        if (curDeltaSec > MAX_FRAME_DELTA_SEC) {
            // We went to sleep for an extended period.  Cap it at a reasonable limit.
            Log.d(TAG, "delta time was $curDeltaSec, capping at $MAX_FRAME_DELTA_SEC")
            curDeltaSec = MAX_FRAME_DELTA_SEC
        }
        var deltaSec: Double

        if (FRAME_RATE_SMOOTHING) {
            if (mRecentTimeDeltaNext < 0) {
                // first time through, fill table with current value
                for (i in 0 until RECENT_TIME_DELTA_COUNT) {
                    mRecentTimeDelta[i] = curDeltaSec
                }
                mRecentTimeDeltaNext = 0
            }
            mRecentTimeDelta[mRecentTimeDeltaNext] = curDeltaSec
            mRecentTimeDeltaNext = (mRecentTimeDeltaNext + 1) % RECENT_TIME_DELTA_COUNT
            deltaSec = 0.0
            for (i in 0 until RECENT_TIME_DELTA_COUNT) {
                deltaSec += mRecentTimeDelta[i]
            }
            deltaSec /= RECENT_TIME_DELTA_COUNT.toDouble()
        } else {
            deltaSec = curDeltaSec
        }

        for (i in grid.indices) {
            for (j in grid.indices) {
                grid[i][j]!!.clear()
                grid[j][i]!!.clear()
            }
        }
        for (b in boids.indices) {
            val i = index(boids[b].location.x)
            val j = index(boids[b].location.y)
            grid[i][j]!!.set(b)
        }
        for (b in boids.indices) {
            var count = 0
            val x = index(boids[b].location.x)
            val y = index(boids[b].location.y)
            var r = 0
            while (count < neigbours[b].size) {
                var i = 0.coerceAtLeast(x - r)
                while (i <= x + r && i < grid.size) {
                    var j = 0.coerceAtLeast(y - r)
                    while (j <= y + r && j < grid.size) {
                        if (abs(x - i) == r || abs(y - j) == r) {
                            var bit = 0
                            while (grid[i][j]!!.nextSetBit(bit) >= 0 && count < neigbours[b].size && !grid[i][j]!!
                                    .isEmpty
                            ) {
                                bit = grid[i][j]!!.nextSetBit(bit)
                                neigbours[b][count] = bit
                                count++
                                bit++
                            }
                        }
                        j++
                    }
                    i++
                }
                r++
            }
            for (n in neigbours[b].indices) {
                val distance: Float = tempVector.copyFrom(boids[b].location)
                    .subtract(boids[neigbours[b][n]].location).magnitude2()
                distances[b][neigbours[b][n]] = distance
            }
        }
        for (b in boids.indices) {
            newBoids[b].copyFrom(boids[b])
        }

        for (b in boids.indices) {
            boids[b].step(deltaSec, newBoids, neigbours[b], distances[b], targets)
        }
        Log.v(TAG, "calculateScene: delta - $deltaSec")
        mPrevFrameWhenNsec = nowNsec
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        mPrevFrameWhenNsec = 0


        this.width = width
        this.height = height
        ratio = width.toFloat() / height
        rotation = (context.applicationContext
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            .rotation
        when (rotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> DISTANCE = 12f
            Surface.ROTATION_90, Surface.ROTATION_270 -> DISTANCE = 12f / ratio
        }
        gl.glViewport(0, 0, width, height)
        gl.glMatrixMode(GL11.GL_PROJECTION)
        gl.glLoadIdentity()
        GLU.gluPerspective(gl, 45f, ratio, DISTANCE - 8f, DISTANCE + 8f)
        gl.glMatrixMode(GL11.GL_MODELVIEW)
        gl.glLoadIdentity()
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        frameCounter = 0

        val size = Config.boidsSize
        val boidsColor = Config.boidsColor
        Boid.initModel(size / 260f, boidsColor)
        val backgroundColor = Config.backgroundColor
        r = Color.red(backgroundColor) / 255f
        g = Color.green(backgroundColor) / 255f
        b = Color.blue(backgroundColor) / 255f
        val count = Config.boidsCount
        boids = Array(count) { Boid() }
        newBoids = Array(count) { Boid() }
        distances = Array(boids.size) { FloatArray(boids.size) }
        neigbours = Array(boids.size) { IntArray(NEIGHBOURS) }
        grid = Array(64) { arrayOfNulls(64) }
        for (i in grid.indices) {
            for (j in grid.indices) {
                grid[i][j] = BitSet()
                grid[j][i] = BitSet()
            }
        }
        gl.glClearDepthf(1.0f)
        gl.glEnable(GL10.GL_DEPTH_TEST)
        gl.glDepthFunc(GL10.GL_LEQUAL)
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)
        gl.glShadeModel(GL10.GL_SMOOTH)
        gl.glDisable(GL10.GL_DITHER)

        FirebaseAnalytics.getInstance(context.applicationContext).run {
            setUserProperty("boids_color", String.format("#%06X", 0xFFFFFF and boidsColor))
            setUserProperty("background_color", String.format("#%06X", 0xFFFFFF and backgroundColor))
            setUserProperty("boids_count", count.toString())
            setUserProperty("boids_size", size.toString())
        }
    }

    fun onTouch(array: SparseArray<Vector>) {
        var i = 0
        array.forEach { key, vector ->
            if (i >= targets.size) return@forEach

            targets[i].x = (vector.x - width / 2f) / width * (ratio * DISTANCE) * 0.9f
            targets[i].y = (height / 2f - vector.y) / height * DISTANCE * 0.9f
            targets[i].z = vector.z
            i++
        }
        for (j in i until targets.size) {
            targets[j].x = 1000f
            targets[j].y = 1000f
            targets[j].z = 0f
        }
    }
}

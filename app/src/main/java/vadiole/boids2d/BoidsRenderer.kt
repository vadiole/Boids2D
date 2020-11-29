package vadiole.boids2d

import android.content.Context
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.view.Surface
import android.view.WindowManager
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.floor

class BoidsRenderer(private val context: Context) : GLSurfaceView.Renderer {

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
    private var grid: Array<Array<BitSet?>> = emptyArray()
    private val temp_dists = FloatArray(NEIGHBOURS)
    private var ratio = 0f
    private var r = 0f
    private var g = 0f
    private var b = 0f

    var startTime = System.currentTimeMillis()
    var dt: Long = 0
    private var rotationDebug = 0f


    override fun onDrawFrame(gl: GL10) {
        dt = System.currentTimeMillis() - startTime
//        if (dt < 30) {
//            try {
//                Thread.sleep(30 - dt)
//            } catch (e: InterruptedException) {
//            }
//        }
        startTime = System.currentTimeMillis()
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


    private fun index(x: Float): Int {
        var x = x
        if (abs(x) > 4f) x = (if (x > 0) 1 else 1) * 4f
        x += 4f
        x *= 3f
        if (x >= 24) x = 23f
        return floor(x.toDouble()).toInt()
    }

    private fun calculateScene() {
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
            boids[b].step(newBoids, neigbours[b], distances[b])
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
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
        GLU.gluPerspective(gl, 45f, ratio, DISTANCE - 5f, DISTANCE + 5f)
        gl.glMatrixMode(GL11.GL_MODELVIEW)
        gl.glLoadIdentity()
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        val size = Preferences.boidsSize / 260f
        val model = Color.WHITE
        Boid.initModel(size, model)
        val backgroud = -0x1000000
        r = Color.red(backgroud) / 255f
        g = Color.green(backgroud) / 255f
        b = Color.blue(backgroud) / 255f
        val count = Preferences.boidsCount
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
    }

    fun touch(x: Float, y: Float) {
        val relx = (x - width / 2f) / width * (ratio * DISTANCE)
        val rely = (height / 2f - y) / height * DISTANCE
        for (boid in boids) {
            boid.velocity.x = relx - boid.location.x
            boid.velocity.y = rely - boid.location.y
            boid.velocity.z = 0 - boid.location.z
            boid.velocity.copyFrom(tempVector.copyFrom(boid.velocity).normalize())
        }
    }
}

package vadiole.boids2d

import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*
import javax.microedition.khronos.opengles.GL10


class Boid {
    var location: Vector
    var velocity: Vector
    fun draw(gl: GL10) {
        gl.glFrontFace(GL10.GL_CCW)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer)
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer)
        gl.glDrawElements(GL10.GL_TRIANGLES, 12, GL10.GL_UNSIGNED_BYTE, mIndexBuffer)
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY)
        gl.glFrontFace(GL10.GL_CW)
    }

    fun step(boids: Array<Boid>, neigbours: IntArray, distances: FloatArray) {
        val acceleration = flock(boids, neigbours, distances)
        velocity.add(acceleration).limit(MAX_VELOCITY)
        location.add(velocity)
    }

    private fun flock(boids: Array<Boid>, neigbours: IntArray, distances: FloatArray): Vector {
        val separation = separate(boids, neigbours, distances).multiply(
            SEPARATION_WEIGHT
        )
        val alignment = align(boids, neigbours).multiply(ALIGNMENT_WEIGHT)
        val cohesion = cohere(boids, neigbours).multiply(COHESION_WEIGHT)
        return separation.add(alignment).add(cohesion)
    }

    private fun cohere(boids: Array<Boid>, neigbours: IntArray): Vector {
        sum.init()
        for (n in neigbours) {
            sum.add(boids[n].location)
        }
        sum.z /= 2
        return steerTo(sum.divide(neigbours.size + CENTRIC_POWER))
    }

    private fun steerTo(target: Vector): Vector {
        val desired = target.subtract(location)
        val d = desired.magnitude()
        return if (d > 0) {
            desired.normalize()
            if (d < INERTION) {
                desired.multiply(MAX_VELOCITY * d / INERTION)
            } else {
                desired.multiply(MAX_VELOCITY)
            }
            desired.subtract(velocity).limit(MAX_FORCE)
        } else {
            Vector(0f, 0f, 0f)
        }
    }

    private fun align(boids: Array<Boid>, neigbours: IntArray): Vector {
        align.init()
        for (n in neigbours) {
            align.add(boids[n].velocity)
        }
        align.divide(neigbours.size.toFloat())
        return align.limit(MAX_FORCE)
    }

    private fun separate(boids: Array<Boid>, neigbours: IntArray, distances: FloatArray): Vector {
        separate.init()
        var count = 0
        for (n in neigbours) {
            val boid = boids[n]
            val d = distances[n]
            tempVector.copyFrom(location).subtract(boid.location)
            if (d > 0 && d < DESIRED_SEPARATION) {
                separate.add(
                    tempVector.divide(
                        Math.sqrt(d.toDouble())
                            .toFloat()
                    )
                )
                count++
            }
        }
        if (count != 0) {
            separate.divide(count.toFloat())
        }
        return separate
    }

    fun copyFrom(boid: Boid) {
        location.copyFrom(boid.location)
        velocity.copyFrom(boid.velocity)
    }

    companion object {
        var side = 0.015f
        private const val MAX_VELOCITY = 0.031f
        private const val DESIRED_SEPARATION = 0.01f
        private const val SEPARATION_WEIGHT = 0.05f
        private const val ALIGNMENT_WEIGHT = 0.5f
        private const val COHESION_WEIGHT = 0.3f
        private const val MAX_FORCE = 0.005f
        private const val INERTION = 0.001f
        private const val CENTRIC_POWER = 0.81f // > 0 more power
        private var mFVertexBuffer: FloatBuffer? = null
        private var mColorBuffer: FloatBuffer? = null
        private var mIndexBuffer: ByteBuffer? = null
        private val tempVector = Vector(0f, 0f, 0f)
        private val sum = Vector(0f, 0f, 0f)
        private val align = Vector(0f, 0f, 0f)
        private val separate = Vector(0f, 0f, 0f)
        fun initModel(size: Float, model: Int) {
            side = size
            val indices = byteArrayOf( // Vertex indices of the 4 Triangles
                2, 4, 3,  // front face (CCW)
                1, 4, 2,  // right face
                0, 4, 1,  // back face
                4, 0, 3 // left face
            )
            val vertices = floatArrayOf( //
                -side / 2f, -side, -side / 2f,  // 0. left-bottom-back
                side / 2f, -side, -side / 2f,  // 1. right-bottom-back
                side / 2f, -side, side / 2f,  // 2. right-bottom-front
                -side / 2f, -side, side / 2f,  // 3. left-bottom-front
                0.0f, side * 2, 0.0f // 4. top
            )
            val colors = floatArrayOf( //
                (Color.red(model) - 40) / 255f,
                (Color.green(model) - 40) / 255f,
                (Color.blue(model) - 40) / 255f,
                0.5f,  //
                (Color.red(model) - 40) / 255f,
                (Color.green(model) - 40) / 255f,
                (Color.blue(model) - 40) / 255f,
                0.5f,  //
                (Color.red(model) - 40) / 255f,
                (Color.green(model) - 40) / 255f,
                (Color.blue(model) - 40) / 255f,
                0.5f,  //
                (Color.red(model) - 40) / 255f,
                (Color.green(model) - 40) / 255f,
                (Color.blue(model) - 40) / 255f,
                0.5f,  //
                (Color.red(model) + 0) / 255f,
                (Color.green(model) + 0) / 255f,
                (Color.blue(model) + 0) / 255f,
                1.0f // nose
            )
            var vbb = ByteBuffer.allocateDirect(vertices.size * 4)
            vbb.order(ByteOrder.nativeOrder())
            mFVertexBuffer = vbb.asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
            vbb = ByteBuffer.allocateDirect(colors.size * 4)
            vbb.order(ByteOrder.nativeOrder())
            mColorBuffer = vbb.asFloatBuffer().apply {
                put(colors)
                position(0)
            }
            mIndexBuffer = ByteBuffer.allocateDirect(indices.size).apply {
                put(indices)
                position(0)
            }
        }
    }

    init {
        val r = Random()
        location = Vector( //
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() * 2,  //
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() * 2,  //
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() * 0.5f
//            0f
        )
        velocity = Vector( //
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() / 100f,  //
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() / 100f,  //
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() / 100f
//            0f
        )
    }
}

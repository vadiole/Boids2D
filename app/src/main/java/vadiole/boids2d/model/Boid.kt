package vadiole.boids2d.model

import android.graphics.Color
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*
import javax.microedition.khronos.opengles.GL10
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt


class Boid {
    var location: Vector
    var velocity: Vector
    var distance: Vector
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

    fun step(
        deltaSec: Double,
        boids: Array<Boid>,
        neighbours: IntArray,
        distances: FloatArray,
        targets: Array<Vector>
    ) {
        val acceleration = flock(boids, neighbours, distances, targets)
        velocity.add(acceleration).limit(MAX_VELOCITY)
        distance.copyFrom(velocity).multiply(60 * deltaSec.toFloat())
        location.add(distance)
    }

    private fun flock(
        boids: Array<Boid>,
        neighbours: IntArray,
        distances: FloatArray,
        targets: Array<Vector>
    ): Vector {
        val separation =
            separate(boids, neighbours, distances).multiply(SEPARATION_WEIGHT * userSeparation)
        val alignment = align(boids, neighbours).multiply(ALIGNMENT_WEIGHT * userAlignment)
        val cohesion = cohere(boids, neighbours).multiply(COHESION_WEIGHT * userCohesion)
        val toTarget = target(targets).multiply(TARGET_WEIGHT * userTarget)
        return separation.add(alignment).add(cohesion).add(toTarget)
    }


    private fun target(targets: Array<Vector>): Vector {
        val nearestTarget = targets.minByOrNull { it ->
            target.copyFrom(it)
            target.z = 0f
            tempLocation.copyFrom(location)
            tempLocation.z = 0f
            return@minByOrNull target.subtract(tempLocation).magnitude()
        } ?: return target.init()
        if (nearestTarget.x > 100) return target.init()
        val force = target.copyFrom(nearestTarget)
        return steerTo(force)
    }

    private fun cohere(boids: Array<Boid>, neighbours: IntArray): Vector {
        sum.init()
        for (n in neighbours) {
            sum.add(boids[n].location)
        }
        sum.z /= 2
        return steerTo(sum.divide(neighbours.size + CENTRIC_POWER))
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

    private fun align(boids: Array<Boid>, neighbours: IntArray): Vector {
        align.init()
        for (n in neighbours) {
            align.add(boids[n].velocity)
        }
        align.divide(neighbours.size.toFloat())
        return align.limit(MAX_FORCE)
    }

    private fun separate(boids: Array<Boid>, neighbours: IntArray, distances: FloatArray): Vector {
        separate.init()
        var count = 0
        for (n in neighbours) {
            val boid = boids[n]
            val d = distances[n]
            tempVector.copyFrom(location).subtract(boid.location)
            if (d > 0 && d < DESIRED_SEPARATION) {
                separate.add(
                    tempVector.divide(
                        sqrt(d)
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
        var userSeparation: Float = 1f
        var userAlignment: Float = 1f
        var userCohesion: Float = 1f
        var userTarget: Float = 1f

        const val MAX_VELOCITY = 0.035f
        private const val DESIRED_SEPARATION = 0.01f
        private const val SEPARATION_WEIGHT = 0.05f
        private const val ALIGNMENT_WEIGHT = 0.3f
        private const val COHESION_WEIGHT = 0.3f
        private const val TARGET_WEIGHT = 0.7f
        private const val MAX_FORCE = 0.005f
        private const val INERTION = 0.0012f
        private const val CENTRIC_POWER = 0.91f // > 0 more power
        private var mFVertexBuffer: FloatBuffer? = null
        private var mColorBuffer: FloatBuffer? = null
        private var mIndexBuffer: ByteBuffer? = null
        private val tempVector = Vector(0f, 0f, 0f)
        private val tempLocation = Vector(0f, 0f, 0f)
        private val sum = Vector(0f, 0f, 0f)
        private val align = Vector(0f, 0f, 0f)
        private val separate = Vector(0f, 0f, 0f)
        private val target = Vector(0f, 0f, 0f)
        fun initModel(
            size: Float,
            color: Int,
            separation: Int,
            alignment: Int,
            cohesion: Int,
            target: Int
        ) {
            side = size
            userSeparation = mapToRange(separation.toFloat(), 0f, 20f, 0f, 2f, isLog = false)
            userAlignment = mapToRange(alignment.toFloat(), 0f, 20f, 0f, 2f, isLog = false)
            userCohesion = mapToRange(cohesion.toFloat(), 0f, 20f, 0f, 2f, isLog = false)
            userTarget = mapToRange(target.toFloat(), 0f, 20f, 0f, 2f, isLog = false)

            Log.d(
                "BOID",
                "separation: $userSeparation, alignment: $userAlignment, cohesion: $userCohesion, target: $userTarget"
            );

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
                (Color.red(color)) / 255f,
                (Color.green(color)) / 255f,
                (Color.blue(color)) / 255f,
                0.5f,  //
                (Color.red(color)) / 255f,
                (Color.green(color)) / 255f,
                (Color.blue(color)) / 255f,
                0.5f,  //
                (Color.red(color)) / 255f,
                (Color.green(color)) / 255f,
                (Color.blue(color)) / 255f,
                0.5f,  //
                (Color.red(color)) / 255f,
                (Color.green(color)) / 255f,
                (Color.blue(color)) / 255f,
                0.5f,  //
                (Color.red(color) + 0) / 255f,
                (Color.green(color) + 0) / 255f,
                (Color.blue(color) + 0) / 255f,
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
            vbb = ByteBuffer.allocateDirect(indices.size)
            vbb.order(ByteOrder.nativeOrder())
            mIndexBuffer = vbb.apply {
                put(indices)
                position(0)
            }
        }

        @Suppress("SameParameterValue")
        private fun mapToRange(
            oldValue: Float,
            oldMin: Float,
            oldMax: Float,
            newMin: Float,
            newMax: Float,
            isLog: Boolean = false
        ): Float {

            val oldRange = (oldMax - oldMin)
            val newRange = (newMax - newMin)
            val newValue = (((oldValue - oldMin) * newRange) / oldRange) + newMin

            return if (isLog) {
                lin2log(newMin.coerceAtLeast(0.001f), newMax, newValue)
            } else {
                newValue
            }
        }


        private fun lin2log(min: Float, max: Float, value: Float): Float {
            val b: Float = ln((max / min) / (max - min))
            val a: Float = max / exp(b * max)
            val tempAnswer: Float = a * exp(b * value)
            return (tempAnswer - 1f).coerceAtLeast(0f)
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

        distance = Vector(0f, 0f, 0f)
    }


}

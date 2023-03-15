package com.teamwizardry.librarianlib.math

import com.teamwizardry.librarianlib.core.bridge.IMatrix4f
import com.teamwizardry.librarianlib.core.util.kotlin.threadLocal
import com.teamwizardry.librarianlib.core.util.mixinCast
import com.teamwizardry.librarianlib.core.util.vec
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3d
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.tan

// adapted from flow/math: https://github.com/flow/math
public open class Matrix4d(
    public open val m00: Double,
    public open val m01: Double,
    public open val m02: Double,
    public open val m03: Double,
    public open val m10: Double,
    public open val m11: Double,
    public open val m12: Double,
    public open val m13: Double,
    public open val m20: Double,
    public open val m21: Double,
    public open val m22: Double,
    public open val m23: Double,
    public open val m30: Double,
    public open val m31: Double,
    public open val m32: Double,
    public open val m33: Double
): Cloneable {

    public constructor(m: Matrix4d): this(
        m.m00, m.m01, m.m02, m.m03,
        m.m10, m.m11, m.m12, m.m13,
        m.m20, m.m21, m.m22, m.m23,
        m.m30, m.m31, m.m32, m.m33)

    public constructor(): this(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    public constructor(
        m00: Float, m01: Float, m02: Float, m03: Float,
        m10: Float, m11: Float, m12: Float, m13: Float,
        m20: Float, m21: Float, m22: Float, m23: Float,
        m30: Float, m31: Float, m32: Float, m33: Float): this(
        m00.toDouble(), m01.toDouble(), m02.toDouble(), m03.toDouble(),
        m10.toDouble(), m11.toDouble(), m12.toDouble(), m13.toDouble(),
        m20.toDouble(), m21.toDouble(), m22.toDouble(), m23.toDouble(),
        m30.toDouble(), m31.toDouble(), m32.toDouble(), m33.toDouble())

    @Suppress("CAST_NEVER_SUCCEEDS")
    public constructor(m: Matrix4f): this(
        (m as IMatrix4f).m00,
        (m as IMatrix4f).m01,
        (m as IMatrix4f).m02,
        (m as IMatrix4f).m03,
        (m as IMatrix4f).m10,
        (m as IMatrix4f).m11,
        (m as IMatrix4f).m12,
        (m as IMatrix4f).m13,
        (m as IMatrix4f).m20,
        (m as IMatrix4f).m21,
        (m as IMatrix4f).m22,
        (m as IMatrix4f).m23,
        (m as IMatrix4f).m30,
        (m as IMatrix4f).m31,
        (m as IMatrix4f).m32,
        (m as IMatrix4f).m33
    )

    public constructor(stack: MatrixStack): this(stack.peek().positionMatrix)

    public operator fun get(row: Int, col: Int): Double {
        when (row) {
            0 -> when (col) {
                0 -> return m00
                1 -> return m01
                2 -> return m02
                3 -> return m03
            }
            1 -> when (col) {
                0 -> return m10
                1 -> return m11
                2 -> return m12
                3 -> return m13
            }
            2 -> when (col) {
                0 -> return m20
                1 -> return m21
                2 -> return m22
                3 -> return m23
            }
            3 -> when (col) {
                0 -> return m30
                1 -> return m31
                2 -> return m32
                3 -> return m33
            }
        }
        throw IllegalArgumentException(
            (if (row < 0 || row > 3) "row must be greater than zero and smaller than 3. " else "") + if (col < 0 || col > 3) "col must be greater than zero and smaller than 3." else "")
    }

    public open fun add(m: Matrix4d): Matrix4d {
        return Matrix4d(
            m00 + m.m00, m01 + m.m01, m02 + m.m02, m03 + m.m03,
            m10 + m.m10, m11 + m.m11, m12 + m.m12, m13 + m.m13,
            m20 + m.m20, m21 + m.m21, m22 + m.m22, m23 + m.m23,
            m30 + m.m30, m31 + m.m31, m32 + m.m32, m33 + m.m33)
    }

    /** Operator function for Kotlin  */
    public open operator fun plus(m: Matrix4d): Matrix4d {
        return add(m)
    }

    public open fun sub(m: Matrix4d): Matrix4d {
        return Matrix4d(
            m00 - m.m00, m01 - m.m01, m02 - m.m02, m03 - m.m03,
            m10 - m.m10, m11 - m.m11, m12 - m.m12, m13 - m.m13,
            m20 - m.m20, m21 - m.m21, m22 - m.m22, m23 - m.m23,
            m30 - m.m30, m31 - m.m31, m32 - m.m32, m33 - m.m33)
    }

    /** Operator function for Kotlin  */
    public open operator fun minus(m: Matrix4d): Matrix4d {
        return sub(m)
    }

    public open fun mul(a: Float): Matrix4d {
        return mul(a.toDouble())
    }

    /** Operator function for Kotlin  */
    public open operator fun times(a: Float): Matrix4d {
        return mul(a)
    }

    public open fun mul(a: Double): Matrix4d {
        return Matrix4d(
            m00 * a, m01 * a, m02 * a, m03 * a,
            m10 * a, m11 * a, m12 * a, m13 * a,
            m20 * a, m21 * a, m22 * a, m23 * a,
            m30 * a, m31 * a, m32 * a, m33 * a)
    }

    /** Operator function for Kotlin  */
    public open operator fun times(a: Double): Matrix4d {
        return mul(a)
    }

    public open fun mul(m: Matrix4d): Matrix4d {
        return Matrix4d(
            m00 * m.m00 + m01 * m.m10 + m02 * m.m20 + m03 * m.m30,
            m00 * m.m01 + m01 * m.m11 + m02 * m.m21 + m03 * m.m31,
            m00 * m.m02 + m01 * m.m12 + m02 * m.m22 + m03 * m.m32,
            m00 * m.m03 + m01 * m.m13 + m02 * m.m23 + m03 * m.m33,
            m10 * m.m00 + m11 * m.m10 + m12 * m.m20 + m13 * m.m30,
            m10 * m.m01 + m11 * m.m11 + m12 * m.m21 + m13 * m.m31,
            m10 * m.m02 + m11 * m.m12 + m12 * m.m22 + m13 * m.m32,
            m10 * m.m03 + m11 * m.m13 + m12 * m.m23 + m13 * m.m33,
            m20 * m.m00 + m21 * m.m10 + m22 * m.m20 + m23 * m.m30,
            m20 * m.m01 + m21 * m.m11 + m22 * m.m21 + m23 * m.m31,
            m20 * m.m02 + m21 * m.m12 + m22 * m.m22 + m23 * m.m32,
            m20 * m.m03 + m21 * m.m13 + m22 * m.m23 + m23 * m.m33,
            m30 * m.m00 + m31 * m.m10 + m32 * m.m20 + m33 * m.m30,
            m30 * m.m01 + m31 * m.m11 + m32 * m.m21 + m33 * m.m31,
            m30 * m.m02 + m31 * m.m12 + m32 * m.m22 + m33 * m.m32,
            m30 * m.m03 + m31 * m.m13 + m32 * m.m23 + m33 * m.m33)
    }

    /** Operator function for Kotlin  */
    public open operator fun times(m: Matrix4d): Matrix4d {
        return mul(m)
    }

    public open operator fun div(a: Float): Matrix4d {
        return div(a.toDouble())
    }

    public open operator fun div(a: Double): Matrix4d {
        return Matrix4d(
            m00 / a, m01 / a, m02 / a, m03 / a,
            m10 / a, m11 / a, m12 / a, m13 / a,
            m20 / a, m21 / a, m22 / a, m23 / a,
            m30 / a, m31 / a, m32 / a, m33 / a)
    }

    public open operator fun div(m: Matrix4d): Matrix4d {
        return mul(m.invert())
    }

    public open fun pow(pow: Float): Matrix4d {
        return pow(pow.toDouble())
    }

    public open fun pow(pow: Double): Matrix4d {
        return Matrix4d(
            m00.pow(pow), m01.pow(pow), m02.pow(pow), m03.pow(pow),
            m10.pow(pow), m11.pow(pow), m12.pow(pow), m13.pow(pow),
            m20.pow(pow), m21.pow(pow), m22.pow(pow), m23.pow(pow),
            m30.pow(pow), m31.pow(pow), m32.pow(pow), m33.pow(pow))
    }

    public open fun translate(v: Vec3d): Matrix4d {
        return translate(v.x, v.y, v.z)
    }

    public open fun translate(x: Float, y: Float, z: Float): Matrix4d {
        return translate(x.toDouble(), y.toDouble(), z.toDouble())
    }

    public open fun translate(x: Double, y: Double, z: Double): Matrix4d {
        return this.mul(createTranslation(x, y, z))
    }

    public open fun scale(scale: Float): Matrix4d {
        return scale(scale.toDouble())
    }

    public open fun scale(scale: Double): Matrix4d {
        return scale(scale, scale, scale, scale)
    }

    public open fun scale(x: Float, y: Float, z: Float, w: Float): Matrix4d {
        return scale(x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())
    }

    public open fun scale(x: Double, y: Double, z: Double, w: Double): Matrix4d {
        return this.mul(createScaling(x, y, z, w))
    }

    public open fun rotate(rot: Quaternion): Matrix4d {
        return this.mul(createRotation(rot))
    }

    public open fun rotate(axis: Vec3d, angle: Double): Matrix4d {
        return this.mul(createRotation(axis, angle))
    }

    /**
     * Transforms the passed vector using this [augmented matrix](https://en.wikipedia.org/wiki/Affine_transformation#Augmented_matrix).
     */
    public fun transform(v: Vec3d): Vec3d {
        return transform(v.x, v.y, v.z)
    }

    /**
     * Transforms the passed vector using this [augmented matrix](https://en.wikipedia.org/wiki/Affine_transformation#Augmented_matrix).
     */
    public fun transform(x: Double, y: Double, z: Double): Vec3d {
        return vec(
            m00 * x + m01 * y + m02 * z + m03 * 1,
            m10 * x + m11 * y + m12 * z + m13 * 1,
            m20 * x + m21 * y + m22 * z + m23 * 1)
    }

    /**
     * Transforms the passed vector using this [augmented matrix](https://en.wikipedia.org/wiki/Affine_transformation#Augmented_matrix),
     * returning the X axis of the result. This method, along with [transformY] and [transformZ], allow applying
     * transforms without creating new [Vec3d] objects.
     */
    public fun transformX(x: Double, y: Double, z: Double): Double {
        return m00 * x + m01 * y + m02 * z + m03 * 1
    }

    /**
     * Transforms the passed vector using this [augmented matrix](https://en.wikipedia.org/wiki/Affine_transformation#Augmented_matrix),
     * returning the Y axis of the result. This method, along with [transformX] and [transformZ], allow applying
     * transforms without creating new [Vec3d] objects.
     */
    public fun transformY(x: Double, y: Double, z: Double): Double {
        return m10 * x + m11 * y + m12 * z + m13 * 1
    }

    /**
     * Transforms the passed vector using this [augmented matrix](https://en.wikipedia.org/wiki/Affine_transformation#Augmented_matrix),
     * returning the Z axis of the result. This method, along with [transformX] and [transformY], allow applying
     * transforms without creating new [Vec3d] objects.
     */
    public fun transformZ(x: Double, y: Double, z: Double): Double {
        return m20 * x + m21 * y + m22 * z + m23 * 1
    }

    /**
     * Transforms the passed delta vector, ignoring the translation component of this
     * [augmented matrix](https://en.wikipedia.org/wiki/Affine_transformation#Augmented_matrix).
     */
    public fun transformDelta(v: Vec3d): Vec3d {
        return transformDelta(v.x, v.y, v.z)
    }

    /**
     * Transforms the passed delta vector, ignoring the translation component of this
     * [augmented matrix](https://en.wikipedia.org/wiki/Affine_transformation#Augmented_matrix).
     */
    public fun transformDelta(x: Double, y: Double, z: Double): Vec3d {
        return vec(
            m00 * x + m01 * y + m02 * z + m03 * 0,
            m10 * x + m11 * y + m12 * z + m13 * 0,
            m20 * x + m21 * y + m22 * z + m23 * 0)
    }

    /**
     * Transforms the passed delta vector, ignoring the translation component of this
     * [augmented matrix](https://en.wikipedia.org/wiki/Affine_transformation#Augmented_matrix), returning the X axis
     * of the result. This method, along with [transformDeltaY] and [transformDeltaZ], allow applying transforms
     * without creating new [Vec3d] objects.
     */
    public fun transformDeltaX(x: Double, y: Double, z: Double): Double {
        return m00 * x + m01 * y + m02 * z + m03 * 0
    }

    /**
     * Transforms the passed delta vector, ignoring the translation component of this
     * [augmented matrix](https://en.wikipedia.org/wiki/Affine_transformation#Augmented_matrix), returning the Y axis
     * of the result. This method, along with [transformDeltaX] and [transformDeltaZ], allow applying transforms
     * without creating new [Vec3d] objects.
     */
    public fun transformDeltaY(x: Double, y: Double, z: Double): Double {
        return m10 * x + m11 * y + m12 * z + m13 * 0
    }

    /**
     * Transforms the passed delta vector, ignoring the translation component of this
     * [augmented matrix](https://en.wikipedia.org/wiki/Affine_transformation#Augmented_matrix), returning the Z axis
     * of the result. This method, along with [transformDeltaX] and [transformDeltaY], allow applying transforms
     * without creating new [Vec3d] objects.
     */
    public fun transformDeltaZ(x: Double, y: Double, z: Double): Double {
        return m20 * x + m21 * y + m22 * z + m23 * 0
    }

    public open fun floor(): Matrix4d {
        return Matrix4d(
            floor(m00), floor(m01), floor(m02), floor(m03),
            floor(m10), floor(m11), floor(m12), floor(m13),
            floor(m20), floor(m21), floor(m22), floor(m23),
            floor(m30), floor(m31), floor(m32), floor(m33))
    }

    public open fun ceil(): Matrix4d {
        return Matrix4d(
            ceil(m00), ceil(m01), ceil(m02), ceil(m03),
            ceil(m10), ceil(m11), ceil(m12), ceil(m13),
            ceil(m20), ceil(m21), ceil(m22), ceil(m23),
            ceil(m30), ceil(m31), ceil(m32), ceil(m33))
    }

    public open fun round(): Matrix4d {
        return Matrix4d(
            m00.roundToLong().toFloat(), m01.roundToLong().toFloat(), m02.roundToLong().toFloat(), m03.roundToLong().toFloat(),
            m10.roundToLong().toFloat(), m11.roundToLong().toFloat(), m12.roundToLong().toFloat(), m13.roundToLong().toFloat(),
            m20.roundToLong().toFloat(), m21.roundToLong().toFloat(), m22.roundToLong().toFloat(), m23.roundToLong().toFloat(),
            m30.roundToLong().toFloat(), m31.roundToLong().toFloat(), m32.roundToLong().toFloat(), m33.roundToLong().toFloat())
    }

    public open fun abs(): Matrix4d {
        return Matrix4d(
            abs(m00), abs(m01), abs(m02), abs(m03),
            abs(m10), abs(m11), abs(m12), abs(m13),
            abs(m20), abs(m21), abs(m22), abs(m23),
            abs(m30), abs(m31), abs(m32), abs(m33))
    }

    public open fun negate(): Matrix4d {
        return Matrix4d(
            -m00, -m01, -m02, -m03,
            -m10, -m11, -m12, -m13,
            -m20, -m21, -m22, -m23,
            -m30, -m31, -m32, -m33)
    }

    /** Operator function for Kotlin  */
    public open operator fun unaryMinus(): Matrix4d {
        return negate()
    }

    /** Transforms the vector using this matrix */
    @JvmSynthetic
    public operator fun times(v: Vec3d): Vec3d = transform(v)

    public open fun transpose(): Matrix4d {
        return Matrix4d(
            m00, m10, m20, m30,
            m01, m11, m21, m31,
            m02, m12, m22, m32,
            m03, m13, m23, m33)
    }

    public fun trace(): Double {
        return m00 + m11 + m22 + m33
    }

    public fun determinant(): Double {
        return m00 * (m11 * m22 * m33 + m21 * m32 * m13 + m31 * m12 * m23 - m31 * m22 * m13 - m11 * m32 * m23 - m21 * m12 * m33) - m10 * (m01 * m22 * m33 + m21 * m32 * m03 + m31 * m02 * m23 - m31 * m22 * m03 - m01 * m32 * m23 - m21 * m02 * m33) + m20 * (m01 * m12 * m33 + m11 * m32 * m03 + m31 * m02 * m13 - m31 * m12 * m03 - m01 * m32 * m13 - m11 * m02 * m33) - m30 * (m01 * m12 * m23 + m11 * m22 * m03 + m21 * m02 * m13 - m21 * m12 * m03 - m01 * m22 * m13 - m11 * m02 * m23)
    }

    public open fun invert(): Matrix4d {
        val det = determinant()
        if (abs(det) < DBL_EPSILON) {
            throw ArithmeticException("Cannot inverse a matrix with a zero determinant")
        }
        return Matrix4d(
            det3(m11, m21, m31, m12, m22, m32, m13, m23, m33) / det, -det3(m01, m21, m31, m02, m22, m32, m03, m23, m33) / det,
            det3(m01, m11, m31, m02, m12, m32, m03, m13, m33) / det, -det3(m01, m11, m21, m02, m12, m22, m03, m13, m23) / det,
            -det3(m10, m20, m30, m12, m22, m32, m13, m23, m33) / det, det3(m00, m20, m30, m02, m22, m32, m03, m23, m33) / det,
            -det3(m00, m10, m30, m02, m12, m32, m03, m13, m33) / det, det3(m00, m10, m20, m02, m12, m22, m03, m13, m23) / det,
            det3(m10, m20, m30, m11, m21, m31, m13, m23, m33) / det, -det3(m00, m20, m30, m01, m21, m31, m03, m23, m33) / det,
            det3(m00, m10, m30, m01, m11, m31, m03, m13, m33) / det, -det3(m00, m10, m20, m01, m11, m21, m03, m13, m23) / det,
            -det3(m10, m20, m30, m11, m21, m31, m12, m22, m32) / det, det3(m00, m20, m30, m01, m21, m31, m02, m22, m32) / det,
            -det3(m00, m10, m30, m01, m11, m31, m02, m12, m32) / det, det3(m00, m10, m20, m01, m11, m21, m02, m12, m22) / det)
    }

    public fun toArray(): DoubleArray {
        return toArray(false)
    }

    public fun toArray(columnMajor: Boolean): DoubleArray {
        return if (columnMajor) {
            doubleArrayOf(m00, m10, m20, m30, m01, m11, m21, m31, m02, m12, m22, m32, m03, m13, m23, m33)
        } else {
            doubleArrayOf(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33)
        }
    }

    public fun toMatrix4f(): Matrix4f {
        val matrix = Matrix4f()
        copyToMatrix4f(matrix)
        return matrix
    }

    /**
     * Copies the contents of this LibrarianLib matrix into the provided Minecraft matrix
     */
    public fun copyToMatrix4f(matrix: Matrix4f) {
        val m: IMatrix4f = mixinCast(matrix)
        m.m00 = m00.toFloat()
        m.m01 = m01.toFloat()
        m.m02 = m02.toFloat()
        m.m03 = m03.toFloat()
        m.m10 = m10.toFloat()
        m.m11 = m11.toFloat()
        m.m12 = m12.toFloat()
        m.m13 = m13.toFloat()
        m.m20 = m20.toFloat()
        m.m21 = m21.toFloat()
        m.m22 = m22.toFloat()
        m.m23 = m23.toFloat()
        m.m30 = m30.toFloat()
        m.m31 = m31.toFloat()
        m.m32 = m32.toFloat()
        m.m33 = m33.toFloat()
    }

    override fun toString(): String {
        return (m00.toString() + " " + m01 + " " + m02 + " " + m03 + "\n"
            + m10 + " " + m11 + " " + m12 + " " + m13 + "\n"
            + m20 + " " + m21 + " " + m22 + " " + m23 + "\n"
            + m30 + " " + m31 + " " + m32 + " " + m33 + "\n")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Matrix4d) return false
        return this.m00 == other.m00 && this.m01 == other.m01 && this.m02 == other.m02 && this.m03 == other.m03 &&
            this.m10 == other.m10 && this.m11 == other.m11 && this.m12 == other.m12 && this.m13 == other.m13 &&
            this.m20 == other.m20 && this.m21 == other.m21 && this.m22 == other.m22 && this.m23 == other.m23 &&
            this.m30 == other.m30 && this.m31 == other.m31 && this.m32 == other.m32 && this.m33 == other.m33
    }

    override fun hashCode(): Int {
        var result = m00.hashCode()
        result = 31 * result + m01.hashCode()
        result = 31 * result + m02.hashCode()
        result = 31 * result + m03.hashCode()
        result = 31 * result + m10.hashCode()
        result = 31 * result + m11.hashCode()
        result = 31 * result + m12.hashCode()
        result = 31 * result + m13.hashCode()
        result = 31 * result + m20.hashCode()
        result = 31 * result + m21.hashCode()
        result = 31 * result + m22.hashCode()
        result = 31 * result + m23.hashCode()
        result = 31 * result + m30.hashCode()
        result = 31 * result + m31.hashCode()
        result = 31 * result + m32.hashCode()
        result = 31 * result + m33.hashCode()
        return result
    }

    override fun clone(): Matrix4d {
        return Matrix4d(this)
    }

    public fun toMutable(): MutableMatrix4d = MutableMatrix4d(this)
    public open fun toImmutable(): Matrix4d = this

    public companion object {
        private val DBL_EPSILON = Double.fromBits(0x3cb0000000000000L)
        public val ZERO: Matrix4d = Matrix4d(
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f)
        public val IDENTITY: Matrix4d = Matrix4d()

        /**
         * A thread local mutable matrix, useful for an no allocation intermediate for various operations
         */
        public val temporaryMatrix: MutableMatrix4d by threadLocal { MutableMatrix4d() }

        internal fun createScaling(x: Double, y: Double, z: Double, w: Double): MutableMatrix4d {
            return temporaryMatrix.set(
                x, 0.0, 0.0, 0.0,
                0.0, y, 0.0, 0.0,
                0.0, 0.0, z, 0.0,
                0.0, 0.0, 0.0, w)
        }

        internal fun createTranslation(x: Double, y: Double, z: Double): MutableMatrix4d {
            return temporaryMatrix.set(
                1.0, 0.0, 0.0, x,
                0.0, 1.0, 0.0, y,
                0.0, 0.0, 1.0, z,
                0.0, 0.0, 0.0, 1.0)
        }

        @Suppress("NAME_SHADOWING")
        internal fun createRotation(rot: Quaternion): MutableMatrix4d {
            var rot = rot
            rot = rot.normalize()
            return temporaryMatrix.set(
                1 - 2 * rot.y * rot.y - 2 * rot.z * rot.z,
                2 * rot.x * rot.y - 2 * rot.w * rot.z,
                2 * rot.x * rot.z + 2 * rot.w * rot.y, 0.0,
                2 * rot.x * rot.y + 2 * rot.w * rot.z,
                1 - 2 * rot.x * rot.x - 2 * rot.z * rot.z,
                2 * rot.y * rot.z - 2 * rot.w * rot.x, 0.0,
                2 * rot.x * rot.z - 2 * rot.w * rot.y,
                2 * rot.y * rot.z + 2 * rot.x * rot.w,
                1 - 2 * rot.x * rot.x - 2 * rot.y * rot.y, 0.0,
                0.0, 0.0, 0.0, 1.0)
        }

        internal fun createRotation(axis: Vec3d, angle: Double): MutableMatrix4d {
            // https://en.wikipedia.org/wiki/Rotation_matrix#Conversion_from_and_to_axis%E2%80%93angle
            val len = axis.length()
            val x = axis.x / len
            val y = axis.y / len
            val z = axis.z / len
            val cos = cos(angle)
            val sin = sin(angle)

            return temporaryMatrix.set(
                cos + x * x * (1 - cos), x * y * (1 - cos) - z * sin, x * z * (1 - cos) + y * sin, 0.0,
                y * x * (1 - cos) + z * sin, cos + y * y * (1 - cos), y * z * (1 - cos) - x * sin, 0.0,
                z * x * (1 - cos) - y * sin, z * y * (1 - cos) + x * sin, cos + z * z * (1 - cos), 0.0,
                0.0, 0.0, 0.0, 1.0
            )
        }

        /**
         * Creates a "look at" matrix for the given eye point.
         *
         * @param eye The position of the camera
         * @param at The point that the camera is looking at
         * @param up The "up" vector
         * @return A rotational transform that corresponds to a camera looking at the given point
         */
        public fun createLookAt(eye: Vec3d, at: Vec3d, up: Vec3d): Matrix4d {
            val f = (at - eye).normalize()
            val s = (f cross up).normalize()
            val u = s cross f
            val mat = Matrix4d(
                s.x, s.y, s.z, 0.0,
                u.x, u.y, u.z, 0.0,
                -f.x, -f.y, -f.z, 0.0,
                0.0, 0.0, 0.0, 1.0)
            return mat.translate(-eye)
        }

        /**
         * Creates a perspective projection matrix with the given (x) FOV, aspect, near and far planes
         *
         * @param fov The field of view in the x direction
         * @param aspect The aspect ratio, usually width/height
         * @param near The near plane, cannot be 0
         * @param far the far plane, far cannot equal near
         * @return A perspective projection matrix built from the given values
         */
        public fun createPerspective(fov: Float, aspect: Float, near: Float, far: Float): Matrix4d {
            return createPerspective(fov.toDouble(), aspect.toDouble(), near.toDouble(), far.toDouble())
        }

        /**
         * Creates a perspective projection matrix with the given (x) FOV, aspect, near and far planes
         *
         * @param fov The field of view in the x direction
         * @param aspect The aspect ratio, usually width/height
         * @param near The near plane, cannot be 0
         * @param far the far plane, far cannot equal near
         * @return A perspective projection matrix built from the given values
         */
        public fun createPerspective(fov: Double, aspect: Double, near: Double, far: Double): Matrix4d {
            val scale = 1 / tan(fov * (PI / 360))
            return Matrix4d(
                scale / aspect, 0.0, 0.0, 0.0,
                0.0, scale, 0.0, 0.0,
                0.0, 0.0, (far + near) / (near - far), 2.0 * far * near / (near - far),
                0.0, 0.0, -1.0, 0.0)
        }

        /**
         * Creates an orthographic viewing frustum built from the provided values
         *
         * @param right the right most plane of the viewing frustum
         * @param left the left most plane of the viewing frustum
         * @param top the top plane of the viewing frustum
         * @param bottom the bottom plane of the viewing frustum
         * @param near the near plane of the viewing frustum
         * @param far the far plane of the viewing frustum
         * @return A viewing frustum built from the provided values
         */
        public fun createOrthographic(right: Float, left: Float, top: Float, bottom: Float,
            near: Float, far: Float): Matrix4d {
            return createOrthographic(right.toDouble(), left.toDouble(), top.toDouble(), bottom.toDouble(), near.toDouble(), far.toDouble())
        }

        /**
         * Creates an orthographic viewing frustum built from the provided values
         *
         * @param right the right most plane of the viewing frustum
         * @param left the left most plane of the viewing frustum
         * @param top the top plane of the viewing frustum
         * @param bottom the bottom plane of the viewing frustum
         * @param near the near plane of the viewing frustum
         * @param far the far plane of the viewing frustum
         * @return A viewing frustum built from the provided values
         */
        public fun createOrthographic(right: Double, left: Double, top: Double, bottom: Double,
            near: Double, far: Double): Matrix4d {
            return Matrix4d(
                2 / (right - left), 0.0, 0.0, -(right + left) / (right - left),
                0.0, 2 / (top - bottom), 0.0, -(top + bottom) / (top - bottom),
                0.0, 0.0, -2 / (far - near), -(far + near) / (far - near),
                0.0, 0.0, 0.0, 1.0)
        }

        internal fun det3(m00: Double, m01: Double, m02: Double,
            m10: Double, m11: Double, m12: Double,
            m20: Double, m21: Double, m22: Double): Double {
            return m00 * (m11 * m22 - m12 * m21) - m01 * (m10 * m22 - m12 * m20) + m02 * (m10 * m21 - m11 * m20)
        }
    }
}

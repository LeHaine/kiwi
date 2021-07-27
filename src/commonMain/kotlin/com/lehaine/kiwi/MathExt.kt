package com.lehaine.kiwi

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

fun ClosedFloatingPointRange<Double>.random() = Random.nextDouble(start, endInclusive)
fun ClosedFloatingPointRange<Float>.random() = Random.nextDouble(start.toDouble(), endInclusive.toDouble()).toFloat()
fun IntRange.randomd() = Random.nextDouble(start.toDouble(), endInclusive.toDouble())

fun sparseListOf(vararg ranges: IntRange): List<Int> = ranges.flatMap { it }

fun distSqr(ax: Double, ay: Double, bx: Double, by: Double) = (ax - bx) * (ax - bx) + (ay - by) * (ay - by)
fun distSqr(ax: Int, ay: Int, bx: Int, by: Int) = distSqr(ax.toDouble(), ay.toDouble(), bx.toDouble(), by.toDouble())
fun dist(ax: Double, ay: Double, bx: Double, by: Double) = sqrt(distSqr(ax, ay, bx, by))
fun dist(ax: Int, ay: Int, bx: Int, by: Int) = dist(ax.toDouble(), ay.toDouble(), bx.toDouble(), by.toDouble())

fun distRadians(a: Double, b: Double): Double = abs(subtractRadians(a, b))
fun distRadians(a: Int, b: Int): Double = distRadians(a.toDouble(), b.toDouble())

fun subtractRadians(a: Double, b: Double): Double = normalizeRadian(normalizeRadian(a) - normalizeRadian(b))

fun normalizeRadian(a: Double): Double {
    var result = a
    while (result < -PI) {
        result += PI * 2
    }
    while (result > PI) {
        result -= PI * 2
    }

    return result
}

package eu.imeon.invaders.core.util

import kotlin.math.pow

typealias MappingLambda = (x: Double) -> Double


class SmoothCurveGenerator(
    private val alpha: Double,
    private val beta: Double,
    private val t3: Double
) {

    private val t1 = alpha * t3
    private val t2 = (1 - beta) * t3
    private val a = 1 / t1
    private val sMax = s2(t3)
    private fun s0(t: Double): Double = .5 * a * t.pow(2.0)
    private fun s1(t: Double): Double = a * t1 * t - .5 * a * t1.pow(2.0)
    private fun s2(t: Double): Double =
        -.5 * a * t.pow(2.0) + a * (t1 + t2) * t - .5 * a * (t1.pow(2.0) + t2.pow(2.0))

    private fun calcRaw(t: Double): Double =
        when {
            t < 0 -> 0.0
            t >= 0 && t < t1 -> s0(t)
            t >= t1 && t < t2 -> s1(t)
            t >= t2 && t < t3 -> s2(t)
            else -> s2(t3)
        }

    private fun calc(t:Double):Double = calcRaw(t)/sMax
    fun calc(t:Float):Float = calc(t.toDouble()).toFloat()
}

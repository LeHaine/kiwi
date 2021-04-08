package com.lehaine.kiwi.component

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow

interface DynamicComponent : GridPositionComponent {
    var gravityX: Double
    var gravityY: Double
    var gravityMultiplier: Double
    var velocityX: Double
    var velocityY: Double
    var frictionX: Double
    var frictionY: Double
    var maxGridMovementPercent: Double


    override fun updateGridPosition(tmod: Double) {
        velocityX += calculateDeltaXGravity(tmod)
        velocityY += calculateDeltaYGravity(tmod)

        /**
         * Any movement greater than [maxGridMovementPercent] will increase the number of steps here.
         * The steps will break down the movement into smaller iterators to avoid jumping over grid collisions
         */
        var steps = ceil(abs(velocityX * tmod) + abs(velocityY * tmod) / maxGridMovementPercent)
        if (steps > 0) {
            val stepX = velocityX * tmod / steps
            val stepY = velocityY * tmod / steps
            while (steps > 0) {
                xr += stepX
                checkXCollision(tmod)

                while (xr > 1) {
                    xr--
                    cx++
                }
                while (xr < 0) {
                    xr++
                    cx--
                }

                yr += stepY
                checkYCollision(tmod)

                while (yr > 1) {
                    yr--
                    cy++
                }

                while (yr < 0) {
                    yr++
                    cy--
                }

                steps--
            }
        }
        velocityX *= frictionX.pow(tmod)
        if (abs(velocityX) <= 0.0005 * tmod) {
            velocityX = 0.0
        }

        velocityY *= frictionY.pow(tmod)
        if (abs(velocityY) <= 0.0005 * tmod) {
            velocityY = 0.0
        }
    }


    fun calculateDeltaXGravity(tmod: Double): Double {
        return 0.0
    }

    fun calculateDeltaYGravity(tmod: Double): Double {
        return 0.0
    }

    fun checkXCollision(tmod: Double) {}
    fun checkYCollision(tmod: Double) {}

    companion object {
        operator fun invoke(): DynamicComponent {
            return DynamicComponentDefault()
        }
    }
}

open class DynamicComponentDefault(
    override var cx: Int = 0,
    override var cy: Int = 0,
    override var xr: Double = 0.5,
    override var yr: Double = 0.5,
    override var anchorX: Double = 0.5,
    override var anchorY: Double = 0.5,
    override var gridCellSize: Int = 16
) : DynamicComponent {
    override var gravityX: Double = 0.0
    override var gravityY: Double = 0.0
    override var gravityMultiplier: Double = 1.0
    override var velocityX: Double = 0.0
    override var velocityY: Double = 0.0
    override var frictionX: Double = 0.82
    override var frictionY: Double = 0.82
    override var maxGridMovementPercent: Double = 0.33

    override var width: Double = 16.0
    override var height: Double = 16.0
}
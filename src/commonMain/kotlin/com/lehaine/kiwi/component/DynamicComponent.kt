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

    override fun updateX(tmod: Double) {
        var steps = ceil(abs(velocityX * tmod))
        val step = velocityX * tmod / steps
        while (steps > 0) {
            xr += step

            checkXCollision(tmod)
            super.updateX(tmod)
            steps--
        }

        velocityX *= frictionX.pow(tmod)
        if (abs(velocityX) <= 0.0005 * tmod) {
            velocityX = 0.0
        }
    }

    override fun updateY(tmod: Double) {
        velocityY += calculateDeltaYGravity(tmod)
        var steps = ceil(abs(velocityY * tmod))
        val step = velocityY * tmod / steps
        while (steps > 0) {
            yr += step
            checkYCollision(tmod)
            super.updateY(tmod)
            steps--
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

    override var width: Double = 16.0
    override var height: Double = 16.0
}
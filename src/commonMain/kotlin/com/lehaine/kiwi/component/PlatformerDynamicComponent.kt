package com.lehaine.kiwi.component

import kotlin.math.floor
import kotlin.math.pow

interface PlatformerDynamicComponent : DynamicComponent {
    val onGround: Boolean
    var hasGravity: Boolean
}

class PlatformerDynamicComponentDefault(
    private val levelComponent: LevelComponent<*>,
    override var cx: Int = 0,
    override var cy: Int = 0,
    override var xr: Double = 0.5,
    override var yr: Double = 1.0,
    override var anchorX: Double = 0.5,
    override var anchorY: Double = 1.0,
    override var gridCellSize: Int = 16
) : PlatformerDynamicComponent {
    override var gravityX: Double = 0.0
    override var gravityY: Double = 0.028
    override var gravityMultiplier: Double = 1.0
    override var velocityX: Double = 0.0
    override var velocityY: Double = 0.0
    override var frictionX: Double = 0.82
    override var frictionY: Double = 0.82

    override var width: Double = 16.0
    override var height: Double = 16.0

    override var hasGravity = true
    override val onGround get() = velocityY == 0.0 && levelComponent.hasCollision(cx, cy + 1)

    private val gravityPulling get() = !onGround && hasGravity

    override fun checkXCollision(tmod: Double) {
        if (levelComponent.hasCollision(cx + 1, cy) && xr >= 0.7) {
            xr = 0.7
            velocityX *= 0.5.pow(tmod)
        }

        if (levelComponent.hasCollision(cx - 1, cy) && xr <= 0.3) {
            xr = 0.3
            velocityX *= 0.5.pow(tmod)
        }
    }

    override fun checkYCollision(tmod: Double) {
        val heightCoordDiff = floor(height / gridCellSize.toDouble())
        if (levelComponent.hasCollision(cx, cy - 1) && yr <= heightCoordDiff) {
            yr = heightCoordDiff
            velocityY = 0.0
        }
        if (levelComponent.hasCollision(cx, cy + 1) && yr >= 1) {
            velocityY = 0.0
            yr = 1.0
        }
    }

    override fun calculateDeltaYGravity(tmod: Double): Double {
        return if (gravityPulling) {
            gravityMultiplier * gravityY * tmod
        } else {
            0.0
        }
    }
}
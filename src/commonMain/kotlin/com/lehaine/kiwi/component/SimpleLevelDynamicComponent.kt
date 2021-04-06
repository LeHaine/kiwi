package com.lehaine.kiwi.component

import kotlin.math.floor
import kotlin.math.pow

class SimpleLevelDynamicComponent(
    val levelComponent: LevelComponent<*>,
    cx: Int = 0,
    cy: Int = 0,
    xr: Double = 0.5,
    yr: Double = 0.5,
    anchorX: Double = 0.5,
    anchorY: Double = 0.5,
    gridCellSize: Int = 16,
    var rightCollisionRatio: Double = 0.7,
    var leftCollisionRatio: Double = 0.3,
    var bottomCollisionRatio: Double = 1.0,
    var topCollisionRatio: Double = 1.0,
    var useTopCollisionRatio: Boolean = false
) : DynamicComponentDefault(cx, cy, xr, yr, anchorX, anchorY, gridCellSize) {

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
        val heightCoordDiff = if (useTopCollisionRatio) topCollisionRatio else floor(height / gridCellSize.toDouble())
        if (levelComponent.hasCollision(cx, cy - 1) && yr <= heightCoordDiff) {
            yr = heightCoordDiff
            velocityY = 0.0
        }
        if (levelComponent.hasCollision(cx, cy + 1) && yr >= 1) {
            velocityY = 0.0
            yr = 1.0
        }
    }
}
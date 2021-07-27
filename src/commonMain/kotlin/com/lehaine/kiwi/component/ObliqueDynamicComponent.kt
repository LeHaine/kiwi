package com.lehaine.kiwi.component

import com.soywiz.korge.debug.uiCollapsibleSection
import com.soywiz.korge.debug.uiEditableValue
import com.soywiz.korui.UiContainer
import kotlin.math.abs
import kotlin.math.floor

/**
 * @author Colton Daily
 * @date 7/16/2021
 */
interface ObliqueDynamicComponent : LevelDynamicComponent {
    var wallSlideDelta: Double
    var wallSlideTolerance: Double
    var wallDeltaRightCollisionRatio: Double
    var wallDeltaLeftCollisionRatio: Double
    var wallDeltaBottomCollisionRatio: Double
    var wallDeltaTopCollisionRatio: Double
    var frictionZ: Double
    var velocityZ: Double
    var zr: Double
    var gravity: Double
    var hasGravity: Boolean
    var onLand: (() -> Unit)?
    val onGround: Boolean get() = zr == 0.0

    override fun buildDebugInfo(container: UiContainer) {
        super.buildDebugInfo(container)

        container.uiCollapsibleSection("Oblique Dynamic Component") {
            uiEditableValue(
                this@ObliqueDynamicComponent::wallSlideDelta,
                name = "wall slide delta",
            )
            uiEditableValue(
                this@ObliqueDynamicComponent::wallSlideTolerance,
                name = "wall slide tolerance",
            )
        }
    }
}

class ObliqueDynamicComponentDefault(
    private val levelComponent: LevelComponent<*>,
    override var cx: Int = 0,
    override var cy: Int = 0,
    override var xr: Double = 0.5,
    override var yr: Double = 1.0,
    override var anchorX: Double = 0.5,
    override var anchorY: Double = 1.0,
    override var gridCellSize: Int = 16,
    override var rightCollisionRatio: Double = 0.8,
    override var leftCollisionRatio: Double = 0.2,
    override var bottomCollisionRatio: Double = 1.0,
    override var topCollisionRatio: Double = 0.3,
    override var useTopCollisionRatio: Boolean = true,
    override var onLevelCollision: ((xDir: Int, yDir: Int) -> Unit)? = null
) : ObliqueDynamicComponent {
    override var preXCheck: (() -> Unit)? = null
    override var preYCheck: (() -> Unit)? = null
    override var gravityX: Double = 0.0
    override var gravityY: Double = 0.05
    override var gravityMultiplier: Double = 1.0
    override var velocityX: Double = 0.0
    override var velocityY: Double = 0.0
    override var frictionX: Double = 0.82
    override var frictionY: Double = 0.82
    override var frictionZ: Double = 0.82
    override var maxGridMovementPercent: Double = 0.33
    override var interpolatePixelPosition: Boolean = true
    override var lastPx: Double = 0.0
    override var lastPy: Double = 0.0
    override var fixedProgressionRatio: Double = 1.0
    override var wallSlideDelta: Double = 0.005
    override var wallSlideTolerance: Double = 0.015
    override var wallDeltaRightCollisionRatio: Double = 0.5
    override var wallDeltaLeftCollisionRatio: Double = 0.5
    override var wallDeltaBottomCollisionRatio: Double = 0.6
    override var wallDeltaTopCollisionRatio: Double = 0.6

    override var onLand: (() -> Unit)? = null
    override var velocityZ: Double = 0.0
    override var zr: Double = 0.0
    override var gravity: Double = 0.05
    override var hasGravity: Boolean = true
    override var width: Double = 16.0
    override var height: Double = 16.0
    override val attachY: Double
        get() = (cy + yr - zr) * gridCellSize

    override fun checkXCollision() {
        if (levelComponent.hasCollision(cx + 1, cy) && xr >= rightCollisionRatio) {
            xr = rightCollisionRatio
            velocityX *= 0.5

            // check if player is stuck on wall / corner and help them by nudging them off it
            if (shouldNudge(yr, wallDeltaTopCollisionRatio, 1, -1, velocityY, true)) {
                velocityY -= wallSlideDelta // todo fix this calculation
            }
            if (shouldNudge(yr, wallDeltaBottomCollisionRatio, 1, 1, velocityY, false)) {
                velocityY += wallSlideDelta
            }

            onLevelCollision?.invoke(1, 0)
        }

        if (levelComponent.hasCollision(cx - 1, cy) && xr <= leftCollisionRatio) {
            xr = leftCollisionRatio
            velocityX *= 0.5

            // check if player is stuck on wall / corner and help them by nudging them off it
            if (shouldNudge(yr, wallDeltaTopCollisionRatio, -1, -1, velocityY, true)) {
                velocityY -= wallSlideDelta // todo fix this calculation
            }
            if (shouldNudge(yr, wallDeltaBottomCollisionRatio, -1, 1, velocityY, false)) {
                velocityY += wallSlideDelta
            }

            onLevelCollision?.invoke(-1, 0)
        }
    }

    override fun checkYCollision() {
        val heightCoordDiff = if (useTopCollisionRatio) topCollisionRatio else floor(height / gridCellSize.toDouble())
        if (levelComponent.hasCollision(cx, cy - 1) && yr <= heightCoordDiff) {
            yr = heightCoordDiff
            velocityY *= 0.5

            // check if player is stuck on wall / corner and help them by nudging them off it
            if (shouldNudge(xr, wallDeltaLeftCollisionRatio, -1, 1, velocityX, true)) {
                velocityX -= wallSlideDelta // todo fix this calculation
            }
            if (shouldNudge(xr, wallDeltaLeftCollisionRatio, 1, 1, velocityX, false)) {
                velocityX += wallSlideDelta
            }

            onLevelCollision?.invoke(0, -1)
        }
        if (levelComponent.hasCollision(cx, cy + 1) && yr >= bottomCollisionRatio) {
            velocityY *= 0.5
            yr = bottomCollisionRatio

            // check if player is stuck on wall / corner and help them by nudging them off it
            if (shouldNudge(xr, wallDeltaLeftCollisionRatio, -1, -1, velocityX, true)) {
                velocityX -= wallSlideDelta // todo fix this calculation
            }
            if (shouldNudge(xr, wallDeltaLeftCollisionRatio, 1, -1, velocityX, false)) {
                velocityX += wallSlideDelta
            }

            onLevelCollision?.invoke(0, 1)
        }
    }

    override fun updateGridPosition() {
        super.updateGridPosition()
        zr += velocityZ

        if (zr > 0 && hasGravity) {
            velocityZ -= gravity
        }

        if (zr < 0) {
            zr = 0.0
            velocityZ = -velocityZ * 0.9
            if (abs(velocityZ) <= 0.06) {
                velocityZ = 0.0
            }
            onLand?.invoke()
        }


        velocityZ *= frictionZ
        if (abs(velocityZ) <= 0.0005) {
            velocityZ = 0.0
        }
    }

    private fun shouldNudge(
        gridRatio: Double,
        collisionRatio1: Double,
        xDir: Int,
        yDir: Int,
        velocity: Double,
        lowerThanTolerance: Boolean
    ): Boolean {
        return gridRatio < collisionRatio1 && !levelComponent.hasCollision(
            cx + xDir,
            cy + yDir
        ) && ((lowerThanTolerance && velocity <= wallSlideTolerance) || (!lowerThanTolerance && velocity >= wallSlideTolerance))
    }

    override fun calculateDeltaYGravity(): Double {
        return 0.0
    }
}
package com.lehaine.kiwi.component

import com.soywiz.korge.debug.uiCollapsibleSection
import com.soywiz.korge.debug.uiEditableValue
import com.soywiz.korui.UiContainer
import kotlin.math.floor

interface LevelDynamicComponent : DynamicComponent {
    var rightCollisionRatio: Double
    var leftCollisionRatio: Double
    var bottomCollisionRatio: Double
    var topCollisionRatio: Double
    var useTopCollisionRatio: Boolean
    var onLevelCollision: ((xDir: Int, yDir: Int) -> Unit)?

    override fun buildDebugInfo(container: UiContainer) {
        super.buildDebugInfo(container)

        container.uiCollapsibleSection("Level Dynamic Component") {
            uiEditableValue(
                this@LevelDynamicComponent::leftCollisionRatio,
                name = "Left Collision Ratio",
                min = 0.0,
                max = 1.0
            )
            uiEditableValue(
                this@LevelDynamicComponent::rightCollisionRatio,
                name = "Right Collision Ratio",
                min = 0.0,
                max = 1.0
            )
            uiEditableValue(
                this@LevelDynamicComponent::useTopCollisionRatio,
                name = "Use Top Collision Ratio"
            )
            uiEditableValue(
                this@LevelDynamicComponent::topCollisionRatio,
                name = "Top Collision Ratio",
                min = 0.0,
                max = 1.0
            )
            uiEditableValue(
                this@LevelDynamicComponent::bottomCollisionRatio,
                name = "Bottom Collision Ratio",
                min = 0.0,
                max = 1.0
            )
        }
    }
}

open class LevelDynamicComponentDefault(
    private val levelComponent: LevelComponent<*>,
    override var cx: Int = 0,
    override var cy: Int = 0,
    override var xr: Double = 0.5,
    override var yr: Double = 0.5,
    override var anchorX: Double = 0.5,
    override var anchorY: Double = 0.5,
    override var gridCellSize: Int = 16,
    override var rightCollisionRatio: Double = 0.7,
    override var leftCollisionRatio: Double = 0.3,
    override var bottomCollisionRatio: Double = 1.0,
    override var topCollisionRatio: Double = 1.0,
    override var useTopCollisionRatio: Boolean = false,
    override var onLevelCollision: ((xDir: Int, yDir: Int) -> Unit)? = null
) : LevelDynamicComponent {
    override var preXCheck: (() -> Unit)? = null
    override var preYCheck: (() -> Unit)? = null
    override var gravityX: Double = 0.0
    override var gravityY: Double = 0.0
    override var gravityMultiplier: Double = 1.0
    override var velocityX: Double = 0.0
    override var velocityY: Double = 0.0
    override var frictionX: Double = 0.82
    override var frictionY: Double = 0.82
    override var maxGridMovementPercent: Double = 0.33
    override var interpolatePixelPosition: Boolean = true
    override var lastPx: Double = 0.0
    override var lastPy: Double = 0.0
    override var fixedProgressionRatio: Double = 1.0

    override var width: Double = 16.0
    override var height: Double = 16.0

    override fun checkXCollision() {
        if (levelComponent.hasCollision(cx + 1, cy) && xr >= rightCollisionRatio) {
            xr = rightCollisionRatio
            velocityX *= 0.5
            onLevelCollision?.invoke(1, 0)
        }

        if (levelComponent.hasCollision(cx - 1, cy) && xr <= leftCollisionRatio) {
            xr = leftCollisionRatio
            velocityX *= 0.5
            onLevelCollision?.invoke(-1, 0)
        }
    }

    override fun checkYCollision() {
        val heightCoordDiff = if (useTopCollisionRatio) topCollisionRatio else floor(height / gridCellSize.toDouble())
        if (levelComponent.hasCollision(cx, cy - 1) && yr <= heightCoordDiff) {
            yr = heightCoordDiff
            velocityY = 0.0
            onLevelCollision?.invoke(0, -1)
        }
        if (levelComponent.hasCollision(cx, cy + 1) && yr >= bottomCollisionRatio) {
            velocityY = 0.0
            yr = bottomCollisionRatio
            onLevelCollision?.invoke(0, 1)
        }
    }
}
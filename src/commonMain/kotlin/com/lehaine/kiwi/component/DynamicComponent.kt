package com.lehaine.kiwi.component

import com.soywiz.klock.TimeSpan
import com.soywiz.korge.debug.uiCollapsibleSection
import com.soywiz.korge.debug.uiEditableValue
import com.soywiz.korui.UiContainer
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


    override fun updateGridPosition(dt: TimeSpan) {
        velocityX += calculateDeltaXGravity(dt)
        velocityY += calculateDeltaYGravity(dt)

        /**
         * Any movement greater than [maxGridMovementPercent] will increase the number of steps here.
         * The steps will break down the movement into smaller iterators to avoid jumping over grid collisions
         */
        var steps = ceil(abs(velocityX * dt.seconds) + abs(velocityY * dt.seconds) / maxGridMovementPercent)
        if (steps > 0) {
            val stepX = velocityX * dt.seconds / steps
            val stepY = velocityY * dt.seconds / steps
            while (steps > 0) {
                xr += stepX

                preXCheck?.invoke()
                checkXCollision(dt)

                while (xr > 1) {
                    xr--
                    cx++
                }
                while (xr < 0) {
                    xr++
                    cx--
                }

                yr += stepY
                preYCheck?.invoke()
                checkYCollision(dt)

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
        velocityX *= frictionX.pow(dt.seconds)
        if (abs(velocityX) <= 0.0005 * dt.seconds) {
            velocityX = 0.0
        }

        velocityY *= frictionY.pow(dt.seconds)
        if (abs(velocityY) <= 0.0005 * dt.seconds) {
            velocityY = 0.0
        }
    }


    fun calculateDeltaXGravity(dt: TimeSpan): Double {
        return 0.0
    }

    fun calculateDeltaYGravity(dt: TimeSpan): Double {
        return 0.0
    }

    fun checkXCollision(dt: TimeSpan) {}
    fun checkYCollision(dt: TimeSpan) {}


    override fun buildDebugInfo(container: UiContainer) {
        super.buildDebugInfo(container)

        container.uiCollapsibleSection("Dynamic Component") {
            uiEditableValue(
                this@DynamicComponent::gravityX,
                name = "Gravity X",
            )
            uiEditableValue(
                this@DynamicComponent::gravityY,
                name = "Gravity Y"
            )
            uiEditableValue(
                this@DynamicComponent::gravityMultiplier,
                name = "Gravity Multiplier"
            )
            uiEditableValue(
                this@DynamicComponent::velocityX,
                name = "Velocity X"
            )
            uiEditableValue(
                this@DynamicComponent::velocityY,
                name = "Velocity Y"
            )
            uiEditableValue(
                this@DynamicComponent::frictionX,
                name = "Friction X"
            )
            uiEditableValue(
                this@DynamicComponent::frictionY,
                name = "Friction Y"
            )
            uiEditableValue(
                this@DynamicComponent::maxGridMovementPercent,
                name = "Max Grid Movement"
            )
        }
    }

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

    override var width: Double = 16.0
    override var height: Double = 16.0
}
package com.lehaine.kiwi.component

import com.soywiz.korge.debug.uiCollapsibleSection
import com.soywiz.korge.debug.uiEditableValue
import com.soywiz.korui.UiContainer
import kotlin.math.max
import kotlin.math.min

interface GridPositionComponent : Component {
    var cx: Int
    var cy: Int
    var xr: Double
    var yr: Double

    var gridCellSize: Int
    var width: Double
    var height: Double

    var anchorX: Double
    var anchorY: Double

    val innerRadius get() = min(width, height) * 0.5
    val outerRadius get() = max(width, height) * 0.5

    val px get() = (cx + xr) * gridCellSize
    val py get() = (cy + yr) * gridCellSize
    val centerX get() = px + (0.5 - anchorX) * gridCellSize
    val centerY get() = py + (0.5 - anchorY) * gridCellSize
    val top get() = py - anchorY * height
    val right get() = px + (1 - anchorX) * width
    val bottom get() = py + (1 - anchorY) * height
    val left get() = px - anchorX * width

    var preXCheck: (() -> Unit)?
    var preYCheck: (() -> Unit)?

    fun updateGridPosition(tmod: Double) {
        preXCheck?.invoke()
        while (xr > 1) {
            xr--
            cx++
        }
        while (xr < 0) {
            xr++
            cx--
        }

        preYCheck?.invoke()
        while (yr > 1) {
            yr--
            cy++
        }
        while (yr < 0) {
            yr++
            cy--
        }
    }


    override fun buildDebugInfo(container: UiContainer) {
        container.uiCollapsibleSection("Grid Position") {
            uiEditableValue(this@GridPositionComponent::gridCellSize, name = "Grid Cell Size", min = 1)
            uiEditableValue(
                listOf(this@GridPositionComponent::cx, this@GridPositionComponent::cy),
                name = "(cx, cy)",
                min = 0,
                max = 10000
            )
            uiEditableValue(
                listOf(this@GridPositionComponent::xr, this@GridPositionComponent::yr),
                name = "(xr, yr)",
                min = 0.0,
                max = 1.0
            )
            uiEditableValue(
                listOf(this@GridPositionComponent::width, this@GridPositionComponent::height),
                name = "Size (w, h)"
            )
        }
    }

    companion object {
        operator fun invoke(
            cx: Int = 0,
            cy: Int = 0,
            xr: Double = 0.5,
            yr: Double = 0.5,
            anchorX: Double = 0.5,
            anchorY: Double = 0.5,
            gridCellSize: Int = 16
        ): GridPositionComponent {
            return GridPositionComponentDefault(cx, cy, xr, yr, anchorX, anchorY, gridCellSize)
        }
    }
}

open class GridPositionComponentDefault(
    override var cx: Int = 0,
    override var cy: Int = 0,
    override var xr: Double = 0.5,
    override var yr: Double = 0.5,
    override var anchorX: Double = 0.5,
    override var anchorY: Double = 0.5,
    override var gridCellSize: Int = 16
) : GridPositionComponent {
    override var preXCheck: (() -> Unit)? = null
    override var preYCheck: (() -> Unit)? = null

    override var width: Double = 16.0
    override var height: Double = 16.0
}
package com.lehaine.kiwi.component

import com.soywiz.klock.TimeSpan
import com.soywiz.korge.debug.uiCollapsibleSection
import com.soywiz.korge.debug.uiEditableValue
import com.soywiz.korui.UiContainer
import kotlin.math.min

interface ScaleAndStretchComponent : Component {
    var stretchX: Double
    var stretchY: Double

    var scaleX: Double
    var scaleY: Double

    var restoreSpeed: Double

    fun updateStretchAndScale(dt: TimeSpan)

    override fun buildDebugInfo(container: UiContainer) {
        container.uiCollapsibleSection("Scale and Stretch Component") {
            uiEditableValue(
                this@ScaleAndStretchComponent::stretchY,
                name = "Stretch X",
            )
            uiEditableValue(
                this@ScaleAndStretchComponent::stretchY,
                name = "Stretch Y"
            )
            uiEditableValue(
                this@ScaleAndStretchComponent::scaleX,
                name = "Scale X",
            )
            uiEditableValue(
                this@ScaleAndStretchComponent::scaleY,
                name = "Scale Y"
            )
        }
    }

    companion object {
        operator fun invoke(): ScaleAndStretchComponent {
            return ScaleAndStretchComponentDefault()
        }
    }
}

class ScaleAndStretchComponentDefault : ScaleAndStretchComponent {
    private var _stretchX = 1.0
    private var _stretchY = 1.0

    override var stretchX: Double
        get() = _stretchX
        set(value) {
            _stretchX = value
            _stretchY = 2 - value
        }
    override var stretchY: Double
        get() = _stretchY
        set(value) {
            _stretchX = 2 - value
            _stretchY = value
        }

    override var scaleX = 1.0
    override var scaleY = 1.0

    override var restoreSpeed: Double = 12.0

    override fun updateStretchAndScale(dt: TimeSpan) {
        _stretchX += (1 - _stretchX) * min(1.0, restoreSpeed * dt.seconds)
        _stretchY += (1 - _stretchY) * min(1.0, restoreSpeed * dt.seconds)
    }
}
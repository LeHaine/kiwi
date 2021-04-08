package com.lehaine.kiwi.component

import kotlin.math.min

interface ScaleAndStretchComponent : Component {
    var stretchX: Double
    var stretchY: Double

    var scaleX: Double
    var scaleY: Double

    fun updateStretchAndScale(tmod: Double)

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

    override fun updateStretchAndScale(tmod: Double) {
        _stretchX += (1 - _stretchX) * min(1.0, 0.2 * tmod)
        _stretchY += (1 - _stretchY) * min(1.0, 0.2 * tmod)
    }
}
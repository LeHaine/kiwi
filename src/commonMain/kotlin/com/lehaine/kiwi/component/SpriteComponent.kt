package com.lehaine.kiwi.component

import com.lehaine.kiwi.EnhancedSprite
import com.lehaine.kiwi.enhancedSprite
import com.soywiz.korge.view.Container

interface SpriteComponent : DrawableComponent, ScaleAndStretchComponent {

    fun updateSprite()

    companion object {
        operator fun invoke(container: Container, anchorX: Double, anchorY: Double): SpriteComponent {
            return SpriteComponentDefault(container, anchorX, anchorY)
        }
    }
}

class SpriteComponentDefault(container: Container, anchorX: Double, anchorY: Double) : SpriteComponent {
    override var dir = 1
    override val sprite: EnhancedSprite = container.enhancedSprite {
        smoothing = false
        this.anchorX = anchorX
        this.anchorY = anchorY
    }
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

    override fun updateStretchAndScale() {
        _stretchX += (1 - _stretchX) * 0.2
        _stretchY += (1 - _stretchY) * 0.2
    }

    override fun updateSprite() {
        updateStretchAndScale()
        sprite.scaleX = dir.toDouble() * scaleX * stretchX
        sprite.scaleY = scaleY * stretchY
    }
}
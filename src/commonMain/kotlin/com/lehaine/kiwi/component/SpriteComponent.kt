package com.lehaine.kiwi.component


import com.lehaine.kiwi.korge.view.EnhancedSprite

interface SpriteComponent {
    var dir: Int
    val sprite: EnhancedSprite

    companion object {
        operator fun invoke(anchorX: Double = 0.5, anchorY: Double = 0.5): SpriteComponent {
            return SpriteComponentDefault(anchorX, anchorY)
        }
    }
}

class SpriteComponentDefault(anchorX: Double = 0.5, anchorY: Double = 0.5) : SpriteComponent {
    override var dir = 0
    override val sprite: EnhancedSprite = EnhancedSprite(smoothing = false, anchorX = anchorX, anchorY = anchorY)
}
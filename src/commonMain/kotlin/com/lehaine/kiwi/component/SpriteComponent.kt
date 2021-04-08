package com.lehaine.kiwi.component


import com.lehaine.kiwi.korge.view.EnhancedSprite
import com.soywiz.korim.bitmap.Bitmaps
import com.soywiz.korim.bitmap.BmpSlice

interface SpriteComponent {
    var dir: Int
    val sprite: EnhancedSprite

    companion object {
        operator fun invoke(
            initBitmap: BmpSlice = Bitmaps.white,
            anchorX: Double = 0.5,
            anchorY: Double = 0.5
        ): SpriteComponent {
            return SpriteComponentDefault(initBitmap, anchorX, anchorY)
        }
    }
}

class SpriteComponentDefault(initBitmap: BmpSlice = Bitmaps.white, anchorX: Double = 0.5, anchorY: Double = 0.5) :
    SpriteComponent {
    override var dir = 1
    override val sprite: EnhancedSprite =
        EnhancedSprite(bitmap = initBitmap, smoothing = false, anchorX = anchorX, anchorY = anchorY)
}
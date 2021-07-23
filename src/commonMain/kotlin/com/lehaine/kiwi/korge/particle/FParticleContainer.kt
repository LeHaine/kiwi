package com.lehaine.kiwi.korge.particle

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.kmem.FBuffer
import com.soywiz.kmem.get
import com.soywiz.kmem.set
import com.soywiz.korge.view.fast.FSprite
import com.soywiz.korge.view.fast.FSprites
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author Colton Daily
 * @date 7/22/2021
 */
class FParticleContainer(maxSize: Int) : FSprites(maxSize) {
    companion object {
        private const val SIZE = 30
    }

    private val pData = FBuffer(maxSize * Float.SIZE_BYTES * SIZE)
    private val i32 = pData.i32
    private val f32 = pData.f32

    var FSprite.particleIndex: Int
        get() = i32[index * SIZE + 0]
        set(value) {
            i32[index * SIZE + 0] = value
        }

    var FSprite.xDelta: Float
        get() = f32[index * SIZE + 1]
        set(value) {
            f32[index * SIZE + 1] = value
        }
    var FSprite.yDelta: Float
        get() = f32[index * SIZE + 2]
        set(value) {
            f32[index * SIZE + 2] = value
        }
    var FSprite.scaleDelta: Float
        get() = f32[index * SIZE + 3]
        set(value) {
            f32[index * SIZE + 3] = value
        }
    var FSprite.scaleDeltaX: Float
        get() = f32[index * SIZE + 4]
        set(value) {
            f32[index * SIZE + 4] = value
        }
    var FSprite.scaleDeltaY: Float
        get() = f32[index * SIZE + 5]
        set(value) {
            f32[index * SIZE + 5] = value
        }
    var FSprite.scaleFriction: Float
        get() = f32[index * SIZE + 6]
        set(value) {
            f32[index * SIZE + 6] = value
        }
    var FSprite.scaleMultiplier: Float
        get() = f32[index * SIZE + 7]
        set(value) {
            f32[index * SIZE + 7] = value
        }
    var FSprite.scaleXMultiplier: Float
        get() = f32[index * SIZE + 8]
        set(value) {
            f32[index * SIZE + 8] = value
        }
    var FSprite.scaleYMultiplier: Float
        get() = f32[index * SIZE + 9]
        set(value) {
            f32[index * SIZE + 9] = value
        }
    var FSprite.rotationDelta: Float
        get() = f32[index * SIZE + 10]
        set(value) {
            f32[index * SIZE + 10] = value
        }
    var FSprite.rotationFriction: Float
        get() = f32[index * SIZE + 11]
        set(value) {
            f32[index * SIZE + 11] = value
        }
    var FSprite.friction
        get() = (frictionX + frictionY) * 0.5f
        set(value) {
            frictionX = value
            frictionY = value
        }

    var FSprite.frictionX: Float
        get() = f32[index * SIZE + 12]
        set(value) {
            f32[index * SIZE + 12] = value
        }
    var FSprite.frictionY: Float
        get() = f32[index * SIZE + 13]
        set(value) {
            f32[index * SIZE + 13] = value
        }
    var FSprite.gravityX: Float
        get() = f32[index * SIZE + 14]
        set(value) {
            f32[index * SIZE + 14] = value
        }
    var FSprite.gravityY: Float
        get() = f32[index * SIZE + 15]
        set(value) {
            f32[index * SIZE + 15] = value
        }

    /**
     * The speed to fade out the particle after [remainingLife] is 0
     */
    var FSprite.fadeOutSpeed: Float
        get() = f32[index * SIZE + 16]
        set(value) {
            f32[index * SIZE + 16] = value
        }

    /**
     * Total particle life
     */
    var FSprite.life: TimeSpan
        get() = i32[index * SIZE + 17].seconds
        set(value) {
            i32[index * SIZE + 17] = value.millisecondsInt
            remainingLife = value
        }

    /**
     * Life remaining before being killed
     */
    var FSprite.remainingLife: TimeSpan
        get() = i32[index * SIZE + 18].milliseconds
        set(value) {
            i32[index * SIZE + 18] = value.millisecondsInt
        }

    /**
     * Time to delay the particle from starting updates
     */

    var FSprite.delay: TimeSpan
        get() = i32[index * SIZE + 19].milliseconds
        set(value) {
            i32[index * SIZE + 19] = value.millisecondsInt
        }

    var FSprite.killed
        get() = i32[index * SIZE + 20] == 1
        set(value) {
            i32[index * SIZE + 20] = if (value) 1 else 0
        }

    val FSprite.alive get() = remainingLife.milliseconds > 0

    var FSprite.colorRdelta: Float
        get() = f32[index * SIZE + 21]
        set(value) {
            f32[index * SIZE + 21] = value
        }

    var FSprite.colorGdelta: Float
        get() = f32[index * SIZE + 22]
        set(value) {
            f32[index * SIZE + 22] = value
        }

    var FSprite.colorBdelta: Float
        get() = f32[index * SIZE + 23]
        set(value) {
            f32[index * SIZE + 23] = value
        }

    var FSprite.alphaDelta: Float
        get() = f32[index * SIZE + 24]
        set(value) {
            f32[index * SIZE + 24] = value
        }

    var FSprite.timeStamp: Float
        get() = f32[index * SIZE + 25]
        set(value) {
            f32[index * SIZE + 25] = value
        }

    var FSprite.data0
        get() = i32[index * SIZE + 26]
        set(value) {
            i32[index * SIZE + 26] = value
        }
    var FSprite.data1
        get() = i32[index * SIZE + 27]
        set(value) {
            i32[index * SIZE + 27] = value
        }
    var FSprite.data2
        get() = i32[index * SIZE + 28]
        set(value) {
            i32[index * SIZE + 28] = value
        }
    var FSprite.data3
        get() = i32[index * SIZE + 29]
        set(value) {
            i32[index * SIZE + 29] = value
        }

    fun FSprite.reset() {
        radiansf = 0f

        xDelta = 0f
        yDelta = 0f
        scaleDelta = 0f
        scaleDeltaX = 0f
        scaleDeltaY = 0f
        scaleFriction = 1f
        scaleMultiplier = 1f
        scaleXMultiplier = 1f
        scaleYMultiplier = 1f
        rotationDelta = 0f
        rotationFriction = 1f
        frictionX = 1f
        frictionY = 1f
        gravityX = 0f
        gravityY = 0f

        fadeOutSpeed = 0.1f

        life = 1.seconds

        remainingLife = TimeSpan.NIL
        delay = TimeSpan.ZERO
        killed = false

        colorRdelta = 0f
        colorGdelta = 0f
        colorBdelta = 0f
        alphaDelta = 0f
        timeStamp = DateTime.nowUnix().toFloat()

        data0 = 0
        data1 = 0
        data2 = 0
        data3 = 0
    }


    fun FSprite.moveAwayFrom(x: Float, y: Float, speed: Float) {
        val angle = atan2(y - this.y, x - this.x)
        xDelta = -cos(angle) * speed
        yDelta = -sin(angle) * speed
    }
}
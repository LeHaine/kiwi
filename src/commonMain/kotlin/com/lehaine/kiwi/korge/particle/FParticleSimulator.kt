package com.lehaine.kiwi.korge.particle

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.korge.view.fast.FSprite
import com.soywiz.korge.view.fast.fastForEach
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korim.color.RGBA
import kotlin.math.pow

class FParticleSimulator {
    var bestIdx = 0
    var stride = 8

    fun alloc(particleContainer: FParticleContainer, bmpSlice: BmpSlice, x: Float, y: Float): FSprite {
        return if (particleContainer.available > 0) {
            particleContainer.run {
                alloc().also {
                    it.reset()
                    it.resetParticle()
                    it.x = x
                    it.y = y
                    it.setTex(bmpSlice)
                }
            }
        } else {
            val best = particleContainer.run {
                FSprite(bestIdx).apply {
                    //     onKill?.invoke()
                    reset()
                    resetParticle()
                    this.x = x
                    this.y = y
                }
            }

            bestIdx += stride

            if (bestIdx / stride >= particleContainer.available) {
                bestIdx = 0
            }
            best
        }
    }


    private fun advance(particle: FSprite, particleContainer: FParticleContainer, dt: TimeSpan, tmod: Float): Boolean {
        particleContainer.run {
            particle.delay -= dt
            if (particle.killed || particle.delay > 0.milliseconds) return false

            with(particle) {
                // gravity
                xDelta += gravityX * dt.seconds.toFloat()
                yDelta += gravityY * tmod

                // movement
                x += xDelta * tmod
                y += yDelta * tmod

                // friction
                if (frictionX == frictionY) {
                    val frictTmod = frictionX.fastPow(tmod)
                    xDelta *= frictTmod
                    yDelta *= frictTmod
                } else {
                    xDelta *= frictionX.fastPow(tmod)
                    yDelta *= frictionY.fastPow(tmod)
                }

                // rotation
                radiansf += rotationDelta * tmod
                rotationDelta *= rotationFriction * tmod

                // scale
                scaleX += (scaleDelta + scaleDeltaX) * tmod
                scaleY += (scaleDelta + scaleDeltaY) * tmod
                val scaleMul = scaleMultiplier.fastPow(tmod)
                scaleX *= scaleMul
                scaleX *= scaleXMultiplier.fastPow(tmod)
                scaleY *= scaleMul
                scaleY *= scaleYMultiplier.fastPow(tmod)
                val scaleFrictPow = scaleFriction.fastPow(tmod)
                scaleDelta *= scaleFrictPow
                scaleDeltaX *= scaleFrictPow
                scaleDeltaY *= scaleFrictPow

                // color
                val colorR = colorMul.rd + particle.colorRdelta * tmod
                val colorG = colorMul.gd + particle.colorGdelta * tmod
                val colorB = colorMul.bd + particle.colorBdelta * tmod
                val colorA = colorMul.ad + particle.alphaDelta * tmod
                colorMul = RGBA.float(colorR, colorG, colorB, colorA)

                // life
                remainingLife -= dt
                if (remainingLife.milliseconds <= 0) {
                    colorMul = colorMul.withAd(colorMul.ad - (fadeOutSpeed * tmod))
                }

                return if (remainingLife <= 0.milliseconds && colorMul.a <= 0) {
                    particle.life = TimeSpan.ZERO
                    particle.killed = true
                    free(particle)
                    false
                } else {
                    true
                }
            }
        }
    }

    fun simulate(
        particleContainer: FParticleContainer,
        dt: TimeSpan,
        optionalTmod: Double = -1.0,
        callback: FParticleContainer.(FSprite) -> Unit = {}
    ) {
        val tmod = if (optionalTmod < 0) {
            dt.seconds * 60
        } else {
            optionalTmod
        }
        particleContainer.fastForEach {
            if (advance(it, particleContainer, dt, tmod.toFloat())) {
                particleContainer.callback(it)
            }
        }
    }
}


private fun Float.fastPow(power: Float): Float {
    if (power == 1f || this == 0f || this == 1f) {
        return this
    }
    return pow(power)
}
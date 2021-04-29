package com.lehaine.kiwi.korge.view

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.kmem.umod
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmaps
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korio.async.Signal
import com.soywiz.korma.geom.vector.VectorPath

inline fun Container.enhancedSprite(
    bitmap: BmpSlice = Bitmaps.white,
    anchorX: Double = 0.0,
    anchorY: Double = 0.0,
    hitShape: VectorPath? = null,
    smoothing: Boolean = true,
    callback: @ViewDslMarker EnhancedSprite.() -> Unit = {}
): EnhancedSprite = EnhancedSprite(bitmap, anchorX, anchorY, hitShape, smoothing).addTo(this, callback)

class EnhancedSprite(
    bitmap: BmpSlice = Bitmaps.white,
    anchorX: Double = 0.0,
    anchorY: Double = anchorX,
    hitShape: VectorPath? = null,
    smoothing: Boolean = true
) : BaseImage(bitmap, anchorX, anchorY, hitShape, smoothing) {

    private var animationRequested = false
    var totalFramesPlayed = 0
    var currentFrame = 0
        private set(value) {
            field = value umod totalFrames
            bitmap = currentAnimation?.getSprite(value) ?: bitmap
        }

    private var animationNumberOfFramesRequested = 0
        set(value) {
            if (value == 0) {
                stopAnimation()
                when (animationType) {
                    AnimationType.STANDARD -> triggerEvent(onAnimationCompleted)
                    else -> triggerEvent(onAnimationStopped)
                }
            }
            field = value
        }
    private var animationType = AnimationType.STANDARD

    private var _onAnimationCompleted: Signal<EnhancedSpriteAnimation>? = null
    private var _onAnimationStopped: Signal<EnhancedSpriteAnimation>? = null
    private var _onAnimationStarted: Signal<EnhancedSpriteAnimation>? = null
    private var _onFrameChanged: Signal<EnhancedSpriteAnimation>? = null

    val onAnimationCompleted: Signal<EnhancedSpriteAnimation>
        get() {
            if (_onAnimationCompleted == null) _onAnimationCompleted = Signal()
            return _onAnimationCompleted!!
        }

    val onAnimationStopped: Signal<EnhancedSpriteAnimation>
        get() {
            if (_onAnimationStopped == null) _onAnimationStopped = Signal()
            return _onAnimationStopped!!
        }

    val onAnimationStarted: Signal<EnhancedSpriteAnimation>
        get() {
            if (_onAnimationStarted == null) _onAnimationStarted = Signal()
            return _onAnimationStarted!!
        }

    val onFrameChanged: Signal<EnhancedSpriteAnimation>
        get() {
            if (_onFrameChanged == null) _onFrameChanged = Signal()
            return _onFrameChanged!!
        }

    var spriteDisplayTime: TimeSpan = 100.milliseconds
    private var animationLooped = false

    private var lastAnimationFrameTime: TimeSpan = 0.milliseconds

    private var animationRemainingDuration: TimeSpan = 0.milliseconds
        set(value) {
            if (value <= 0.milliseconds && animationType == AnimationType.DURATION) {
                stopAnimation()
                triggerEvent(_onAnimationCompleted)
            }
            field = value
        }

    private var currentAnimation: EnhancedSpriteAnimation? = null

    private var lastAnimation: EnhancedSpriteAnimation? = null
    private var lastLooped: Boolean = false
    private var lastOnAnimationEndCallback: (() -> Unit)? = null
    private var lastOnAnimationFrameChangeCallback: ((Int) -> Unit)? = null
    private var overlapPlaying: Boolean = false

    private var onAnimationEndCallback: (() -> Unit)? = null
    private var onAnimationFrameChangeCallback: ((Int) -> Unit)? = null

    val anim = AnimationManager()

    init {
        addUpdater { frameTime ->
            if (animationRequested) {
                nextSprite(frameTime)
            }
        }
    }


    fun playOverlap(
        spriteAnimation: EnhancedSpriteAnimation,
        onAnimationFrameChange: ((Int) -> Unit)? = null,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        if (!overlapPlaying) {
            lastOnAnimationEndCallback = onAnimationEndCallback
            lastOnAnimationFrameChangeCallback = onAnimationFrameChangeCallback
            lastAnimation = currentAnimation
            lastLooped = animationLooped
        }
        overlapPlaying = true
        playAnimation(
            spriteAnimation,
            onAnimationFrameChange = onAnimationFrameChange,
            onAnimationEnd = onAnimationEnd
        )
    }

    fun playOverlap(bmpSlice: BmpSlice, frameTime: TimeSpan = 50.milliseconds, numFrames: Int = 1) =
        playOverlap(EnhancedSpriteAnimation(bmpSlice, numFrames, frameTime))

    fun playAnimation(
        times: Int = 1,
        spriteAnimation: EnhancedSpriteAnimation? = currentAnimation,
        onAnimationFrameChange: ((Int) -> Unit)? = null,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        onAnimationFrameChangeCallback = onAnimationFrameChange
        onAnimationEndCallback = onAnimationEnd
        updateCurrentAnimation(
            spriteAnimation = spriteAnimation,
            animationCyclesRequested = times,
            type = AnimationType.STANDARD
        )
    }

    fun playAnimation(
        spriteAnimation: EnhancedSpriteAnimation? = currentAnimation,
        onAnimationFrameChange: ((Int) -> Unit)? = null,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        onAnimationEndCallback = onAnimationEnd
        onAnimationFrameChangeCallback = onAnimationFrameChange
        updateCurrentAnimation(
            spriteAnimation = spriteAnimation,
            animationCyclesRequested = 1,
            type = AnimationType.STANDARD
        )
    }

    fun playAnimationLooped(
        spriteAnimation: EnhancedSpriteAnimation? = currentAnimation,
        onAnimationFrameChange: ((Int) -> Unit)? = null,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        onAnimationFrameChangeCallback = onAnimationFrameChange
        onAnimationEndCallback = onAnimationEnd
        updateCurrentAnimation(
            spriteAnimation = spriteAnimation,
            looped = true,
            type = AnimationType.LOOPED
        )
    }


    fun stopAnimation() {
        animationRequested = false
        triggerEvent(_onAnimationStopped)
        onAnimationEndCallback?.invoke()
        if (overlapPlaying) {
            overlapPlaying = false
            if (lastLooped) {
                playAnimationLooped(
                    lastAnimation,
                    lastOnAnimationFrameChangeCallback,
                    lastOnAnimationEndCallback
                )
            } else {
                playAnimation(lastAnimation, lastOnAnimationFrameChangeCallback, lastOnAnimationEndCallback)
            }
            lastAnimation = null
            lastLooped = false
            lastOnAnimationEndCallback = null
            lastOnAnimationFrameChangeCallback = null
        }
    }

    private fun nextSprite(frameTime: TimeSpan) {
        lastAnimationFrameTime += frameTime
        if (lastAnimationFrameTime + frameTime >= this.spriteDisplayTime) {
            when (animationType) {
                AnimationType.STANDARD -> {
                    if (animationNumberOfFramesRequested > 0) {
                        animationNumberOfFramesRequested--
                    }
                }
                AnimationType.DURATION -> {
                    animationRemainingDuration -= lastAnimationFrameTime
                }
                AnimationType.LOOPED -> {

                }
            }
            if (animationRequested) {
                totalFramesPlayed++
                currentFrame++
                spriteDisplayTime = currentAnimation?.getSpriteFrameTime(currentFrame) ?: 0.milliseconds
                onAnimationFrameChangeCallback?.invoke(currentFrame)
                triggerEvent(_onFrameChanged)
                lastAnimationFrameTime = 0.milliseconds
            }
        }
    }

    val totalFrames: Int
        get() {
            val ca = currentAnimation ?: return 1
            return ca.totalFrames
        }

    private fun updateCurrentAnimation(
        spriteAnimation: EnhancedSpriteAnimation?,
        animationCyclesRequested: Int = 1,
        looped: Boolean = false,
        type: AnimationType = AnimationType.STANDARD
    ) {
        currentAnimation = spriteAnimation
        currentFrame = 0
        triggerEvent(_onAnimationStarted)
        onAnimationFrameChangeCallback?.invoke(currentFrame)
        spriteDisplayTime = currentAnimation?.getSpriteFrameTime(currentFrame) ?: 0.milliseconds
        animationLooped = looped
        animationType = type
        animationRequested = true
        currentAnimation?.let {
            val requestedFrames = animationCyclesRequested * it.totalFrames
            this.animationNumberOfFramesRequested = requestedFrames
        }
    }

    fun setFrame(index: Int) {
        currentFrame = index

    }

    private fun triggerEvent(signal: Signal<EnhancedSpriteAnimation>?) {
        if (signal != null) currentAnimation?.let { signal.invoke(it) }
    }

    inner class AnimationManager {
        private val states = arrayListOf<AnimationState>()

        /**
         * Priority is represented by the deepest. The deepest has top priority while the shallowest has lowest.
         */
        fun registerState(anim: EnhancedSpriteAnimation, loop: Boolean = true, reason: () -> Boolean) {
            states.add(AnimationState(anim, loop, reason))
        }

        fun removeState(anim: EnhancedSpriteAnimation) {
            states.find { it.anim == anim }?.also { states.remove(it) }
        }

        fun removeAllStates() {
            states.clear()
        }

        internal fun update() {
            states.fastForEach { state ->
                if (state.reason()) {
                    if (state.loop) {
                        playAnimationLooped(state.anim)
                    } else {
                        playAnimation(state.anim)
                    }
                    return
                }
            }
        }
    }

    private data class AnimationState(val anim: EnhancedSpriteAnimation, val loop: Boolean, val reason: () -> Boolean)
}

fun EnhancedSprite.registerState(anim: EnhancedSpriteAnimation, loop: Boolean = true, reason: () -> Boolean) =
    this.anim.registerState(anim, loop, reason)

fun EnhancedSprite.removeState(anim: EnhancedSpriteAnimation) = this.anim.removeState(anim)
fun EnhancedSprite.removeAllStates() = this.anim.removeAllStates()
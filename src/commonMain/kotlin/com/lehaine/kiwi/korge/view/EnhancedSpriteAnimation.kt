package com.lehaine.kiwi.korge.view

import com.soywiz.kds.FastArrayList
import com.soywiz.kds.toFastList
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.kmem.umod
import com.soywiz.korim.atlas.Atlas
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korim.bitmap.sliceWithSize

class EnhancedSpriteAnimation(
    val sprites: List<BmpSlice>,
    val frames: List<Int>,
    val frameTimes: List<TimeSpan>
) {

    constructor(
        bmpSlice: BmpSlice,
        numFrames: Int = 1,
        frameTime: TimeSpan = 100.milliseconds,
    ) : this(listOf<BmpSlice>(bmpSlice), List(numFrames) { 0 }, List(numFrames) { frameTime })

    val duration = frameTimes.reduce { acc, timeSpan -> acc + timeSpan }

    companion object {
        operator fun invoke(
            spriteWidth: Int = 16,
            spriteMap: Bitmap,
            spriteHeight: Int = 16,
            marginTop: Int = 0,
            marginLeft: Int = 0,
            columns: Int = 1,
            rows: Int = 1,
            offsetBetweenColumns: Int = 0,
            offsetBetweenRows: Int = 0
        ): EnhancedSpriteAnimation {
            val bmps = FastArrayList<BmpSlice>().apply {
                for (row in 0 until rows) {
                    for (col in 0 until columns) {
                        add(
                            spriteMap.sliceWithSize(
                                marginLeft + (spriteWidth + offsetBetweenColumns) * col,
                                marginTop + (spriteHeight + offsetBetweenRows) * row,
                                spriteWidth,
                                spriteHeight,
                                name = "slice$size"
                            )
                        )
                    }
                }
            }

            return EnhancedSpriteAnimation(bmps, List(bmps.size) { it }, List(bmps.size) { TimeSpan.NIL })
        }
    }


    val spriteStackSize: Int get() = sprites.size
    val totalFrames: Int get() = frames.size
    val firstSprite: BmpSlice get() = sprites[0]
    fun getSprite(index: Int): BmpSlice = sprites[frames[index umod frames.size]]
    fun getSpriteFrameTime(index: Int): TimeSpan = frameTimes[frames[index umod frames.size]]
    operator fun get(index: Int) = getSprite(index)
}


class EnhancedSpriteAnimationBuilder(private val bmpSlices: List<BmpSlice>) {

    private val frames = arrayListOf<Int>()
    private val frameTimes = arrayListOf<TimeSpan>()

    fun frames(indices: IntRange = 0..bmpSlices.size, repeats: Int = 0, frameTime: TimeSpan = 100.milliseconds) {
        repeat(repeats + 1) {
            frames.addAll(indices)
            repeat(indices.count()) { frameTimes += frameTime }
        }
    }

    fun frames(index: Int = 0, repeats: Int = 0, frameTime: TimeSpan = 100.milliseconds) =
        frames(index..index, repeats, frameTime)

    fun build(): EnhancedSpriteAnimation = EnhancedSpriteAnimation(bmpSlices, frames, frameTimes)
}

data class Frames(val indices: IntRange, val repeat: Int = 0, val frameTime: TimeSpan)


fun Atlas.getEnhancedSpriteAnimation(
    prefix: String = "",
    defaultTimePerFrame: TimeSpan = TimeSpan.NIL
): EnhancedSpriteAnimation {
    val bmps =
        this.entries.filter { it.filename.startsWith(prefix) }.map { it.slice }.toFastList()
    return EnhancedSpriteAnimation(bmps, List(bmps.size) { it }, List(bmps.size) { defaultTimePerFrame })
}

fun Atlas.getEnhancedSpriteAnimation(
    regex: Regex,
    defaultTimePerFrame: TimeSpan = TimeSpan.NIL
): EnhancedSpriteAnimation {
    val bmps = this.entries.filter { regex.matches(it.filename) }.map { it.slice }.toFastList()
    return EnhancedSpriteAnimation(bmps, List(bmps.size) { it }, List(bmps.size) { defaultTimePerFrame })
}

fun Atlas.createEnhancedSpriteAnimation(prefix: String, action: EnhancedSpriteAnimationBuilder.() -> Unit) =
    EnhancedSpriteAnimationBuilder(this.entries.filter { it.filename.startsWith(prefix) }.map { it.slice }
        .toFastList()).apply(
        action
    ).build()

package com.lehaine.kiwi.korge

import com.soywiz.kds.Pool
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.korge.component.UpdateComponent
import com.soywiz.korge.view.View

private data class CooldownTimer(
    var time: TimeSpan,
    var name: String,
    var callback: () -> Unit
) {
    val ratio get() = 1 - elapsed / time
    var elapsed = 0.milliseconds
    val finished get() = elapsed >= time

    fun update(dt: TimeSpan) {
        elapsed += dt
        if (finished) {
            callback()
        }
    }
}

class CooldownComponents(override val view: View) : UpdateComponent {
    private val cooldownTimerPool = Pool(
        reset = {
            it.elapsed = 0.milliseconds
            it.time = 0.milliseconds
            it.name = ""
            it.callback = {}
        },
        gen = { CooldownTimer(0.milliseconds, "", {}) })

    private val timers = mutableMapOf<String, CooldownTimer>()

    override fun update(dt: TimeSpan) {
        val iterate = timers.iterator()
        while (iterate.hasNext()) {
            val timer = iterate.next().value.also { it.update(dt) }
            if (timer.finished) {
                iterate.remove()
            }
        }
    }

    private fun addTimer(name: String, timer: CooldownTimer) {
        timers[name] = timer
    }

    private fun removeTimer(name: String) {
        timers.remove(name)?.also {
            cooldownTimerPool.free(it)
        }
    }

    private fun reset(name: String, time: TimeSpan, callback: () -> Unit) {
        timers[name]?.apply {
            this.time = time
            this.callback = callback
            this.elapsed = 0.milliseconds
        }
    }

    private fun interval(name: String, time: TimeSpan, callback: () -> Unit = {}) {
        if (has(name)) {
            reset(name, time, callback)
            return
        }
        val timer = cooldownTimerPool.alloc().apply {
            this.time = time
            this.name = name
            this.callback = callback
        }
        addTimer(name, timer)
    }


    fun timeout(name: String, time: TimeSpan, callback: () -> Unit = { }) =
        interval(name, time, callback)

    fun has(name: String) = timers[name] != null

    fun remove(name: String) = removeTimer(name)

    fun ratio(name: String): Double {
        return timers[name]?.ratio ?: 0.0
    }
}

val View.cooldown get() = this.getOrCreateComponentUpdate { CooldownComponents(this) }
val View.cd get() = this.cooldown

fun View.cooldown(name: String, time: TimeSpan, callback: () -> Unit = {}) =
    this.cooldown.timeout(name, time, callback)

fun View.cd(name: String, time: TimeSpan, callback: () -> Unit = {}) =
    cooldown(name, time, callback)
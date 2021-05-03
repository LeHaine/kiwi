package com.lehaine.kiwi.korge

import com.lehaine.kiwi.korge.view.Layers
import com.lehaine.kiwi.korge.view.addToLayer
import com.soywiz.klock.Frequency
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.korge.component.UpdateComponent
import com.soywiz.korge.component.attach
import com.soywiz.korge.component.detach
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.View
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korim.atlas.Atlas
import com.soywiz.korio.lang.Cancellable

fun Atlas.getByPrefix(prefix: String = "") = this.entries.first { it.filename.startsWith(prefix) }.slice
fun Atlas.getRandomByPrefix(prefix: String = "") = this.entries.filter { it.filename.startsWith(prefix) }.random().slice

inline fun Layers.container(layer: Int = 0, callback: @ViewDslMarker Container.() -> Unit = {}) =
    Container().addToLayer(this, layer, callback)

fun <T : View> T.addFixedInterpUpdater(
    timesPerSecond: Frequency,
    initial: Boolean = true,
    interpolate: (ratio: Double) -> Unit,
    updatable: T.() -> Unit
): Cancellable = addFixedInterpUpdater(timesPerSecond.timeSpan, initial, interpolate, updatable)

fun <T : View> T.addFixedInterpUpdater(
    time: TimeSpan,
    initial: Boolean = true,
    interpolate: (ratio: Double) -> Unit,
    updatable: T.() -> Unit
): Cancellable {
    var accum = 0.0.milliseconds
    val component = object : UpdateComponent {
        override val view: View get() = this@addFixedInterpUpdater
        override fun update(dt: TimeSpan) {
            accum += dt
            while (accum >= time * 0.75) {
                accum -= time
                updatable(this@addFixedInterpUpdater)
            }

            interpolate(accum / time)
        }
    }.attach()
    if (initial) {
        updatable(this@addFixedInterpUpdater)
    }
    return Cancellable { component.detach() }
}
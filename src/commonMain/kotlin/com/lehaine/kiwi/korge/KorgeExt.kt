package com.lehaine.kiwi.korge

import com.lehaine.kiwi.korge.view.Layers
import com.lehaine.kiwi.korge.view.addToLayer
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korim.atlas.Atlas

fun Atlas.getByPrefix(prefix: String = "") = this.entries.first { it.filename.startsWith(prefix) }.slice
fun Atlas.getRandomByPrefix(prefix: String = "") = this.entries.filter { it.filename.startsWith(prefix) }.random().slice

inline fun Layers.container(layer: Int = 0, callback: @ViewDslMarker Container.() -> Unit = {}) =
    Container().addToLayer(this, layer, callback)
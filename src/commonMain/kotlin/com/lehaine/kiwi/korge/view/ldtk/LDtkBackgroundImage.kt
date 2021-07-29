package com.lehaine.kiwi.korge.view.ldtk

import com.lehaine.kiwi.korge.view.Layers
import com.lehaine.kiwi.korge.view.addToLayer
import com.soywiz.korge.view.*

/**
 * @author Colton Daily
 * @date 7/29/2021
 */
inline fun Container.ldtkBgImage(
    ldtkLevel: LDtkLevel,
    callback: Image.() -> Unit = {}
) = ldtkLevel.bgImage?.let {
    image(texture = it) {
        ldtkLevel.level.bgImageInfos?.let {
            xy(it.topLeftX, it.topLeftY)
            scale(it.scaleX, it.scaleY)
        }
    }.addTo(this, callback)
}


inline fun Layers.ldtkBgImage(
    ldtkLevel: LDtkLevel,
    layer: Int = 0,
    callback: Image.() -> Unit = {}
) = ldtkLevel.bgImage?.let {
    image(texture = it) {
        ldtkLevel.level.bgImageInfos?.let {
            xy(it.topLeftX, it.topLeftY)
            scale(it.scaleX, it.scaleY)
        }
    }.addToLayer(this, layer, callback)
}

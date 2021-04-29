package com.lehaine.kiwi.korge.view

import com.soywiz.kds.FastArrayList
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.kds.iterators.fastForEachWithIndex
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.View
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korge.view.addTo

inline fun Container.layers(callback: @ViewDslMarker (Layers.() -> Unit) = {}) =
    Layers().addTo(this, callback)

inline fun Layers.layers(layer: Int, callback: @ViewDslMarker (Layers.() -> Unit) = {}) =
    Layers().addToLayer(this, layer, callback)

class Layers : Container() {
    private val layers = FastArrayList<Container>()

    val numLayers get() = layers.size

    fun add(view: View, layer: Int = 0) = addChildToLayer(view, layer)

    fun addChild(view: View, layer: Int = 0) = add(view, layer)

    fun remove(view: View?) {
        layers.fastForEach {
            if (view?.parent == it) {
                view.removeFromParent()
                return
            }
        }
    }

    /**
     * @return the layer index on which the child view resides on
     */
    fun getChildLayer(view: View): Int {
        layers.fastForEachWithIndex { index, container ->
            if (view.parent == container) {
                return index
            }
        }
        return -1
    }


    /**
     * Sends child to the back of its layer so that it is rendered first (behind all other objects in its layer).
     */
    fun sendToBack(view: View) {
        layers.fastForEach {
            if (view.parent == it) {
                it.sendChildToBack(view)
                return
            }
        }
    }

    /**
     * Sends child to front of its layer so that it is rendered last (in front of all other objects in its layer)
     */
    fun sendToFront(view: View) {
        layers.fastForEach {
            if (view.parent == it) {
                it.sendChildToFront(view)
                return
            }
        }
    }

    /**
     * Grabs the specified layer.
     */
    fun getLayer(index: Int): Container = layers[index]

    private fun addChildToLayer(view: View, layer: Int) {
        while (layer >= numLayers) {
            val container = Container().addTo(this)
            layers += container

            container.addChild(view)
        }
    }
}

inline fun <T : View> T.addToLayer(
    instance: Layers,
    layer: Int = 0,
    callback: @ViewDslMarker T.() -> Unit = {}
): T {
    instance.add(this, layer)
    apply(callback)
    return this
}

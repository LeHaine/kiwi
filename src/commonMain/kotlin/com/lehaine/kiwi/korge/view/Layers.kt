package com.lehaine.kiwi.korge.view

import com.soywiz.kds.FastArrayList
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.kds.iterators.fastForEachWithIndex
import com.soywiz.korge.render.RenderContext
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.BoundsBuilder
import com.soywiz.korma.geom.Rectangle

inline fun Container.layers(callback: @ViewDslMarker (Layers.() -> Unit) = {}) =
    Layers().addTo(this, callback)

class Layers : View() {
    private val children = FastArrayList<Container>()

    val numLayers get() = children.size

    fun add(view: View, layer: Int = 0) = addChildToLayer(view, layer)

    fun addChild(view: View, layer: Int = 0) = add(view, layer)

    fun remove(view: View?) {
        children.fastForEach {
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
        children.fastForEachWithIndex { index, container ->
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
        children.fastForEach {
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
        children.fastForEach {
            if (view.parent == it) {
                it.sendChildToFront(view)
                return
            }
        }
    }

    /**
     * Grabs the specified layer.
     */
    operator fun get(index: Int): View = children[index]

    private fun addChildToLayer(view: View, layer: Int) {
        while (layer >= numLayers) {
            val container = Container()
            children += container

            container.addChild(view)
        }
    }

    override fun renderInternal(ctx: RenderContext) {
        if (!visible) return

        forEachChild {
            it.render(ctx)
        }
    }


    override fun renderDebug(ctx: RenderContext) {
        forEachChild {
            it.render(ctx)
        }
        super.renderDebug(ctx)
    }

    private val bb = BoundsBuilder()
    private val tempRect = Rectangle()

    override fun getLocalBoundsInternal(out: Rectangle) {
        bb.reset()
        forEachChild { child: View ->
            child.getBounds(this, tempRect)
            bb.add(tempRect)
        }
        bb.getBounds(out)
    }

    override fun createInstance(): View {
        return Layers()
    }

    override fun clone(): View {
        val out = super.clone()
        children.fastForEach { out += it.clone() }
        return out
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

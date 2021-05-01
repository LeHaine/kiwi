package com.lehaine.kiwi.korge

import com.lehaine.kiwi.checkTrue
import com.lehaine.kiwi.korge.view.Layers
import kotlin.math.max

class SceneNodeManager {
    var targetFps = 60

    private val sceneNodes = mutableListOf<SceneNode>()

    fun addSceneNode(sceneNode: SceneNode) {
        sceneNodes += sceneNode
    }

    fun removeSceneNode(sceneNode: SceneNode) {
        sceneNodes -= sceneNode
    }

    fun hasSceneNode(sceneNode: SceneNode) = sceneNodes.contains(sceneNode)

    fun updateAll(utmod: Double) {
        sceneNodes.forEach {
            _preUpdate(it, utmod)
        }

        sceneNodes.forEach {
            _update(it)
        }

        sceneNodes.forEach {
            _fixedUpdate(it)
        }

        sceneNodes.forEach {
            _postUpdate(it)
        }

        _cleanUp(sceneNodes)
    }

    fun resizeAll() {
        sceneNodes.forEach {
            _resize(it)
        }
    }

    private fun _canRun(sceneNode: SceneNode) = !sceneNode.paused && !sceneNode.destroyed

    private fun _preUpdate(sceneNode: SceneNode, utmod: Double) {
        sceneNode.utmod = utmod

        if (_canRun(sceneNode)) {
            sceneNode.preUpdate()

            sceneNode.children.forEach {
                _preUpdate(it, sceneNode.utmod)
            }
        }
    }

    private fun _update(sceneNode: SceneNode) {
        if (!_canRun(sceneNode)) return

        sceneNode.update()

        sceneNode.children.forEach {
            _update(it)
        }
    }

    private fun _fixedUpdate(sceneNode: SceneNode) {
        if (!_canRun(sceneNode)) return

        sceneNode.run {
            fixedUpdateCounter += tmod
            while (fixedUpdateCounter >= targetFps / fixedUpdateFps) {
                fixedUpdateCounter -= targetFps / fixedUpdateFps
                fixedUpdate()
            }

            children.forEach {
                _fixedUpdate(it)
            }
        }
    }

    private fun _postUpdate(sceneNode: SceneNode) {
        if (!_canRun(sceneNode)) return

        sceneNode.postUpdate()
        sceneNode.children.forEach {
            _postUpdate(it)
        }
    }

    private fun _resize(sceneNode: SceneNode) {
        if (!sceneNode.destroyed) {
            sceneNode.onResize()
            sceneNode.children.forEach {
                _resize(it)
            }
        }
    }

    private fun _cleanUp(sceneNodes: MutableList<SceneNode>) {
        var i = 0
        while (i < sceneNodes.size) {
            val sceneNode = sceneNodes[i]
            if (sceneNode.destroyed) {
                _dispose(sceneNode)
            } else {
                _cleanUp(sceneNode.children)
                i++
            }
        }
    }

    private fun _dispose(sceneNode: SceneNode) {
        sceneNode.children.forEach {
            it.destroy()
        }
        _cleanUp(sceneNode.children)

        if (sceneNode.parent != null) {
            sceneNode.parent?.children?.remove(sceneNode)
        } else {
            sceneNodes.remove(sceneNode)
        }

        sceneNode.parent = null
        sceneNode.sceneNodeManager = null
        sceneNode.root.removeFromParent()
        sceneNode.root.removeChildren()
        sceneNode.onDispose()
    }
}


open class SceneNode(var parent: SceneNode? = null) {

    var sceneNodeManager: SceneNodeManager? = null
    val root = Layers()

    var timeMultiplier = 1.0
    var utmod = 1.0
    val tmod get() = utmod * max(0.0, timeMultiplier * 1.0)

    // fixed update
    var fixedUpdateFps = 30
    var fixedUpdateCounter = 0.0

    var paused = false
    var destroyed = false
    val children = mutableListOf<SceneNode>()

    fun addTo(newParent: SceneNode?) {
        parent = newParent
        parent?.addChild(this)
    }

    fun addChild(sceneNode: SceneNode) {
        if (sceneNode.parent == null) {
            sceneNodeManager?.removeSceneNode(sceneNode)
        } else {
            sceneNode.parent?.children?.remove(sceneNode)
        }

        sceneNode.parent = this
        sceneNode.sceneNodeManager = sceneNodeManager
        children.add(sceneNode)
        sceneNode.init()
    }

    fun removeAndDestroyChild(child: SceneNode) {
        checkTrue(child.parent != this) {
            "Not a child of this process"
        }

        child.parent = null
        child.sceneNodeManager = null
        child.root.removeFromParent()
        child.root.removeChildren()
        children.remove(child)
        child.destroy()
    }


    fun destroy() {
        destroyed = true
    }

    fun destroyChildren() {
        children.forEach {
            it.destroy()
        }
    }

    fun isRootProcess() = parent == null

    open fun init() {}

    open fun preUpdate() {}

    open fun update() {}

    open fun fixedUpdate() {}

    open fun postUpdate() {}

    open fun onResize() {}

    open fun onDispose() {}
}
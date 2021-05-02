package com.lehaine.kiwi.korge

import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addUpdater

abstract class SceneNodeProcessor : Scene() {

    abstract val rootSceneNode: SceneNode

    open var targetFPS = 60.0
        set(value) {
            field = value
            sceneNodeManager.targetFps = value.toInt()
        }

    protected val sceneNodeManager = SceneNodeManager()
    protected val fpsDT get() = (1.0 / targetFPS).seconds

    override suspend fun Container.sceneInit() {
        initializeRootSceneNode()
    }

    protected suspend fun Container.initializeRootSceneNode() {
        addChild(rootSceneNode.root)
        rootSceneNode.sceneNodeManager = sceneNodeManager
        rootSceneNode.run {
            load()
            root.init()
        }

        addUpdater { dt ->
            val tmod = if (dt == 0.milliseconds) 0.0 else (dt / fpsDT)
            sceneNodeManager.updateAll(tmod)
        }
    }
}
package com.lehaine.kiwi.korge

import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addUpdater

abstract class SceneNodeProcessor : Scene() {

    open val wantedFPS = 60.0

    abstract val rootSceneNode: SceneNode
    protected val sceneNodeManager = SceneNodeManager()

    protected val fpsDT get() = (1.0 / wantedFPS).seconds

    override suspend fun Container.sceneInit() {
        addChild(rootSceneNode.root)

        rootSceneNode.sceneNodeManager = sceneNodeManager

        addUpdater { dt ->
            val tmod = if (dt == 0.milliseconds) 0.0 else (dt / fpsDT)
            sceneNodeManager.updateAll(tmod)
        }
    }
}
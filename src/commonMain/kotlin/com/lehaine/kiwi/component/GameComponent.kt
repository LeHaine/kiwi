package com.lehaine.kiwi.component

import com.soywiz.korui.UiContainer

interface GameComponent : Component {
    var fixedProgressionRatio: Double
    val entities: ArrayList<Entity>
    val staticEntities: ArrayList<Entity>
    val level: LevelComponent<*>

    override fun buildDebugInfo(container: UiContainer) {
        level.buildDebugInfo(container)
    }
}
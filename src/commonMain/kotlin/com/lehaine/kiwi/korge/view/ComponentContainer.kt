package com.lehaine.kiwi.korge.view

import com.lehaine.kiwi.component.Component
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Views
import com.soywiz.korui.UiContainer

/**
 * A container that allows to display component debug info.
 */
class ComponentContainer(initialComponents: List<Component>) : Container() {
    private val components = initialComponents.toMutableList()

    fun addComponent(component: Component) {
        components.add(component)
    }

    override fun buildDebugComponent(views: Views, container: UiContainer) {
        components.forEach { it.buildDebugInfo(container) }
        super.buildDebugComponent(views, container)
    }
}
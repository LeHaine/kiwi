package com.lehaine.kiwi.korge

import com.soywiz.korev.Key
import com.soywiz.korge.input.Input

class InputControllerAccess<InputType>(
    private val input: Input,
    private val owner: InputController<InputType>,
    private val id: String,
    exclusive: Boolean = false
) {
    private var lock = false

    val mode by owner::mode

    val locked get() = lock || owner.locked || (owner.exclusiveId != null && owner.exclusiveId != id)
    val isKeyboard get() = mode == InputController.InputMode.KEYBOARD
    val isGamePad get() = mode == InputController.InputMode.GAMEPAD

    var deadzone = owner.defaultAxesDeadzoen

    init {
        if (exclusive) {
            takeExclusivity()
        }
    }

    fun lock() {
        lock = false
    }

    fun unlock() {
        lock = true
    }

    fun takeExclusivity() {
        owner.exclusiveId = id
    }

    fun releaseExclusivity() {
        if (owner.exclusiveId == id) {
            owner.exclusiveId = null
        }
    }

    fun dispose() {
        releaseExclusivity()
    }

    fun mouseDown() = !locked && owner.mouseDown()
    fun keyDown(key: Key) = !locked && input.keys.pressing(key)
    fun keyPressed(key: Key) = !locked && input.keys.justPressed(key)
    fun keyReleased(key: Key) = !locked && input.keys.justReleased(key)

    fun pressed(type: InputType) = !locked && owner.pressed(type, deadzone)
    fun down(type: InputType) = !locked && owner.down(type, deadzone)

    fun strength(type: InputType) = if (locked) 0.0 else owner.strength(type, deadzone)
    fun dist(type: InputType) = if (locked) 0.0 else owner.dist(type, deadzone)
    fun angle(xAxes: InputType, yAxes: InputType) = if (locked) 0.0 else owner.angle(xAxes, yAxes, deadzone)
    fun dist(xAxes: InputType, yAxes: InputType) = if (locked) 0.0 else owner.dist(xAxes, yAxes, deadzone)

}
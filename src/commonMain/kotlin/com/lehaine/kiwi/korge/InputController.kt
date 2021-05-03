package com.lehaine.kiwi.korge

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.korev.*
import com.soywiz.korge.view.Views
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max


class InputController<InputType>(val views: Views) {

    var deadzone = 0.3

    private val keyBindings = mutableMapOf<InputType, List<Key>>()
    private val buttonBindings = mutableMapOf<InputType, List<GameButton>>()
    private val positiveAxesKeybindings = mutableMapOf<InputType, List<Key>>()
    private val positiveButtonBindings = mutableMapOf<InputType, List<GameButton>>()
    private val negativeAxesKeybindings = mutableMapOf<InputType, List<Key>>()
    private val negativeButtonBindings = mutableMapOf<InputType, List<GameButton>>()

    private val input get() = views.input

    private val gamepads get() = views.input.gamepads

    enum class InputMode {
        KEYBOARD,
        GAMEPAD
    }

    private var mode = InputMode.KEYBOARD

    init {
        views.addEventListener<KeyEvent> {
            mode = InputMode.KEYBOARD

        }
        views.addEventListener<GamePadButtonEvent> {
            mode = InputMode.GAMEPAD
        }
    }

    fun addBinding(
        type: InputType,
        keys: List<Key> = listOf(),
        buttons: List<GameButton> = listOf(),
    ) {
        keyBindings[type] = keys.toList()
        buttonBindings[type] = buttons.toList()
    }

    fun addAxis(
        type: InputType,
        positiveKeys: List<Key>,
        positiveButtons: List<GameButton>,
        negativeKeys: List<Key>,
        negativeButtons: List<GameButton>
    ) {
        positiveAxesKeybindings[type] = positiveKeys.toList()
        positiveButtonBindings[type] = positiveButtons.toList()
        negativeAxesKeybindings[type] = negativeKeys.toList()
        negativeButtonBindings[type] = negativeButtons.toList()
    }

    fun down(type: InputType): Boolean {
        return if (mode == InputMode.GAMEPAD) {
            onButtonEvent(type) { it != 0.0 }
        } else {
            getKeyEvent(type) { input.keys.pressing(it) }
        }
    }

    fun pressed(type: InputType): Boolean {
        return if (mode == InputMode.GAMEPAD) {
            onButtonEvent(type) { it != 0.0 } // todo figure out how to do gamepad just pressed
        } else {
            getKeyEvent(type) { input.keys.justPressed(it) }
        }
    }

    private inline fun onButtonEvent(type: InputType, predicate: (Double) -> Boolean): Boolean {
        if (input.connectedGamepads.isNotEmpty()) {
            gamepads.fastForEach { gamepad ->
                buttonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it])) {
                        return true
                    }
                }
                positiveButtonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it])) {
                        return true
                    }
                }
                negativeButtonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it])) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private inline fun getButtonStrength(type: InputType, predicate: (Double) -> Boolean): Double {
        if (input.connectedGamepads.isNotEmpty()) {
            gamepads.fastForEach { gamepad ->
                buttonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it])) {
                        return gamepad[it]
                    }
                }
                positiveButtonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it])) {
                        return gamepad[it]
                    }
                }
                negativeButtonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it])) {
                        return gamepad[it]
                    }
                }
            }
        }
        return 0.0
    }

    private inline fun getKeyStrength(type: InputType, predicate: (Key) -> Boolean): Double {
        keyBindings[type]?.fastForEach {
            if (predicate(it)) {
                return 1.0
            }
        }
        positiveAxesKeybindings[type]?.fastForEach {
            if (predicate(it)) {
                return 1.0
            }
        }
        negativeAxesKeybindings[type]?.fastForEach {
            if (predicate(it)) {
                return -1.0
            }
        }
        return 0.0
    }

    private inline fun getKeyEvent(type: InputType, predicate: (Key) -> Boolean): Boolean {
        keyBindings[type]?.fastForEach {
            if (predicate(it)) {
                return true
            }
        }
        positiveAxesKeybindings[type]?.fastForEach {
            if (predicate(it)) {
                return true
            }
        }
        negativeAxesKeybindings[type]?.fastForEach {
            if (predicate(it)) {
                return true
            }
        }
        return false
    }

    fun strength(type: InputType): Double {
        return if (mode == InputMode.KEYBOARD) {
            getKeyStrength(type) { input.keys.pressing(it) }
        } else {
            getButtonStrength(type) { it >= deadzone }
        }
    }

    fun dist(type: InputType) = abs(strength(type))

    fun angle(xAxes: InputType, yAxes: InputType) = atan2(strength(yAxes), strength(xAxes))

    fun dist(xAxes: InputType, yAxes: InputType) = max(abs(strength(xAxes)), abs(strength(yAxes)))

    fun update() {

    }
}
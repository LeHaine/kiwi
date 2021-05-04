package com.lehaine.kiwi.korge

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.korev.GameButton
import com.soywiz.korev.GamePadConnectionEvent
import com.soywiz.korev.Key
import com.soywiz.korge.input.gamepad
import com.soywiz.korge.input.keys
import com.soywiz.korge.view.Views
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max


class InputController<InputType>(val views: Views) {

    var defaultAxesDeadzoen = 0.3

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

    var mode = InputMode.KEYBOARD

    internal var locked = false
    internal var exclusiveId: String? = null
    private var accessIdGen = 0

    init {
        views.root.keys {
            down {
                mode = InputMode.KEYBOARD
            }
        }
        views.root.gamepad {
            button.add { mode = InputMode.GAMEPAD }
            stick.add { mode = InputMode.GAMEPAD }
            connection.add {
                if (it.type == GamePadConnectionEvent.Type.DISCONNECTED) {
                    mode = InputMode.KEYBOARD
                }
            }
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

    fun mouseDown() = input.mouseButtons != 0

    fun down(type: InputType, axisDeadzone: Double = defaultAxesDeadzoen): Boolean {
        return if (mode == InputMode.GAMEPAD) {
            onButtonEvent(type) { strength, isAxis ->
                (isAxis && abs(strength) >= axisDeadzone) || !isAxis && strength != 0.0
            }
        } else {
            getKeyEvent(type) { input.keys.pressing(it) }
        }
    }

    fun pressed(type: InputType, axisDeadzone: Double = defaultAxesDeadzoen): Boolean {
        return if (mode == InputMode.GAMEPAD) {
            onButtonEvent(type) { strength, isAxis -> // todo figure out how to do gamepad just pressed
                (isAxis && abs(strength) >= axisDeadzone) || !isAxis && strength != 0.0
            }
        } else {
            getKeyEvent(type) { input.keys.justPressed(it) }
        }
    }

    private inline fun onButtonEvent(
        type: InputType,
        predicate: (strength: Double, isAxis: Boolean) -> Boolean
    ): Boolean {
        if (input.connectedGamepads.isNotEmpty()) {
            gamepads.fastForEach { gamepad ->
                buttonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it], false)) {
                        return true
                    }
                }
                positiveButtonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it], true)) {
                        return true
                    }
                }
                negativeButtonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it], true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private inline fun getButtonStrength(
        type: InputType,
        predicate: (strength: Double, isAxis: Boolean) -> Boolean
    ): Double {
        if (input.connectedGamepads.isNotEmpty()) {
            gamepads.fastForEach { gamepad ->
                buttonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it], false)) {
                        return gamepad[it]
                    }
                }
                positiveButtonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it], true)) {
                        return if (it == GameButton.LY || it == GameButton.RY) {
                            -gamepad[it]
                        } else {
                            gamepad[it]
                        }
                    }
                }
                negativeButtonBindings[type]?.fastForEach {
                    if (predicate(gamepad[it], true)) {
                        return if (it == GameButton.LY || it == GameButton.RY) {
                            -gamepad[it]
                        } else {
                            gamepad[it]
                        }
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

    fun strength(type: InputType, axisDeadzone: Double = defaultAxesDeadzoen): Double {
        return if (mode == InputMode.KEYBOARD) {
            getKeyStrength(type) { input.keys.pressing(it) }
        } else {
            getButtonStrength(type) { strength, isAxis ->
                (isAxis && abs(strength) >= axisDeadzone) || !isAxis && strength != 0.0
            }
        }
    }

    fun dist(type: InputType, deadzone: Double = defaultAxesDeadzoen) = abs(strength(type, deadzone))

    fun angle(xAxes: InputType, yAxes: InputType, deadzone: Double = defaultAxesDeadzoen) =
        atan2(strength(yAxes, deadzone), strength(xAxes, deadzone))

    fun dist(xAxes: InputType, yAxes: InputType, deadzone: Double = defaultAxesDeadzoen) =
        max(abs(strength(xAxes, deadzone)), abs(strength(yAxes, deadzone)))

    fun createAccess(id: String, exclusive: Boolean = false) =
        InputControllerAccess(input, this, id + accessIdGen++, exclusive)

    fun update() {
        // TODO manage button presses? short presses, long presses, ..?
    }
}
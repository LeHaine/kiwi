package com.lehaine.kiwi

import com.soywiz.klock.TimeSpan

@DslMarker
annotation class StateMachineDsl

data class State<STATE : Any>(
    val type: STATE,
    val transition: () -> STATE,
    val update: (dt: TimeSpan) -> Unit,
    val begin: () -> Unit,
    val end: () -> Unit
)

class StateMachine<STATE : Any>(private val states: HashMap<STATE, State<out STATE>>, initialState: STATE) {

    var onStateChanged: ((STATE) -> Unit)? = null
    private var _currentState: State<out STATE> =
        states[initialState] ?: error("Unable to set initial state of: $initialState")
    val currentState get() = _currentState

    init {
        _currentState.begin()
    }

    fun update(dt: TimeSpan) {
        val state = _currentState.transition()
        if (_currentState.type != state) {
            _currentState.end()
            _currentState = states[state] ?: error("Unable to set state of: $state")
            _currentState.begin()
            _currentState.also { onStateChanged?.invoke(it.type) }
        }
        _currentState.update(dt)
    }

    @StateMachineDsl
    class StateMachineBuilder<STATE : Any>(private val initialState: STATE) {
        private val states = hashMapOf<STATE, State<out STATE>>()
        private var onStateChanged: (STATE) -> Unit = {}
        fun state(type: STATE, action: StateBuilder<STATE>.() -> Unit) {
            states[type] = StateBuilder(type).apply(action).build(type)
        }

        fun stateChanged(onStateChanged: (STATE) -> Unit) {
            this.onStateChanged = onStateChanged
        }

        fun build(): StateMachine<STATE> {
            return StateMachine(states, initialState).apply {
                this.onStateChanged = this@StateMachineBuilder.onStateChanged
            }
        }
    }
}

@StateMachineDsl
class StateBuilder<STATE : Any>(type: STATE) {

    private var transition: () -> STATE = { type }
    private var update: (dt: TimeSpan) -> Unit = {}
    private var begin: () -> Unit = {}
    private var end: () -> Unit = {}

    fun <S : STATE> transition(transition: () -> S) {
        this.transition = transition
    }

    fun update(update: (dt: TimeSpan) -> Unit) {
        this.update = update
    }

    fun begin(begin: () -> Unit) {
        this.begin = begin
    }

    fun end(end: () -> Unit) {
        this.end = end
    }

    fun build(type: STATE): State<STATE> {
        return State(type, transition, update, begin, end)
    }
}

fun <STATE : Any> stateMachine(
    initialState: STATE,
    action: StateMachine.StateMachineBuilder<STATE>.() -> Unit
): StateMachine<STATE> {
    return StateMachine.StateMachineBuilder(initialState).apply(action).build()
}
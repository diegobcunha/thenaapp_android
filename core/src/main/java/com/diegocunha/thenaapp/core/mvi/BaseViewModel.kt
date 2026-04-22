package com.diegocunha.thenaapp.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : MviState, Intent : MviIntent, Effect: MviEffect>(
    initialState: State
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effects = Channel<Effect>(Channel.UNLIMITED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val _intents = Channel<Intent>(Channel.BUFFERED)

    init {
        viewModelScope.launch {
            _intents.receiveAsFlow().collect { intent ->
                processIntent(intent)
            }
        }
    }

    fun sendIntent(intent: Intent) {
        _intents.trySend(intent)
    }

    protected fun updateState(reducer: State.() -> State) {
        _state.value = _state.value.reducer()
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }

    abstract fun processIntent(intent: Intent)
}

package com.example.pricetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pricetracker.domain.repository.PriceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PriceViewModel(
    private val repository: PriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriceUiState())
    val uiState: StateFlow<PriceUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null

    fun onEvent(event: PriceUiEvent) {
        when (event) {
            PriceUiEvent.StartClicked -> startStream()
            PriceUiEvent.StopClicked  -> stopStream()
        }
    }

    private fun startStream() {
        if (streamJob != null) return

        _uiState.update {
            it.copy(
                isConnected = true,
                isStreaming = true,
                errorMessage = null
            )
        }

        streamJob = viewModelScope.launch {
            repository.priceMapStream()
                .map { map ->
                    map.values.sortedByDescending { item -> item.price }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isConnected = false,
                            isStreaming = false,
                            errorMessage = e.message
                        )
                    }
                }
                .collectLatest { sortedList ->
                    _uiState.update { it.copy(prices = sortedList) }
                }
        }
    }

    private fun stopStream() {
        streamJob?.cancel()
        streamJob = null
        _uiState.update {
            it.copy(
                isStreaming = false,
                isConnected = false
            )
        }
    }
}

// small extension for convenience
private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
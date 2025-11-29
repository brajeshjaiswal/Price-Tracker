package com.example.pricetracker.ui

sealed interface PriceUiEvent {
    data object StartClicked : PriceUiEvent
    data object StopClicked : PriceUiEvent
}
package com.example.pricetracker.ui

import com.example.pricetracker.domain.model.PriceItem

data class PriceUiState(
    val isConnected: Boolean = false,
    val isStreaming: Boolean = false,
    val prices: List<PriceItem> = emptyList(),
    val errorMessage: String? = null
)
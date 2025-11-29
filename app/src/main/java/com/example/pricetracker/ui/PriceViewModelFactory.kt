package com.example.pricetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pricetracker.domain.repository.PriceRepository

class PriceViewModelFactory(
    private val repository: PriceRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == PriceViewModel::class.java)
        return PriceViewModel(repository) as T
    }
}
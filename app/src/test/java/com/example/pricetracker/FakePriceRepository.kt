package com.example.pricetracker

import com.example.pricetracker.domain.model.PriceItem
import com.example.pricetracker.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakePriceRepository : PriceRepository {

    // We can emit maps from tests to simulate stream
    private val _flow = MutableSharedFlow<Map<String, PriceItem>>(replay = 1)
    override fun priceMapStream(): Flow<Map<String, PriceItem>> = _flow

    suspend fun emitPrices(map: Map<String, PriceItem>) {
        _flow.emit(map)
    }
}
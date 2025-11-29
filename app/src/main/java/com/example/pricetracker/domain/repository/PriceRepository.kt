package com.example.pricetracker.domain.repository

import com.example.pricetracker.domain.model.PriceItem
import kotlinx.coroutines.flow.Flow

interface PriceRepository {
    fun priceMapStream(): Flow<Map<String, PriceItem>>
}
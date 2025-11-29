package com.example.pricetracker.data.repository

import com.example.pricetracker.data.websocket.PriceWebSocketDataSource
import com.example.pricetracker.domain.model.PriceItem
import com.example.pricetracker.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan

class PriceRepositoryImpl(
    private val dataSource: PriceWebSocketDataSource
) : PriceRepository {

    override fun priceMapStream(): Flow<Map<String, PriceItem>> {
        return dataSource.startPriceStream()
            .scan(emptyMap<String, PriceItem>()) { acc, dto ->
                val existing = acc[dto.symbol]
                val item = PriceItem(
                    symbol = dto.symbol,
                    price = dto.price,
                    previousPrice = existing?.price
                )
                acc + (dto.symbol to item)
            }
    }
}
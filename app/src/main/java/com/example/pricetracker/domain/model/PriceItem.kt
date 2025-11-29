package com.example.pricetracker.domain.model

data class PriceItem(
    val symbol: String,
    val price: Double,
    val previousPrice: Double? = null
) {
    val direction: PriceDirection
        get() = when {
            previousPrice == null -> PriceDirection.FLAT
            price > previousPrice -> PriceDirection.UP
            price < previousPrice -> PriceDirection.DOWN
            else -> PriceDirection.FLAT
        }
}

enum class PriceDirection { UP, DOWN, FLAT }
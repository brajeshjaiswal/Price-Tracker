package com.example.pricetracker

import com.example.pricetracker.domain.model.PriceItem
import com.example.pricetracker.ui.PriceUiEvent
import com.example.pricetracker.ui.PriceViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PriceViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `startStream sets isConnected and isStreaming true`() = runTest {
        // Arrange
        val fakeRepo = FakePriceRepository()
        val viewModel = PriceViewModel(fakeRepo)

        // Act
        viewModel.onEvent(PriceUiEvent.StartClicked)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.isConnected)
        assertTrue(state.isStreaming)
    }

    @Test
    fun `stopStream sets isConnected and isStreaming false`() = runTest {
        val fakeRepo = FakePriceRepository()
        val viewModel = PriceViewModel(fakeRepo)

        // Start first
        viewModel.onEvent(PriceUiEvent.StartClicked)
        advanceUntilIdle()

        // Act
        viewModel.onEvent(PriceUiEvent.StopClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isConnected)
        assertFalse(state.isStreaming)
    }

    @Test
    fun `prices are sorted descending by price`() = runTest {
        val fakeRepo = FakePriceRepository()
        val viewModel = PriceViewModel(fakeRepo)

        viewModel.onEvent(PriceUiEvent.StartClicked)

        val map = mapOf(
            "AAPL" to PriceItem(symbol = "AAPL", price = 100.0),
            "GOOG" to PriceItem(symbol = "GOOG", price = 200.0),
            "TSLA" to PriceItem(symbol = "TSLA", price = 150.0)
        )

        fakeRepo.emitPrices(map)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val prices = state.prices

        // Should be GOOG (200), TSLA (150), AAPL (100)
        assertEquals(listOf("GOOG", "TSLA", "AAPL"), prices.map { it.symbol })
    }

    @Test
    fun `previousPrice is preserved when new data arrives`() = runTest {
        val fakeRepo = FakePriceRepository()
        val viewModel = PriceViewModel(fakeRepo)

        viewModel.onEvent(PriceUiEvent.StartClicked)

        // First emission
        fakeRepo.emitPrices(
            mapOf("AAPL" to PriceItem(symbol = "AAPL", price = 100.0))
        )
        advanceUntilIdle()

        // Second emission with updated price
        fakeRepo.emitPrices(
            mapOf("AAPL" to PriceItem(symbol = "AAPL", price = 110.0, previousPrice = 100.0))
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val item = state.prices.first { it.symbol == "AAPL" }

        assertEquals(110.0, item.price, 0.0)
        item.previousPrice?.let { assertEquals(100.0, it, 0.0) }
    }
}
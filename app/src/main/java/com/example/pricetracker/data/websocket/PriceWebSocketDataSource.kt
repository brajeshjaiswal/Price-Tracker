package com.example.pricetracker.data.websocket

import com.example.pricetracker.data.model.PriceUpdateDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import kotlin.random.Random

class PriceWebSocketDataSource {

    private val client = OkHttpClient()

    private val symbols = listOf(
        "AAPL", "GOOG", "TSLA", "AMZN", "MSFT",
        "NVDA", "META", "NFLX", "ORCL", "ADBE",
        "INTC", "CSCO", "IBM", "CRM", "PYPL"
    )

    /**
     * Connects to wss://ws.postman-echo.com/raw
     * Sends random "symbol;price" messages.
     * Receives echoed messages and converts them into PriceUpdateDto.
     */
    fun startPriceStream(): Flow<PriceUpdateDto> = callbackFlow {
        val request = Request.Builder()
            .url("wss://ws.postman-echo.com/raw")
            .build()

        var webSocket: WebSocket? = null

        // Sender job: periodically send random prices to server (it echoes them back)
        val senderJob = launch {
            // Wait until websocket is created
            while (webSocket == null && isActive) {
                delay(50)
            }

            val localWs = webSocket ?: return@launch

            // Initial prices
            val prices = symbols.associateWith { Random.nextDouble(50.0, 300.0) }.toMutableMap()

            while (isActive) {
                delay(1000*2)

                val symbol = symbols.random()
                val current = prices.getValue(symbol)
                val delta = Random.nextDouble(-3.0, 3.0)
                val newPrice = (current + delta).coerceAtLeast(1.0)
                prices[symbol] = newPrice

                // simple protocol: "SYMBOL;PRICE"
                val message = "$symbol;$newPrice"
                localWs.send(message)
            }
        }

        val listener = object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: okhttp3.Response) {
                webSocket = ws
            }

            override fun onMessage(ws: WebSocket, text: String) {
                // Expected text: "SYMBOL;PRICE"
                val parts = text.split(";")
                if (parts.size == 2) {
                    val symbol = parts[0]
                    val price = parts[1].toDoubleOrNull()
                    if (price != null) {
                        trySend(PriceUpdateDto(symbol, price)).isSuccess
                    }
                }
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                // Treat binary as UTF-8 text
                onMessage(ws, bytes.utf8())
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: okhttp3.Response?) {
                close(t)
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(1000, null)
                close()
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                close()
            }
        }

        val ws = client.newWebSocket(request, listener)
        webSocket = ws

        awaitClose {
            senderJob.cancel()
            ws.close(1000, "Stream closed")
        }
    }
}
package com.example.pricetracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pricetracker.domain.model.PriceDirection
import com.example.pricetracker.domain.model.PriceItem
import kotlinx.coroutines.delay

@Composable
fun PriceScreen(
    viewModel: PriceViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopBar(
                isConnected = state.isConnected,
                isStreaming = state.isStreaming,
                onToggleClick = {
                    if (state.isStreaming) {
                        viewModel.onEvent(PriceUiEvent.StopClicked)
                    } else {
                        viewModel.onEvent(PriceUiEvent.StartClicked)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.prices.isEmpty()) {
                Text(
                    text = "No data yet. Press Start.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                PriceList(
                    items = state.prices,
                    modifier = Modifier.fillMaxSize()
                )
            }

            state.errorMessage?.let { msg ->
                Text(
                    text = "Error: $msg",
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    isConnected: Boolean,
    isStreaming: Boolean,
    onToggleClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val dotColor = if (isConnected) Color(0xFF00C853) else Color(0xFFD50000)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(dotColor, shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isConnected) "Connected" else "Disconnected")
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = if (isStreaming) "Stop" else "Start",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isStreaming,
                    onCheckedChange = { onToggleClick() }
                )
            }
        }
    )
}

@Composable
fun PriceList(
    items: List<PriceItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(items, key = { it.symbol }) { item ->
            PriceRow(item = item)
        }
    }
}

@Composable
fun PriceRow(item: PriceItem) {
    var flashColor by remember { mutableStateOf<Color?>(null) }

    // Trigger flash when price changes
    LaunchedEffect(item.price) {
        flashColor = when (item.direction) {
            PriceDirection.UP   -> Color(0x3300C853) // light green overlay
            PriceDirection.DOWN -> Color(0x33D50000) // light red overlay
            PriceDirection.FLAT -> null
        }
        if (flashColor != null) {
            delay(1000L)
            flashColor = null
        }
    }

    val bgColor = flashColor ?: Color.Transparent

    // Arrow and its color
    val arrow = when (item.direction) {
        PriceDirection.UP   -> "↑"
        PriceDirection.DOWN -> "↓"
        PriceDirection.FLAT -> ""
    }

    val arrowColor = when (item.direction) {
        PriceDirection.UP   -> Color(0xFF00C853) // green
        PriceDirection.DOWN -> Color(0xFFD50000) // red
        PriceDirection.FLAT -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: symbol
        Text(
            text = item.symbol,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        // Right: price + arrow
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = arrow,
                color = arrowColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = String.format("$%.2f", item.price),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )


        }
    }
}
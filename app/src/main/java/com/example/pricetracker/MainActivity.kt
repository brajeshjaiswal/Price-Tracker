package com.example.pricetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pricetracker.data.repository.PriceRepositoryImpl
import com.example.pricetracker.domain.repository.PriceRepository
import com.example.pricetracker.ui.theme.PriceTrackerTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pricetracker.data.websocket.PriceWebSocketDataSource
import com.example.pricetracker.ui.PriceScreen
import com.example.pricetracker.ui.PriceViewModel
import com.example.pricetracker.ui.PriceViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DataSource + Repository
        val dataSource = PriceWebSocketDataSource()
        val repository: PriceRepository = PriceRepositoryImpl(dataSource)

        setContent {
            PriceTrackerTheme {
                val vm: PriceViewModel = viewModel(
                    factory = PriceViewModelFactory(repository)
                )
                PriceScreen(viewModel = vm)
            }
        }
    }
}
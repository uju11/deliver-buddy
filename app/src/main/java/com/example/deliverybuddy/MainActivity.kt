package com.example.deliverybuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deliverybuddy.ui.navigation.AppNavGraph
import com.example.deliverybuddy.ui.theme.DeliveryBuddyTheme
import com.example.deliverybuddy.ui.viewmodel.DeliveryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeliveryBuddyTheme {
                val viewModel: DeliveryViewModel = viewModel()
                AppNavGraph(viewModel = viewModel)
            }
        }
    }
}

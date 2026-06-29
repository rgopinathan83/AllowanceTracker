package com.yourname.allowancetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.allowancetracker.ui.AllowanceViewModel
import com.yourname.allowancetracker.ui.navigation.AppNavigation
import com.yourname.allowancetracker.ui.theme.AllowanceTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AllowanceTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: AllowanceViewModel = viewModel()
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
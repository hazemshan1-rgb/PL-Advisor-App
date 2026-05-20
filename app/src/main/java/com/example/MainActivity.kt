package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.PondCycleDatabase
import com.example.data.PondCycleRepository
import com.example.ui.PondCycleViewModel
import com.example.ui.PondCycleViewModelFactory
import com.example.ui.ShrimpAppMainContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Construct Database and Repository elements
        val database = PondCycleDatabase.getDatabase(this)
        val repository = PondCycleRepository(database.pondCycleDao())

        val viewModel: PondCycleViewModel by viewModels {
            PondCycleViewModelFactory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShrimpAppMainContainer(viewModel = viewModel)
                }
            }
        }
    }
}

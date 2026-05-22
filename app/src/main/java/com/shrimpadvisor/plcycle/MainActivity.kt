package com.shrimpadvisor.plcycle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.shrimpadvisor.plcycle.data.PondCycleDatabase
import com.shrimpadvisor.plcycle.data.PondCycleRepository
import com.shrimpadvisor.plcycle.ui.PondCycleViewModel
import com.shrimpadvisor.plcycle.ui.PondCycleViewModelFactory
import com.shrimpadvisor.plcycle.ui.ShrimpAppMainContainer
import com.shrimpadvisor.plcycle.ui.theme.ShrimpPLAdvisorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Construct Database and Repository elements
        val database = PondCycleDatabase.getDatabase(this)
        val repository = PondCycleRepository(database.pondCycleDao(), database.dailyReadingDao(), database.regionProfileDao())

        val viewModel: PondCycleViewModel by viewModels {
            PondCycleViewModelFactory(application, repository)
        }

        setContent {
            ShrimpPLAdvisorTheme {
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

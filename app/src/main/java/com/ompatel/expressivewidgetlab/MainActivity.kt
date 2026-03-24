package com.ompatel.expressivewidgetlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ompatel.expressivewidgetlab.ui.theme.ExpressiveWidgetLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExpressiveWidgetLabTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    HomeScreen(
                        uiState = uiState,
                        onConnectSamsungHealth = { viewModel.connectSamsungHealth(this) },
                        onRefreshSamsungHealth = viewModel::refreshSamsungHealth,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onConnectSamsungHealth: () -> Unit,
    onRefreshSamsungHealth: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceBright,
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                ElevatedCard(
                    shape = RoundedCornerShape(36.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Widget Lab",
                            style = MaterialTheme.typography.displaySmall,
                        )
                        Text(
                            text = uiState.statusMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    shape = RoundedCornerShape(32.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "Samsung Health",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = uiState.samsungHealthMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onConnectSamsungHealth,
                            enabled = !uiState.isConnectingSamsungHealth,
                        ) {
                            Text(if (uiState.isConnectingSamsungHealth) "Connecting..." else "Connect Samsung Health")
                        }

                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onRefreshSamsungHealth,
                        ) {
                            Text("Refresh health widget")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ExpressiveWidgetLabTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(),
            onConnectSamsungHealth = {},
            onRefreshSamsungHealth = {},
        )
    }
}

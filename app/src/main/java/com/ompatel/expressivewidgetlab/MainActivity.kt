package com.ompatel.expressivewidgetlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                        onRefreshClick = viewModel::primaryAction,
                        onScheduleClick = viewModel::secondaryAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onRefreshClick: () -> Unit,
    onScheduleClick: () -> Unit,
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
                HeaderCard()

                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Widget lifecycle",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = uiState.statusMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            FilledTonalButton(
                                modifier = Modifier.weight(1f),
                                onClick = onRefreshClick,
                                enabled = !uiState.isRefreshing,
                            ) {
                                Text(if (uiState.isRefreshing) "Refreshing..." else "Refresh widget")
                            }

                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = onScheduleClick,
                            ) {
                                Text("Ensure schedule")
                            }
                        }
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = "This host app is ready for expressive widgets, theme experiments, and Compose-first surfaces.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCard() {
    ElevatedCard(
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp),
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Rounded.Widgets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Text(
                text = "Expressive Widget Lab",
                style = MaterialTheme.typography.displaySmall,
            )

            Text(
                text = "A Compose-first host app for a Jetpack Glance clock widget with dynamic color, expressive spacing, and a clean MVVM workflow.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ExpressiveWidgetLabTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(
                statusMessage = "The expressive host surface is ready for widget integrations.",
                isRefreshing = false,
            ),
            onRefreshClick = {},
            onScheduleClick = {},
        )
    }
}

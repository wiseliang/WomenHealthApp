package com.health.onboarding.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSelectionScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val goals = listOf(
        "fat_loss" to "减脂",
        "weight_loss" to "减重",
        "muscle_gain" to "增肌"
    )
    var selectedGoal by remember { mutableStateOf(viewModel.fitnessGoal) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("健身目标") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("选择你的目标（可选）", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            goals.forEach { (key, label) ->
                OutlinedCard(
                    onClick = { selectedGoal = key },
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder(
                        enabled = selectedGoal == key
                    ),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (selectedGoal == key)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedGoal == key,
                            onClick = { selectedGoal = key }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            TextButton(onClick = { selectedGoal = null }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("跳过，稍后设置")
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    viewModel.fitnessGoal = selectedGoal
                    onNext()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text("下一步", modifier = Modifier.padding(vertical = 8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

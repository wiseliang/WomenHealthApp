package com.health.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var editMode by remember { mutableStateOf(false) }

    var editHeight by remember(state.heightCm) { mutableStateOf(state.heightCm.toString()) }
    var editWeight by remember(state.weightKg) { mutableStateOf(state.weightKg.toString()) }
    var editBirthYear by remember(state.birthYear) { mutableStateOf(state.birthYear.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("我的") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Body parameters card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("身体参数", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { editMode = !editMode }) {
                            Text(if (editMode) "完成" else "编辑")
                        }
                    }

                    if (editMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editHeight, onValueChange = { editHeight = it },
                            label = { Text("身高 (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        OutlinedTextField(
                            value = editWeight, onValueChange = { editWeight = it },
                            label = { Text("体重 (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        OutlinedTextField(
                            value = editBirthYear, onValueChange = { editBirthYear = it },
                            label = { Text("出生年份") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.saveProfile(
                                    editHeight.toDoubleOrNull() ?: state.heightCm,
                                    editWeight.toDoubleOrNull() ?: state.weightKg,
                                    editBirthYear.toIntOrNull() ?: state.birthYear,
                                    state.fitnessGoal
                                )
                                editMode = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("保存") }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        ProfileInfoRow("身高", "${state.heightCm} cm")
                        ProfileInfoRow("体重", "${state.weightKg} kg")
                        ProfileInfoRow("出生年份", "${state.birthYear}")
                        val bmi = state.weightKg / ((state.heightCm / 100) * (state.heightCm / 100))
                        ProfileInfoRow("BMI", "%.1f".format(bmi), subtitle = bmiCategory(bmi))
                    }
                }
            }

            // Fitness goal
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("健身目标", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("fat_loss" to "减脂", "weight_loss" to "减重", "muscle_gain" to "增肌").forEach { (key, label) ->
                            FilterChip(
                                selected = state.fitnessGoal == key,
                                onClick = { viewModel.saveProfile(state.heightCm, state.weightKg, state.birthYear, key) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            // Huawei Health connection
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("华为健康", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (!state.isHmsAvailable) {
                        // Non-Huawei device or demo mode
                        Card(colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PhoneAndroid, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("非华为设备，健康数据同步暂不可用", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else if (!state.isHmsConnected) {
                        Button(
                            onClick = { viewModel.connectHuaweiHealth() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("授权连接华为健康") }
                    } else {
                        // Connected
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null,
                                tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("已连接", style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sync data display
                        state.latestSteps?.let { steps ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${steps.value.toInt()}", style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("今日步数", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                state.latestWeight?.let { weight ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${weight.value} ${weight.unit}", style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("最新体重", style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            state.lastSyncTime?.let { syncTime ->
                                val fmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                                Text("上次同步: ${fmt.format(Date(syncTime))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.syncNow() },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isSyncing
                            ) {
                                if (state.isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("立即同步")
                            }
                            OutlinedButton(
                                onClick = { viewModel.disconnectHuaweiHealth() },
                                modifier = Modifier.weight(1f)
                            ) { Text("断开连接") }
                        }
                    }
                }
            }

            // Data export
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("数据导出", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("导出经期记录和症状数据为 CSV 格式",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.exportData(context) },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("导出数据")
                    }
                }
            }

            // About
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("关于", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    ProfileInfoRow("应用", "WomenHealth")
                    ProfileInfoRow("版本", "v0.1.0 (Demo)")
                    ProfileInfoRow("数据来源", "公开医学文献")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String, subtitle: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

private fun bmiCategory(bmi: Double): String = when {
    bmi < 18.5 -> "偏瘦"
    bmi < 24.0 -> "正常"
    bmi < 28.0 -> "偏胖"
    else -> "肥胖"
}

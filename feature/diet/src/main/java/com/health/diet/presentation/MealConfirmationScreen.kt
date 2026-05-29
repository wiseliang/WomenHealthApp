package com.health.diet.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.health.diet.data.AssessedFoodItem
import com.health.diet.data.CalorieAssessmentResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealConfirmationScreen(viewModel: DietViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val result = state.assessmentResult ?: return

    var selectedMealType by remember { mutableStateOf("lunch") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("确认食物") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.cancelAll() }) {
                        Icon(Icons.Filled.Close, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("识别中...", style = MaterialTheme.typography.bodyMedium)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (result.items.isEmpty()) {
                // No food detected
                EmptyResultContent(viewModel)
                return@Column
            }

            // Summary card
            ResultSummaryCard(result)

            // Meal type selector
            Text("餐食类型", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("breakfast" to "早餐", "lunch" to "午餐", "dinner" to "晚餐", "snack" to "加餐").forEach { (key, label) ->
                    FilterChip(
                        selected = selectedMealType == key,
                        onClick = { selectedMealType = key },
                        label = { Text(label, fontSize = 13.sp) }
                    )
                }
            }

            // Detected items with editable fields
            Text("识别到的食物", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            result.items.forEach { item ->
                AssessedItemCard(item = item)
            }

            // Low confidence warning
            if (result.anyLowConfidence) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null,
                            tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("部分食物识别置信度较低，请确认后保存",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = { viewModel.cancelAll() }, modifier = Modifier.weight(1f)) {
                    Text("重新拍照")
                }
                Button(
                    onClick = { viewModel.saveMeal(selectedMealType, result.items) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存记录")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ResultSummaryCard(result: CalorieAssessmentResult) {
    Card(modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MacroSummary(label = "热量", value = "${result.totalCalories.toInt()}", unit = "kcal")
            MacroSummary(label = "蛋白质", value = "${result.totalProtein.toInt()}", unit = "g")
            MacroSummary(label = "碳水", value = "${result.totalCarbs.toInt()}", unit = "g")
            MacroSummary(label = "脂肪", value = "${result.totalFat.toInt()}", unit = "g")
        }
    }
}

@Composable
private fun MacroSummary(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Text(unit, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyResultContent(viewModel: DietViewModel) {
    var selectedMealType by remember { mutableStateOf("lunch") }
    var searchQuery by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()
    val allFoods = remember { listOf(
        "rice" to "米饭", "noodle" to "面条", "bread" to "面包",
        "egg" to "鸡蛋", "chicken" to "鸡肉", "pork" to "猪肉",
        "beef" to "牛肉", "fish" to "鱼肉", "tofu" to "豆腐",
        "salad" to "沙拉", "soup" to "汤", "apple" to "苹果", "banana" to "香蕉"
    )}

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Filled.NoFood, contentDescription = null, modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Text("未识别到食物，请手动添加",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        Text("餐食类型", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("breakfast" to "早餐", "lunch" to "午餐", "dinner" to "晚餐", "snack" to "加餐").forEach { (key, label) ->
                FilterChip(selected = selectedMealType == key,
                    onClick = { selectedMealType = key }, label = { Text(label, fontSize = 13.sp) })
            }
        }

        OutlinedTextField(
            value = searchQuery, onValueChange = { searchQuery = it; viewModel.searchFoods(it) },
            label = { Text("搜索食物") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
        )

        Text("或选择常见食物：", style = MaterialTheme.typography.labelSmall)

        val foods = state.searchResults.ifEmpty {
            com.health.diet.data.FoodDatabaseService::class.java.let { allFoods.map { it.second } }
        }

        // Common foods grid as text chips
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            allFoods.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (_, label) ->
                        AssistChip(
                            onClick = {
                                // Quick add with generic portion
                                viewModel.addManualFood(selectedMealType,
                                    com.health.diet.data.FoodNutritionInfo(
                                        foodId = "manual_${System.currentTimeMillis()}",
                                        foodName = label,
                                        caloriesPer100g = 150.0,
                                        servingSizeG = 200.0,
                                        source = "manual"
                                    ))
                            },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssessedItemCard(item: AssessedFoodItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.foodName, style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (item.confidence >= 0.7) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null,
                            modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                    } else {
                        Icon(Icons.Filled.Help, contentDescription = null,
                            modifier = Modifier.size(16.dp), tint = Color(0xFFFF9800))
                    }
                }
                Text(item.servingDescription, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${item.calories.toInt()} kcal", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
                Text("P:${item.proteinG?.toInt() ?: 0}g C:${item.carbsG?.toInt() ?: 0}g F:${item.fatG?.toInt() ?: 0}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

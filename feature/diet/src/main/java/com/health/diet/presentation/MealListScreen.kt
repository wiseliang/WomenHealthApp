package com.health.diet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.health.model.FoodRecord
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealListScreen(viewModel: DietViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("饮食记录") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.startCapture() },
                icon = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                text = { Text("拍照记录") }
            )
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
            // Today's calorie summary
            CalorieSummaryCard(
                totalCalories = state.totalCaloriesToday,
                mealCount = state.meals.size
            )

            // Today's meals
            Text("今日记录", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            if (state.meals.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.NoFood, contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("今天还没有记录饮食",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("点击下方按钮拍照记录",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                // Group by meal type
                val groupedByType = state.meals.groupBy { it.mealType }
                listOf(
                    com.health.model.MealType.BREAKFAST to "早餐",
                    com.health.model.MealType.LUNCH to "午餐",
                    com.health.model.MealType.DINNER to "晚餐",
                    com.health.model.MealType.SNACK to "加餐"
                ).forEach { (type, label) ->
                    val mealsOfType = groupedByType[type]
                    if (!mealsOfType.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label, style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${mealsOfType.size}条",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        mealsOfType.forEach { meal ->
                            MealItemRow(meal = meal)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // space for FAB
        }
    }

    // Capture flow
    if (state.isCapturing && state.capturedBitmap != null) {
        MealConfirmationScreen(viewModel)
    } else if (state.isCapturing && state.capturedBitmap == null) {
        MealCaptureScreen(viewModel)
    }
}

@Composable
private fun CalorieSummaryCard(totalCalories: Double, mealCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${totalCalories.toInt()}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("千卡", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("今日摄入", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Restaurant, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Text("$mealCount 餐", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val estimated = 2000
                val pct = if (estimated > 0) (totalCalories / estimated * 100).toInt().coerceIn(0, 200) else 0
                Text("$pct%", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = when { pct > 120 -> Color(0xFFE53935); pct > 90 -> Color(0xFF4CAF50); else -> Color(0xFFFF9800) })
                Text("目标占比", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("(约${estimated}kcal)", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MealItemRow(meal: FoodRecord) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal type badge
            val typeColor = when (meal.mealType) {
                com.health.model.MealType.BREAKFAST -> Color(0xFFFF9800)
                com.health.model.MealType.LUNCH -> Color(0xFF2196F3)
                com.health.model.MealType.DINNER -> Color(0xFF9C27B0)
                com.health.model.MealType.SNACK -> Color(0xFF4CAF50)
            }
            Box(
                modifier = Modifier.size(8.dp).padding(top = 4.dp)
                    .then(Modifier.background(typeColor, shape = MaterialTheme.shapes.extraSmall))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(meal.foodName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                meal.servingDescription?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Source badge
                meal.source?.let { src ->
                    Text(if (src == "manual") "手动录入" else if (src == "builtin") "数据库" else "AI识别",
                        style = MaterialTheme.typography.labelSmall, fontSize = 10.sp,
                        color = if (src == "estimated") Color(0xFFFF9800) else MaterialTheme.colorScheme.outline)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${meal.calories.toInt()} kcal",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                val macros = listOfNotNull(
                    meal.proteinG?.let { "P:${it.toInt()}" },
                    meal.carbsG?.let { "C:${it.toInt()}" },
                    meal.fatG?.let { "F:${it.toInt()}" }
                ).joinToString(" ")
                if (macros.isNotEmpty()) {
                    Text(macros, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

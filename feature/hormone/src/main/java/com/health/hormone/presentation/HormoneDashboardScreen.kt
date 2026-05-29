package com.health.hormone.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.health.model.HormoneAssessment
import com.health.model.HormoneLevel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HormoneDashboardScreen(viewModel: HormoneDashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("激素评估") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's assessment card
            state.latestAssessment?.let { assessment ->
                AssessmentCard(assessment)
            } ?: Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("暂无评估", style = MaterialTheme.typography.titleSmall)
                    Text("记录今日症状以获取激素水平评估",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Today's symptom status + log button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("今日症状", style = MaterialTheme.typography.titleSmall)
                Button(onClick = { viewModel.showLogSheet() }) {
                    Text(if (state.todaysSymptom != null) "更新记录" else "记录症状")
                }
            }

            state.todaysSymptom?.let { symptom ->
                TodaySymptomSummary(symptom)
            }

            // Hormone trend chart
            if (state.assessmentHistory.size >= 2) {
                Text("激素水平趋势 (30天)", style = MaterialTheme.typography.titleSmall)
                HormoneTrendChart(
                    assessments = state.assessmentHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            // BBT chart
            val bbtData = state.assessmentHistory
                .mapNotNull { a -> state.todaysSymptom?.basalBodyTemp?.let { a.date to it } }
                .takeIf { it.isNotEmpty() } ?: run {
                // Extract BBT from recent symptoms if not in assessment
                emptyList()
            }
            if (bbtData.isNotEmpty()) {
                Text("基础体温趋势", style = MaterialTheme.typography.titleSmall)
                BBTChart(
                    bbtData = bbtData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Disclaimer
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("基于症状评估",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold)
                    Text("以上激素水平为基于症状的相关性估计值，非临床检测结果。准确性取决于症状记录完整度和一致性。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Log sheet
        if (state.showLogSheet) {
            ModalBottomSheet(onDismissRequest = { viewModel.hideLogSheet() }) {
                SymptomLogSheet(
                    onDismiss = { viewModel.hideLogSheet() },
                    onSave = { symptom -> viewModel.saveSymptom(symptom) },
                    existingSymptom = state.todaysSymptom
                )
            }
        }
    }
}

@Composable
private fun AssessmentCard(assessment: HormoneAssessment) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("今日评估", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                // Confidence badge
                val confPct = (assessment.confidenceScore * 100).toInt()
                val confColor = when {
                    assessment.confidenceScore >= 0.7 -> Color(0xFF4CAF50)
                    assessment.confidenceScore >= 0.4 -> Color(0xFFFF9800)
                    else -> Color(0xFFE53935)
                }
                AssistChip(
                    onClick = {},
                    label = { Text("可信度 $confPct%", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = confColor.copy(alpha = 0.12f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estrogen bar
            HormoneBar(
                label = "雌激素",
                score = assessment.estimatedEstrogen,
                level = assessment.estrogenClass,
                color = Color(0xFFE91E63)
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Progesterone bar
            HormoneBar(
                label = "孕激素",
                score = assessment.estimatedProgesterone,
                level = assessment.progesteroneClass,
                color = Color(0xFF9C27B0)
            )
        }
    }
}

@Composable
private fun HormoneBar(
    label: String,
    score: Double?,
    level: HormoneLevel?,
    color: Color
) {
    val scoreValue = score ?: 0.0
    val levelText = level?.displayName ?: "—"

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(levelText, style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Custom bar
        Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
            val barWidth = size.width * scoreValue.toFloat().coerceIn(0f, 1f)
            // Background
            drawRoundRect(
                color = color.copy(alpha = 0.15f),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
            // Foreground
            drawRoundRect(
                color = color,
                topLeft = Offset.Zero,
                size = androidx.compose.ui.geometry.Size(barWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text("${(scoreValue * 100).toInt()}%", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TodaySymptomSummary(symptom: com.health.model.DailySymptom) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            symptom.mood?.let { val score = it
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("情绪", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${score}/5", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
            symptom.sleepQuality?.let { val score = it
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("睡眠", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${score}/5", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
            symptom.skinCondition?.let { val score = it
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("皮肤", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${score}/5", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
            symptom.cervicalMucus?.let {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("黏液", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(it.displayName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
            symptom.basalBodyTemp?.let { val temp = it
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("体温", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${temp}℃", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HormoneTrendChart(
    assessments: List<HormoneAssessment>,
    modifier: Modifier = Modifier
) {
    val sorted = assessments.sortedBy { it.date }
    val estrogenColor = Color(0xFFE91E63)
    val progesteroneColor = Color(0xFF9C27B0)

    Card(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val width = size.width
            val height = size.height
            val padding = 20f
            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2

            // Grid lines
            for (i in 0..4) {
                val y = padding + chartHeight * (1 - i / 4f)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(padding, y),
                    end = Offset(width - padding, y),
                    strokeWidth = 1f
                )
            }

            // Estrogen line
            if (sorted.size >= 2) {
                val estrogenPath = Path()
                val progesteronePath = Path()

                sorted.forEachIndexed { index, assessment ->
                    val x = padding + (index.toFloat() / (sorted.size - 1).coerceAtLeast(1)) * chartWidth
                    assessment.estimatedEstrogen?.let { e ->
                        val y = padding + chartHeight * (1 - e.toFloat())
                        if (index == 0) estrogenPath.moveTo(x, y)
                        else estrogenPath.lineTo(x, y)
                    }
                    assessment.estimatedProgesterone?.let { p ->
                        val y = padding + chartHeight * (1 - p.toFloat())
                        if (index == 0) progesteronePath.moveTo(x, y)
                        else progesteronePath.lineTo(x, y)
                    }
                }

                drawPath(estrogenPath, estrogenColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
                drawPath(progesteronePath, progesteroneColor, style = Stroke(width = 3f, cap = StrokeCap.Round))

                // Draw dots
                sorted.forEachIndexed { index, assessment ->
                    val x = padding + (index.toFloat() / (sorted.size - 1).coerceAtLeast(1)) * chartWidth
                    assessment.estimatedEstrogen?.let { e ->
                        drawCircle(estrogenColor, 4f, Offset(x, padding + chartHeight * (1 - e.toFloat())))
                    }
                    assessment.estimatedProgesterone?.let { p ->
                        drawCircle(progesteroneColor, 4f, Offset(x, padding + chartHeight * (1 - p.toFloat())))
                    }
                }
            }

            // Labels
            drawContext.canvas.nativeCanvas.apply {
                drawText("E2", padding, 12f, android.graphics.Paint().apply {
                    color = 0xFFE91E63.toInt()
                    textSize = 30f
                })
                drawText("P4", width - padding - 30, 12f, android.graphics.Paint().apply {
                    color = 0xFF9C27B0.toInt()
                    textSize = 30f
                })
            }
        }
    }
}

@Composable
private fun BBTChart(
    bbtData: List<Pair<LocalDate, Double>>,
    modifier: Modifier = Modifier
) {
    val sorted = bbtData.sortedBy { it.first }
    val color = Color(0xFFFF7043)

    Card(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (sorted.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val padding = 20f
            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2

            val temps = sorted.map { it.second }
            val minTemp = temps.min() - 0.3
            val maxTemp = temps.max() + 0.1
            val tempRange = (maxTemp - minTemp).coerceAtLeast(0.1)

            // Basal line at 36.5
            val baseY = padding + chartHeight * (1 - ((36.5 - minTemp) / tempRange).toFloat().coerceIn(0f, 1f))
            drawLine(Color.Gray.copy(alpha = 0.5f), Offset(padding, baseY), Offset(width - padding, baseY),
                strokeWidth = 1f)

            if (sorted.size >= 2) {
                val path = Path()
                sorted.forEachIndexed { index, (_, temp) ->
                    val x = padding + (index.toFloat() / (sorted.size - 1)) * chartWidth
                    val y = padding + chartHeight * (1 - ((temp - minTemp) / tempRange).toFloat().coerceIn(0f, 1f))
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

                sorted.forEachIndexed { index, (_, temp) ->
                    val x = padding + (index.toFloat() / (sorted.size - 1)) * chartWidth
                    val y = padding + chartHeight * (1 - ((temp - minTemp) / tempRange).toFloat().coerceIn(0f, 1f))
                    drawCircle(color, 4f, Offset(x, y))
                }
            }
        }
    }
}

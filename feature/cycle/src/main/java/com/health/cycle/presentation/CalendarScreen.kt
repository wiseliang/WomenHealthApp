package com.health.cycle.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.health.model.CyclePrediction
import com.health.model.PeriodRecord
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.health.model.CyclePhase
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("经期追踪") },
                actions = {
                    IconButton(onClick = { viewModel.showLogSheet() }) {
                        Icon(Icons.Filled.Add, contentDescription = "记录经期")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            // Phase banner
            state.currentPhase?.let { phase ->
                PhaseBanner(phase = phase, cycleDay = state.cycleDayNow)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Month navigation
            MonthNavigator(
                selectedMonth = state.selectedMonth,
                onMonthChanged = { viewModel.onMonthChanged(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            CalendarGrid(
                selectedMonth = state.selectedMonth,
                periods = state.periods,
                prediction = state.prediction
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Prediction card
            PredictionCard(prediction = state.prediction)

            Spacer(modifier = Modifier.height(12.dp))

            // Recent periods
            if (state.periods.isNotEmpty()) {
                Text("最近记录", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                state.periods.take(5).forEach { period ->
                    PeriodRecordRow(
                        period = period,
                        onDelete = { viewModel.deletePeriod(period) }
                    )
                }
            }
        }

        // Log sheet
        if (state.showLogSheet) {
            ModalBottomSheet(onDismissRequest = { viewModel.hideLogSheet() }) {
                PeriodLogSheet(
                    onDismiss = { viewModel.hideLogSheet() },
                    onSave = { start, end, notes ->
                        viewModel.recordPeriod(start, end, notes)
                    }
                )
            }
        }

        // Error snackbar
        state.error?.let { error ->
            LaunchedEffect(error) {
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun PhaseBanner(phase: CyclePhase, cycleDay: Int?) {
    val (bannerColor, bannerText, subtitle) = when (phase) {
        CyclePhase.MENSTRUAL -> Triple(
            Color(0xFFE91E63), "月经期", "注意休息，补充铁质"
        )
        CyclePhase.FOLLICULAR -> Triple(
            Color(0xFF4CAF50), "卵泡期", "精力恢复，适合高强度运动"
        )
        CyclePhase.OVULATORY -> Triple(
            Color(0xFFFF9800), "排卵期", "代谢高峰，注意营养均衡"
        )
        CyclePhase.LUTEAL -> Triple(
            Color(0xFF9C27B0), "黄体期", "可能情绪波动，多摄入复合碳水"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bannerColor.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(bannerColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(bannerText, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = subtitle + (cycleDay?.let { " · 周期第${it}天" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MonthNavigator(
    selectedMonth: LocalDate,
    onMonthChanged: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onMonthChanged(selectedMonth.minusMonths(1)) }) {
            Text("<", fontSize = 18.sp)
        }
        Text(
            text = "${selectedMonth.year}年${selectedMonth.monthValue}月",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { onMonthChanged(selectedMonth.plusMonths(1)) }) {
            Text(">", fontSize = 18.sp)
        }
    }
}

@Composable
private fun CalendarGrid(
    selectedMonth: LocalDate,
    periods: List<PeriodRecord>,
    prediction: CyclePrediction?
) {
    val yearMonth = YearMonth.of(selectedMonth.year, selectedMonth.monthValue)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value

    // Build sets for coloring
    val periodDays = periods.flatMap { p ->
        var d = p.startDate
        val days = mutableListOf<LocalDate>()
        while (!d.isAfter(p.endDate)) {
            if (d.monthValue == selectedMonth.monthValue && d.year == selectedMonth.year) {
                days.add(d)
            }
            d = d.plusDays(1)
        }
        days
    }.toSet()

    val predictedDays = buildSet {
        prediction?.let { pred ->
            var d = pred.predictedNextPeriodStart
            while (!d.isAfter(pred.predictedNextPeriodEnd)) {
                if (d.monthValue == selectedMonth.monthValue && d.year == selectedMonth.year) {
                    add(d)
                }
                d = d.plusDays(1)
            }
        }
    }

    val ovulationDay = prediction?.ovulationDate?.takeIf {
        it.monthValue == selectedMonth.monthValue && it.year == selectedMonth.year
    }

    val today = LocalDate.now()

    // Day-of-week headers
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Empty cells before first day
        items((firstDayOfWeek + 5) % 7) {
            Box(modifier = Modifier.size(42.dp))
        }

        items(daysInMonth) { index ->
            val day = index + 1
            val date = yearMonth.atDay(day)
            val isPeriod = date in periodDays
            val isPredicted = date in predictedDays
            val isOvulation = date == ovulationDay
            val isToday = date == today

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .then(
                        when {
                            isPeriod -> Modifier.background(MaterialTheme.colorScheme.primary)
                            isOvulation -> Modifier
                                .border(2.dp, Color(0xFFFF9800), CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                            isPredicted -> Modifier
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                            isToday -> Modifier
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                            else -> Modifier.background(MaterialTheme.colorScheme.surface)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$day",
                    fontSize = 13.sp,
                    color = when {
                        isPeriod -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = if (isToday || isPeriod) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }

    // Legend
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = MaterialTheme.colorScheme.primary, label = "经期")
        Spacer(modifier = Modifier.width(16.dp))
        LegendItem(border = true, color = MaterialTheme.colorScheme.primary, label = "预测经期")
        Spacer(modifier = Modifier.width(16.dp))
        LegendItem(border = true, color = Color(0xFFFF9800), label = "排卵日")
    }
}

@Composable
private fun LegendItem(color: Color, label: String, border: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .then(
                    if (border) Modifier.border(1.5.dp, color, CircleShape)
                        .background(Color.Transparent)
                    else Modifier.background(color)
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PredictionCard(prediction: CyclePrediction?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("周期预测", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (prediction == null || prediction.averageCycleLength == 28 && prediction.modelVersion == "v1-mean") {
                Text(
                    "记录 3 个完整周期后，\n系统将自动预测下次经期和排卵日。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            val fmt = DateTimeFormatter.ofPattern("MM/dd")
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("预计经期", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${prediction.predictedNextPeriodStart.format(fmt)} - ${prediction.predictedNextPeriodEnd.format(fmt)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                prediction.ovulationDate?.let { ovDate ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text("预计排卵日", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            ovDate.format(fmt),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "平均周期 ${prediction.averageCycleLength} 天",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "算法: ${prediction.modelVersion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun PeriodRecordRow(
    period: PeriodRecord,
    onDelete: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${period.startDate.format(fmt)} → ${period.endDate.format(fmt)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "持续 ${period.durationDays} 天",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Text("✕", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

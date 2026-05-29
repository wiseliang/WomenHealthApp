package com.health.hormone.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.health.model.CervicalMucus
import com.health.model.DailySymptom
import java.time.LocalDate

@Composable
fun SymptomLogSheet(
    onDismiss: () -> Unit,
    onSave: (DailySymptom) -> Unit,
    existingSymptom: DailySymptom? = null
) {
    var periodFlow by remember { mutableStateOf(existingSymptom?.periodFlow) }
    var mood by remember { mutableStateOf(existingSymptom?.mood) }
    var sleepQuality by remember { mutableStateOf(existingSymptom?.sleepQuality) }
    var skinCondition by remember { mutableStateOf(existingSymptom?.skinCondition) }
    var cervicalMucus by remember { mutableStateOf(existingSymptom?.cervicalMucus) }
    var bbtText by remember { mutableStateOf(existingSymptom?.basalBodyTemp?.toString() ?: "") }
    var notes by remember { mutableStateOf(existingSymptom?.notes ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("记录今日症状", style = MaterialTheme.typography.titleLarge)
        Text("所有项目均为选填", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Period flow
        Text("经血量", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0 to "无", 1 to "点滴", 2 to "少量", 3 to "中等", 4 to "大量").forEach { (value, label) ->
                FilterChip(
                    selected = periodFlow == value,
                    onClick = { periodFlow = if (periodFlow == value) null else value },
                    label = { Text(label) }
                )
            }
        }

        // Mood 1-5
        Text("情绪", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..5) {
                val labels = listOf("", "很差", "较差", "一般", "较好", "很好")
                FilterChip(
                    selected = mood == i,
                    onClick = { mood = if (mood == i) null else i },
                    label = { Text(labels[i]) }
                )
            }
        }

        // Sleep quality
        Text("睡眠质量", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..5) {
                val labels = listOf("", "很差", "较差", "一般", "较好", "很好")
                FilterChip(
                    selected = sleepQuality == i,
                    onClick = { sleepQuality = if (sleepQuality == i) null else i },
                    label = { Text(labels[i]) }
                )
            }
        }

        // Skin condition
        Text("皮肤状态", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..5) {
                val labels = listOf("", "很差", "较差", "一般", "较好", "很好")
                FilterChip(
                    selected = skinCondition == i,
                    onClick = { skinCondition = if (skinCondition == i) null else i },
                    label = { Text(labels[i]) }
                )
            }
        }

        // Cervical mucus
        Text("宫颈黏液", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CervicalMucus.entries.forEach { mucus ->
                FilterChip(
                    selected = cervicalMucus == mucus,
                    onClick = { cervicalMucus = if (cervicalMucus == mucus) null else mucus },
                    label = { Text(mucus.displayName) }
                )
            }
        }

        // BBT
        Text("基础体温 (℃)", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = bbtText,
            onValueChange = { bbtText = it },
            placeholder = { Text("例如 36.50") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("备注 (可选)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("取消") }
            Button(
                onClick = {
                    val symptom = DailySymptom(
                        date = existingSymptom?.date ?: LocalDate.now(),
                        periodFlow = periodFlow,
                        mood = mood,
                        sleepQuality = sleepQuality,
                        skinCondition = skinCondition,
                        cervicalMucus = cervicalMucus,
                        basalBodyTemp = bbtText.toDoubleOrNull(),
                        notes = notes.ifBlank { null }
                    )
                    onSave(symptom)
                },
                modifier = Modifier.weight(1f)
            ) { Text("保存") }
        }
    }
}

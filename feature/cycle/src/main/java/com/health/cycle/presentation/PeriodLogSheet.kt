package com.health.cycle.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodLogSheet(
    onDismiss: () -> Unit,
    onSave: (startDate: LocalDate, endDate: LocalDate, notes: String?) -> Unit,
    initialStart: LocalDate = LocalDate.now(),
    initialEnd: LocalDate = LocalDate.now()
) {
    var startDate by remember { mutableStateOf(initialStart) }
    var endDate by remember { mutableStateOf(initialEnd) }
    var notes by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "记录经期",
            style = MaterialTheme.typography.titleLarge
        )

        DateRangePicker(
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = {
                startDate = it
                dateError = null
            },
            onEndDateChange = {
                endDate = it
                dateError = null
            }
        )

        if (dateError != null) {
            Text(dateError!!, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("备注 (可选)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) { Text("取消") }

            Button(
                onClick = {
                    if (endDate.isBefore(startDate)) {
                        dateError = "结束日期不能早于开始日期"
                    } else {
                        onSave(startDate, endDate, notes.ifBlank { null })
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("保存") }
        }
    }
}

@Composable
private fun DateRangePicker(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit
) {
    val datePickerStateStart = rememberDatePickerState(initialSelectedDateMillis = startDate.toEpochDay() * 86400000)
    val datePickerStateEnd = rememberDatePickerState(initialSelectedDateMillis = endDate.toEpochDay() * 86400000)

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedCard(
                onClick = { showStartPicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("开始", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(startDate.toString(), style = MaterialTheme.typography.bodyLarge)
                }
            }
            OutlinedCard(
                onClick = { showEndPicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("结束", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(endDate.toString(), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (showStartPicker) {
            DatePickerDialog(
                onDismissRequest = { showStartPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerStateStart.selectedDateMillis?.let { millis ->
                            onStartDateChange(LocalDate.ofEpochDay(millis / 86400000))
                        }
                        showStartPicker = false
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartPicker = false }) { Text("取消") }
                }
            ) { DatePicker(state = datePickerStateStart) }
        }

        if (showEndPicker) {
            DatePickerDialog(
                onDismissRequest = { showEndPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerStateEnd.selectedDateMillis?.let { millis ->
                            onEndDateChange(LocalDate.ofEpochDay(millis / 86400000))
                        }
                        showEndPicker = false
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndPicker = false }) { Text("取消") }
                }
            ) { DatePicker(state = datePickerStateEnd) }
        }
    }
}

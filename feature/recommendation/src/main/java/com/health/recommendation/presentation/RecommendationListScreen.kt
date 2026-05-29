package com.health.recommendation.presentation

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.health.model.Citation
import com.health.model.CyclePhase
import com.health.model.Recommendation
import com.health.model.RecommendationCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationListScreen(viewModel: RecommendationListViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("个性化建议") })
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Phase info banner
            val phase = try { CyclePhase.valueOf(state.currentPhase) } catch (_: Exception) { CyclePhase.LUTEAL }
            PhaseInfoCard(phase = phase, fitnessGoal = state.fitnessGoal)

            // Recommendations grouped by category
            val grouped = state.recommendations.groupBy { it.category }
            val orderedCategories = listOf(
                RecommendationCategory.DIET,
                RecommendationCategory.EXERCISE,
                RecommendationCategory.SLEEP,
                RecommendationCategory.GENERAL
            )

            orderedCategories.forEach { category ->
                val recs = grouped[category]
                if (!recs.isNullOrEmpty()) {
                    Text(
                        text = "${category.displayName}建议",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    recs.forEach { recommendation ->
                        RecommendationCard(
                            recommendation = recommendation,
                            onCitationClick = { viewModel.showCitation(it) }
                        )
                    }
                }
            }

            if (state.recommendations.isEmpty()) {
                Text(
                    "该阶段暂无建议内容",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Disclaimer
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Info, contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "建议内容基于公开发表的医学和运动科学文献，仅供参考，不构成医疗诊断。如有健康问题请咨询专业医师。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Citation dialog
        state.selectedCitation?.let { citation ->
            CitationDialog(
                citation = citation,
                onDismiss = { viewModel.hideCitation() }
            )
        }
    }
}

@Composable
private fun PhaseInfoCard(phase: CyclePhase, fitnessGoal: String?) {
    val phaseInfo = when (phase) {
        CyclePhase.MENSTRUAL -> "月经期" to Color(0xFFE91E63)
        CyclePhase.FOLLICULAR -> "卵泡期" to Color(0xFF4CAF50)
        CyclePhase.OVULATORY -> "排卵期" to Color(0xFFFF9800)
        CyclePhase.LUTEAL -> "黄体期" to Color(0xFF9C27B0)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = phaseInfo.second.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "当前阶段：${phaseInfo.first}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (fitnessGoal != null) {
                val goalText = when (fitnessGoal) {
                    "fat_loss" -> "减脂"
                    "weight_loss" -> "减重"
                    "muscle_gain" -> "增肌"
                    else -> fitnessGoal
                }
                Text(
                    "目标：$goalText · 以下为该阶段针对此目标的建议",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: Recommendation,
    onCitationClick: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = {}) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title row with category icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = categoryIcon(recommendation.category),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = categoryColor(recommendation.category)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    recommendation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                // Priority badge
                if (recommendation.priority == 1) {
                    AssistChip(
                        onClick = {},
                        label = { Text("优先", fontSize = 10.sp) },
                        modifier = Modifier.height(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                recommendation.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )

            // Detail HTML (expanded)
            if (recommendation.detailHtml != null) {
                Spacer(modifier = Modifier.height(8.dp))
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "收起详情" else "展开详情", fontSize = 13.sp)
                }
                if (expanded) {
                    Text(
                        recommendation.detailHtml.replace(Regex("<[^>]*>"), "")
                            .replace(Regex("\\s+"), " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Citations
            if (recommendation.citationKeys.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    recommendation.citationKeys.forEach { key ->
                        TextButton(
                            onClick = { onCitationClick(key) },
                            modifier = Modifier.defaultMinSize(minWidth = 0.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("[${key.takeLast(4)}]", fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CitationDialog(citation: Citation, onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("文献引用", style = MaterialTheme.typography.titleSmall)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(citation.summarySentence, style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic)
                HorizontalDivider()
                Text(citation.authors, style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold)
                Text("${citation.title} (${citation.year})", style = MaterialTheme.typography.bodySmall)
                Text(citation.journal, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                citation.doi?.let { doi ->
                    Text("DOI: $doi", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                }
                citation.pmid?.let { pmid ->
                    Text("PMID: $pmid", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
        dismissButton = {
            citation.url?.let { url ->
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }) { Text("查看原文") }
            }
        }
    )
}

private fun categoryIcon(category: RecommendationCategory): ImageVector = when (category) {
    RecommendationCategory.DIET -> Icons.Filled.Restaurant
    RecommendationCategory.EXERCISE -> Icons.Filled.FitnessCenter
    RecommendationCategory.SLEEP -> Icons.Filled.Bedtime
    RecommendationCategory.GENERAL -> Icons.Filled.Lightbulb
}

private fun categoryColor(category: RecommendationCategory): Color = when (category) {
    RecommendationCategory.DIET -> Color(0xFFFF7043)
    RecommendationCategory.EXERCISE -> Color(0xFF4CAF50)
    RecommendationCategory.SLEEP -> Color(0xFF7E57C2)
    RecommendationCategory.GENERAL -> Color(0xFF42A5F5)
}

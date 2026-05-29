package com.health.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    // Onboarding
    @Serializable data object Welcome : Screen
    @Serializable data object HealthInfo : Screen
    @Serializable data object GoalSelection : Screen
    @Serializable data object HuaweiPrompt : Screen

    // Main App — Cycle tab
    @Serializable data object Calendar : Screen
    @Serializable data class PeriodDetail(val periodId: Long) : Screen
    @Serializable data object PredictionDetail : Screen

    // Main App — Hormone tab
    @Serializable data object HormoneDashboard : Screen

    // Main App — Diet tab
    @Serializable data object MealList : Screen
    @Serializable data object MealCapture : Screen
    @Serializable data object FoodSearch : Screen

    // Main App — Recommendations tab
    @Serializable data object RecommendationList : Screen
    @Serializable data class CitationDetail(val citationKey: String) : Screen

    // Main App — Profile tab
    @Serializable data object Profile : Screen
    @Serializable data object HuaweiConnect : Screen
    @Serializable data object GoalSettings : Screen
    @Serializable data object DataExport : Screen
}

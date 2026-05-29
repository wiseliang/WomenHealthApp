package com.health.diet.data

import com.health.data.dao.FoodDatabaseEntryDao
import com.health.data.entity.FoodDatabaseEntryEntity
import javax.inject.Inject
import javax.inject.Singleton

data class FoodNutritionInfo(
    val foodId: String,
    val foodName: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double? = null,
    val carbsPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val fiberPer100g: Double? = null,
    val servingSizeG: Double? = null,
    val source: String
)

/**
 * Local food database with built-in Chinese food data.
 * Falls back to built-in database when API is unavailable (demo mode).
 */
@Singleton
class FoodDatabaseService @Inject constructor(
    private val cacheDao: FoodDatabaseEntryDao
) {
    companion object {
        // Built-in Chinese food data (per 100g, unless noted)
        private val BUILT_IN_FOODS = listOf(
            FoodNutritionInfo("builtin_rice", "米饭", 116.0, 2.6, 25.9, 0.3, 0.3, 150.0, "builtin"),
            FoodNutritionInfo("builtin_noodle", "面条(煮)", 110.0, 4.5, 22.0, 0.7, 0.5, 250.0, "builtin"),
            FoodNutritionInfo("builtin_bread", "白面包", 265.0, 8.0, 49.0, 3.5, 2.4, 60.0, "builtin"),
            FoodNutritionInfo("builtin_egg", "鸡蛋(煮)", 155.0, 13.0, 1.1, 11.0, 0.0, 50.0, "builtin"),
            FoodNutritionInfo("builtin_chicken", "鸡胸肉(熟)", 165.0, 31.0, 0.0, 3.6, 0.0, 100.0, "builtin"),
            FoodNutritionInfo("builtin_pork", "猪肉(瘦)", 143.0, 20.0, 1.5, 6.2, 0.0, 100.0, "builtin"),
            FoodNutritionInfo("builtin_beef", "牛肉(瘦)", 125.0, 22.0, 0.0, 4.2, 0.0, 100.0, "builtin"),
            FoodNutritionInfo("builtin_fish", "鱼肉(蒸)", 105.0, 18.0, 0.0, 3.5, 0.0, 100.0, "builtin"),
            FoodNutritionInfo("builtin_tofu", "豆腐", 76.0, 8.1, 2.0, 3.7, 0.4, 200.0, "builtin"),
            FoodNutritionInfo("builtin_apple", "苹果", 52.0, 0.3, 14.0, 0.2, 2.4, 200.0, "builtin"),
            FoodNutritionInfo("builtin_banana", "香蕉", 89.0, 1.1, 23.0, 0.3, 2.6, 120.0, "builtin"),
            FoodNutritionInfo("builtin_orange", "橙子", 47.0, 1.0, 12.0, 0.1, 2.4, 200.0, "builtin"),
            FoodNutritionInfo("builtin_milk", "牛奶(全脂)", 61.0, 3.0, 5.0, 3.3, 0.0, 250.0, "builtin"),
            FoodNutritionInfo("builtin_yogurt", "酸奶(原味)", 61.0, 3.5, 7.0, 1.5, 0.0, 200.0, "builtin"),
            FoodNutritionInfo("builtin_salad", "蔬菜沙拉", 25.0, 1.5, 3.5, 0.5, 2.0, 250.0, "builtin"),
            FoodNutritionInfo("builtin_nuts", "混合坚果", 607.0, 20.0, 21.0, 54.0, 7.0, 30.0, "builtin"),
            FoodNutritionInfo("builtin_coffee", "咖啡(黑)", 2.0, 0.1, 0.0, 0.0, 0.0, 240.0, "builtin"),
            FoodNutritionInfo("builtin_juice", "橙汁", 45.0, 0.7, 10.0, 0.2, 0.2, 250.0, "builtin"),
            FoodNutritionInfo("builtin_soup", "蔬菜汤", 30.0, 1.0, 5.0, 0.5, 0.5, 300.0, "builtin"),
            FoodNutritionInfo("builtin_dumpling", "饺子(猪肉)", 240.0, 10.0, 28.0, 9.0, 1.0, 200.0, "builtin")
        )

        // Fuzzy matching map from ML Kit labels to built-in foods
        private val LABEL_MAP = mapOf(
            "rice" to "builtin_rice", "noodle" to "builtin_noodle", "bread" to "builtin_bread",
            "egg" to "builtin_egg", "chicken" to "builtin_chicken", "pork" to "builtin_pork",
            "beef" to "builtin_beef", "fish" to "builtin_fish", "tofu" to "builtin_tofu",
            "apple" to "builtin_apple", "banana" to "builtin_banana", "orange" to "builtin_orange",
            "milk" to "builtin_milk", "yogurt" to "builtin_yogurt", "salad" to "builtin_salad",
            "nuts" to "builtin_nuts", "coffee" to "builtin_coffee", "juice" to "builtin_juice",
            "soup" to "builtin_soup", "dumpling" to "builtin_dumpling"
        )
    }

    private val builtInById = BUILT_IN_FOODS.associateBy { it.foodId }

    suspend fun lookup(foodName: String): FoodNutritionInfo? {
        // 1. Search local cache first
        val cached = cacheDao.searchFoods(foodName)
        if (cached.isNotEmpty()) {
            val match = cached.first()
            return FoodNutritionInfo(
                foodId = match.foodId,
                foodName = match.foodName,
                caloriesPer100g = match.caloriesPer100g,
                proteinPer100g = match.proteinPer100g,
                carbsPer100g = match.carbsPer100g,
                fatPer100g = match.fatPer100g,
                fiberPer100g = match.fiberPer100g,
                servingSizeG = match.servingSizeG,
                source = match.source
            )
        }

        // 2. Fuzzy match against built-in labels
        val lowerName = foodName.lowercase()
        for ((label, foodId) in LABEL_MAP) {
            if (lowerName.contains(label)) {
                return builtInById[foodId]
            }
        }

        // 3. Generic fallback for common food categories
        return when {
            lowerName.contains("chicken") || lowerName.contains("鸡") ->
                builtInById["builtin_chicken"]
            lowerName.contains("rice") || lowerName.contains("饭") || lowerName.contains("米") ->
                builtInById["builtin_rice"]
            lowerName.contains("meat") || lowerName.contains("肉") ->
                builtInById["builtin_pork"]
            lowerName.contains("vegetable") || lowerName.contains("菜") || lowerName.contains("veg") ->
                builtInById["builtin_salad"]
            lowerName.contains("fruit") || lowerName.contains("水果") ->
                builtInById["builtin_apple"]
            else -> null // No match at all — user must enter manually
        }
    }

    suspend fun getAllFoods(): List<FoodNutritionInfo> = BUILT_IN_FOODS

    suspend fun search(query: String): List<FoodNutritionInfo> {
        return BUILT_IN_FOODS.filter {
            it.foodName.contains(query, ignoreCase = true)
        }.ifEmpty {
            // Also try cache
            cacheDao.searchFoods(query).map { entity ->
                FoodNutritionInfo(
                    foodId = entity.foodId,
                    foodName = entity.foodName,
                    caloriesPer100g = entity.caloriesPer100g,
                    proteinPer100g = entity.proteinPer100g,
                    carbsPer100g = entity.carbsPer100g,
                    fatPer100g = entity.fatPer100g,
                    servingSizeG = entity.servingSizeG,
                    source = entity.source
                )
            }
        }
    }
}

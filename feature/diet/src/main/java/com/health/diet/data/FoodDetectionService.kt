package com.health.diet.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.food.Food
import com.google.mlkit.vision.food.FoodRecognition
import com.google.mlkit.vision.food.FoodRecognizerOptions
import javax.inject.Inject

data class DetectedFood(
    val name: String,
    val confidence: Float  // 0.0 - 1.0
)

class FoodDetectionService @Inject constructor() {

    private val recognizer = FoodRecognition.getClient(
        FoodRecognizerOptions.Builder()
            .setExecutorType(FoodRecognizerOptions.EXECUTOR_TYPE_CPU) // CPU mode for broader compatibility
            .build()
    )

    suspend fun detect(bitmap: Bitmap): Result<List<DetectedFood>> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val results = recognizer.process(image).await()
            val foods = results.map { label ->
                DetectedFood(
                    name = label.text ?: "未知食物",
                    confidence = label.confidence ?: 0f
                )
            }
            Result.success(foods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        recognizer.close()
    }
}

// Extension to convert Task to suspend
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
    return com.google.android.gms.tasks.Tasks.await(this)
}

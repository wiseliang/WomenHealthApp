package com.health.common

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.health.data.dao.DailySymptomDao
import com.health.data.dao.FoodRecordDao
import com.health.data.dao.PeriodRecordDao
import com.health.data.dao.UserDao
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class DataExporter @Inject constructor(
    private val userDao: UserDao,
    private val periodRecordDao: PeriodRecordDao,
    private val dailySymptomDao: DailySymptomDao,
    private val foodRecordDao: FoodRecordDao
) {
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun exportCsv(context: Context): File {
        val user = userDao.getCurrentUser()
        val userId = user?.userId ?: 0

        val file = File(context.cacheDir, "womenhealth_export_${LocalDate.now()}.csv")
        FileWriter(file).use { writer ->
            writer.write("﻿") // BOM for Excel UTF-8
            writer.write("=== WomenHealth 数据导出 ===\n")
            writer.write("导出日期,${LocalDate.now()}\n")
            writer.write("身高,${user?.heightCm}cm,体重,${user?.weightKg}kg,出生年,${user?.birthYear}\n\n")

            // Period records
            writer.write("=== 经期记录 ===\n")
            writer.write("开始日期,结束日期,持续天数,备注\n")
            if (userId > 0) {
                val periods = periodRecordDao.getAllPeriodsOrdered(userId)
                periods.forEach { p ->
                    writer.write("${p.startDate},${p.endDate},${p.startDate.until(p.endDate).days + 1},${(p.notes ?: "").replace(",", ";")}\n")
                }
            }
            writer.write("\n")

            // Daily symptoms
            writer.write("=== 每日症状 ===\n")
            writer.write("日期,经血量,情绪(1-5),睡眠(1-5),皮肤(1-5),宫颈黏液,基础体温,备注\n")
            if (userId > 0) {
                val symptoms = dailySymptomDao.getRecentSymptoms(userId, 365)
                symptoms.forEach { s ->
                    writer.write("${s.date},${s.periodFlow ?: ""},${s.mood ?: ""},${s.sleepQuality ?: ""},${s.skinCondition ?: ""},${s.cervicalMucus ?: ""},${s.basalBodyTemp ?: ""},${(s.notes ?: "").replace(",", ";")}\n")
                }
            }
            writer.write("\n")

            // Food records (last 90 days)
            writer.write("=== 饮食记录 (近90天) ===\n")
            writer.write("餐食类型,食物名称,热量(kcal),蛋白质(g),碳水(g),脂肪(g),纤维(g),份量,来源\n")
            if (userId > 0) {
                val ninetyDaysAgo = LocalDate.now().minusDays(90).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000
                val meals = foodRecordDao.getMealsInRange(userId, ninetyDaysAgo, System.currentTimeMillis())
                meals.forEach { m ->
                    val mealType = when(m.mealType) { "breakfast" -> "早餐"; "lunch" -> "午餐"; "dinner" -> "晚餐"; "snack" -> "加餐"; else -> m.mealType }
                    writer.write("$mealType,${m.foodName.replace(",", ";")},${m.calories},${m.proteinG ?: ""},${m.carbsG ?: ""},${m.fatG ?: ""},${m.fiberG ?: ""},${m.servingDescription ?: ""},${m.source ?: ""}\n")
                }
            }
        }

        return file
    }

    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "导出数据"))
    }
}

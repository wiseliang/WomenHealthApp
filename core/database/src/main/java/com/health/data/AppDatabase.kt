package com.health.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.health.data.converter.LocalDateConverters
import com.health.data.dao.*
import com.health.data.entity.*

@Database(
    entities = [
        UserEntity::class,
        PeriodRecordEntity::class,
        DailySymptomEntity::class,
        CyclePredictionEntity::class,
        FoodRecordEntity::class,
        FoodDatabaseEntryEntity::class,
        HealthSyncRecordEntity::class,
        RecommendationEntity::class,
        CitationEntity::class,
        HormoneAssessmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun periodRecordDao(): PeriodRecordDao
    abstract fun dailySymptomDao(): DailySymptomDao
    abstract fun cyclePredictionDao(): CyclePredictionDao
    abstract fun foodRecordDao(): FoodRecordDao
    abstract fun foodDatabaseEntryDao(): FoodDatabaseEntryDao
    abstract fun healthSyncRecordDao(): HealthSyncRecordDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun citationDao(): CitationDao
    abstract fun hormoneAssessmentDao(): HormoneAssessmentDao
}

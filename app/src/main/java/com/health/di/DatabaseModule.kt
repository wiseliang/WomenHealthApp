package com.health.di

import android.content.Context
import androidx.room.Room
import com.health.data.AppDatabase
import com.health.data.DataInitializer
import com.health.data.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "women_health.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDataInitializer(
        @ApplicationContext context: Context,
        citationDao: CitationDao,
        recommendationDao: RecommendationDao
    ): DataInitializer = DataInitializer(context, citationDao, recommendationDao)

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun providePeriodRecordDao(db: AppDatabase): PeriodRecordDao = db.periodRecordDao()
    @Provides fun provideDailySymptomDao(db: AppDatabase): DailySymptomDao = db.dailySymptomDao()
    @Provides fun provideCyclePredictionDao(db: AppDatabase): CyclePredictionDao = db.cyclePredictionDao()
    @Provides fun provideFoodRecordDao(db: AppDatabase): FoodRecordDao = db.foodRecordDao()
    @Provides fun provideFoodDatabaseEntryDao(db: AppDatabase): FoodDatabaseEntryDao = db.foodDatabaseEntryDao()
    @Provides fun provideHealthSyncRecordDao(db: AppDatabase): HealthSyncRecordDao = db.healthSyncRecordDao()
    @Provides fun provideRecommendationDao(db: AppDatabase): RecommendationDao = db.recommendationDao()
    @Provides fun provideCitationDao(db: AppDatabase): CitationDao = db.citationDao()
    @Provides fun provideHormoneAssessmentDao(db: AppDatabase): HormoneAssessmentDao = db.hormoneAssessmentDao()
}

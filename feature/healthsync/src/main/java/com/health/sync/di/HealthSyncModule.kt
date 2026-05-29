package com.health.sync.di

import com.health.sync.data.HealthSyncRepositoryImpl
import com.health.sync.data.HuaweiHealthManagerImpl
import com.health.sync.data.MockHealthManager
import com.health.sync.domain.HealthSyncManager
import com.health.sync.domain.HealthSyncRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthSyncModule {

    @Binds
    @Singleton
    abstract fun bindHealthSyncRepository(impl: HealthSyncRepositoryImpl): HealthSyncRepository

    companion object {
        @Provides
        @Singleton
        fun provideHealthSyncManager(): HealthSyncManager {
            return try {
                HuaweiHealthManagerImpl()
            } catch (_: Exception) {
                MockHealthManager()
            }
        }
    }
}

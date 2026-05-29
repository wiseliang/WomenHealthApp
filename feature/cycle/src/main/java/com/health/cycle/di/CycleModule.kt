package com.health.cycle.di

import com.health.cycle.data.CycleLocalDataSource
import com.health.cycle.data.CycleRepositoryImpl
import com.health.cycle.domain.repository.CycleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CycleModule {

    @Binds
    @Singleton
    abstract fun bindCycleRepository(impl: CycleRepositoryImpl): CycleRepository
}

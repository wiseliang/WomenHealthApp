package com.health.hormone.di

import com.health.hormone.data.HormoneRepositoryImpl
import com.health.hormone.domain.HormoneRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HormoneModule {

    @Binds
    @Singleton
    abstract fun bindHormoneRepository(impl: HormoneRepositoryImpl): HormoneRepository
}

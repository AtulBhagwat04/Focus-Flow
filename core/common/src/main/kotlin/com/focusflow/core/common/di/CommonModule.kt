package com.focusflow.core.common.di

import com.focusflow.core.common.dispatcher.DefaultDispatcherProvider
import com.focusflow.core.common.dispatcher.DispatcherProvider
import com.focusflow.core.common.time.Clock
import com.focusflow.core.common.time.SystemClock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {

    @Binds
    @Singleton
    abstract fun bindClock(impl: SystemClock): Clock

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}

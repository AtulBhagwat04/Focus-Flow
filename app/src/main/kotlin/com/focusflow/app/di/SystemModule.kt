package com.focusflow.app.di

import com.focusflow.app.system.audio.DefaultAmbientAudioPlayer
import com.focusflow.core.domain.audio.AmbientAudioPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemModule {

    @Binds
    @Singleton
    abstract fun bindAmbientAudioPlayer(
        impl: DefaultAmbientAudioPlayer,
    ): AmbientAudioPlayer
}

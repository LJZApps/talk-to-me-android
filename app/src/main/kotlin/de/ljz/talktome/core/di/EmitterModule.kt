package de.ljz.talktome.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.ljz.talktome.data.emitter.ErrorEmitter
import de.ljz.talktome.data.emitter.NetworkErrorEmitter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EmitterModule {

  @Singleton
  @Provides
  fun provideNetworkErrorEmitter(): NetworkErrorEmitter {
    return NetworkErrorEmitter()
  }

  @Singleton
  @Provides
  fun provideErrorEmitter(): ErrorEmitter {
    return ErrorEmitter()
  }

}
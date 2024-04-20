package de.ljz.talktome.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.ljz.talktome.core.coroutine.ContextProvider
import de.ljz.talktome.core.coroutine.ContextProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun provideContextProvider(): ContextProvider = ContextProviderImpl()
}
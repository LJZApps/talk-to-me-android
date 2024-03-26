package de.ljz.talktome.rewrite.core.inject

import de.ljz.talktome.rewrite.core.data.emitter.ErrorEmitter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.ljz.talktome.rewrite.core.data.emitter.NetworkErrorEmitter
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
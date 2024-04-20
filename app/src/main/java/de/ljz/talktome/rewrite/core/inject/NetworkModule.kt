package de.ljz.talktome.rewrite.core.inject

import de.ljz.talktome.BuildConfig
import de.ljz.talktome.rewrite.core.data.api.core.ApiClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.ljz.talktome.rewrite.core.data.api.core.adapters.StringToDateAdapter
import de.ljz.talktome.rewrite.core.data.emitter.NetworkErrorEmitter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Duration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Singleton
  @Provides
  fun provideMoshi(): Moshi {
    return Moshi.Builder()
      .add(StringToDateAdapter())
      .add(KotlinJsonAdapterFactory())
      .build()
  }

  @Singleton
  @Provides
  fun provideRetrofit(
    okHttpClient: OkHttpClient,
    moshi: Moshi
  ): Retrofit {
    return Retrofit.Builder()
      .baseUrl(BuildConfig.BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
  }

  @Singleton
  @Provides
  fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder().apply {
      callTimeout(Duration.ofMinutes(3))
      connectTimeout(Duration.ofMinutes(3))
      readTimeout(Duration.ofMinutes(3))
      writeTimeout(Duration.ofMinutes(3))

      if (BuildConfig.DEBUG) {
        addNetworkInterceptor(
          HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
          }
        )
      }
    }.build()
  }

  @Singleton
  @Provides
  fun provideApiClient(
    moshi: Moshi,
    retrofit: Retrofit,
    networkErrorEmitter: NetworkErrorEmitter
  ): ApiClient {
    return ApiClient(
      moshi = moshi,
      retrofit = retrofit,
      networkErrorEmitter = networkErrorEmitter
    )
  }
}
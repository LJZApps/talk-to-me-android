package de.ljz.talktome.core.di

import android.content.Context
import com.skydoves.sandwich.retrofit.adapters.ApiResponseCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.ljz.talktome.BuildConfig
import de.ljz.talktome.data.api.core.ApiClient
import de.ljz.talktome.data.api.core.adapters.StringToDateAdapter
import de.ljz.talktome.data.api.core.interceptors.FailedRequestInterceptor
import de.ljz.talktome.data.emitter.NetworkErrorEmitter
import de.ljz.talktome.data.sharedpreferences.SessionManager
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
  fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
    return SessionManager(context)
  }

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
      .addCallAdapterFactory(ApiResponseCallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun provideOkHttpClient(
    @ApplicationContext context: Context,
    sessionManager: SessionManager,
    moshi: Moshi
  ): OkHttpClient {
    return OkHttpClient.Builder().apply {
      callTimeout(Duration.ofMinutes(3))
      connectTimeout(Duration.ofMinutes(3))
      readTimeout(Duration.ofMinutes(3))
      writeTimeout(Duration.ofMinutes(3))

//      addInterceptor(AuthorizationInterceptor(sessionManager))
      addInterceptor(FailedRequestInterceptor(moshi))

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
    retrofit: Retrofit,
  ): ApiClient {
    return ApiClient(
      retrofit = retrofit,
    )
  }
}
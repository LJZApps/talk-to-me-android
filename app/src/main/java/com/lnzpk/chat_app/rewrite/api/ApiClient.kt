package com.lnzpk.chat_app.rewrite.api

import com.lnzpk.chat_app.rewrite.api.services.LoginService
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date

private const val BASE_URL = "http://talk-to-me.dev.local/"
object ApiClient {

    private val client = OkHttpClient.Builder()
        //.addInterceptor()
        .build()

    private val moshi = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    fun getLoginService(): LoginService = retrofit.create(LoginService::class.java)
}
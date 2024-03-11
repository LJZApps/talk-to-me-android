package com.lnzpk.chat_app.rewrite.core.data.api.core

import android.util.Log
import com.lnzpk.chat_app.rewrite.core.data.api.services.LoginService
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.lnzpk.chat_app.rewrite.core.app.TAG
import com.lnzpk.chat_app.rewrite.core.data.api.core.exceptions.RequestFailedException
import com.lnzpk.chat_app.rewrite.core.data.emitter.NetworkErrorEmitter
import okhttp3.internal.http2.ConnectionShutdownException
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Singleton

@Singleton
class ApiClient constructor(
    private val moshi: Moshi,
    private val retrofit: Retrofit,
    private val networkErrorEmitter: NetworkErrorEmitter,
) {
    val loginService: LoginService = retrofit.create(LoginService::class.java)

    suspend fun <T> call(
        block: suspend () -> T,
        onSuccess: suspend (T) -> Unit,
        onError: (suspend (Exception) -> Unit)? = null,
        emitErrors: Boolean = true
    ) {
        try {
            onSuccess(block.invoke())
        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())

            if (emitErrors) {
                when (e) {
                    is UnknownHostException -> networkErrorEmitter.emitNoInternet()
                    is SocketTimeoutException -> networkErrorEmitter.emitNoInternet()
                    is ConnectionShutdownException -> networkErrorEmitter.emitNoInternet()
                    is JsonDataException -> networkErrorEmitter.emitInvalidResponse()
                    is JsonEncodingException -> networkErrorEmitter.emitInvalidResponse()
                    is RequestFailedException -> networkErrorEmitter.emitRequestFailed(e.errorMessage)
                    is HttpException -> networkErrorEmitter.emitHttpError(
                        errorMessage = NetworkUtils.parseSuccessResponse(
                            moshi = moshi,
                            response = e.response()?.errorBody()?.string() ?: ""
                        )?.message
                    )
                }
            }

            onError?.invoke(e)
        }
    }

    suspend fun <T> callWithReturn(
        block: suspend () -> T,
        onError: (suspend (Exception) -> Unit)? = null,
        emitErrors: Boolean = true
    ): T? {
        try {
            return block.invoke()
        } catch (e: Exception) {
            if (emitErrors) {
                when (e) {
                    is UnknownHostException -> networkErrorEmitter.emitNoInternet()
                    is SocketTimeoutException -> networkErrorEmitter.emitNoInternet()
                    is ConnectionShutdownException -> networkErrorEmitter.emitNoInternet()
                    is JsonDataException -> networkErrorEmitter.emitInvalidResponse()
                    is JsonEncodingException -> networkErrorEmitter.emitInvalidResponse()
                    is HttpException -> networkErrorEmitter.emitHttpError(
                        errorMessage = NetworkUtils.parseSuccessResponse(
                            moshi = moshi,
                            response = e.response()?.errorBody()?.string() ?: ""
                        )?.message
                    )
                }
            }
            onError?.invoke(e)

            return null
        }
    }
}
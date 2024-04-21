package de.ljz.talktome.data.api.core

import android.util.Log
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import de.ljz.talktome.core.application.TAG
import de.ljz.talktome.data.api.core.exceptions.RequestFailedException
import de.ljz.talktome.data.api.services.LoginService
import de.ljz.talktome.data.emitter.NetworkErrorEmitter
import okhttp3.internal.http2.ConnectionShutdownException
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Singleton

@Singleton
class ApiClient(
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
          is RequestFailedException -> networkErrorEmitter.emitRequestFailed(e.errorCode, e.errorMessage)
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
          is RequestFailedException -> networkErrorEmitter.emitRequestFailed(e.errorCode, e.errorMessage)
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
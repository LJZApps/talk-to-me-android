package de.ljz.talktome.data.api.core

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import de.ljz.talktome.data.api.core.exceptions.RequestFailedException
import de.ljz.talktome.data.api.responses.common.ErrorResponse
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
    onError: (suspend (ErrorResponse) -> Unit)? = null,
    emitErrors: Boolean = true
  ) {
    try {
      onSuccess(block.invoke())
    } catch (e: Exception) {
      if (emitErrors) {
        when (e) {
          is HttpException -> {
            val successResponse =  NetworkUtils.parseErrorResponse(
              moshi = moshi,
              response = e.response()?.errorBody()?.string() ?: "{'error_code': 'unknown_error', 'error_message': 'An unknown error has been thrown'}"
            )
            successResponse?.let {
              onError?.invoke(it)
            }
          }
          is RequestFailedException -> onError?.invoke(ErrorResponse(errorCode = e.errorCode, errorMessage = e.errorMessage))
          is UnknownHostException -> onError?.invoke(ErrorResponse(errorCode = "no_network", errorMessage = "No internet connection"))
          is SocketTimeoutException -> onError?.invoke(ErrorResponse(errorCode = "no_network", errorMessage = "No internet connection"))
          is ConnectionShutdownException -> onError?.invoke(ErrorResponse(errorCode = "no_network", errorMessage = "No internet connection"))
          is JsonDataException -> onError?.invoke(ErrorResponse(errorCode = "invalid_response", errorMessage = "An invalid response has been received"))
          is JsonEncodingException -> onError?.invoke(ErrorResponse(errorCode = "invalid_response", errorMessage = "An invalid response has been received"))
          else -> {
            onError?.invoke(ErrorResponse(errorCode = "unknown_error", errorMessage = "An unknown error has been thrown"))
          }
        }
      }
    }
  }

  suspend fun <T> callWithReturn(
    block: suspend () -> T,
    onError: (suspend (ErrorResponse) -> Unit)? = null,
    emitErrors: Boolean = true
  ): T? {
    try {
      return block.invoke()
    } catch (e: Exception) {
      if (emitErrors) {
        when (e) {
          is HttpException -> {
            val successResponse =  NetworkUtils.parseErrorResponse(
              moshi = moshi,
              response = e.response()?.errorBody()?.string() ?: "{'error_code': 'unknown_error', 'error_message': 'An unknown error has been thrown'}"
            )
            successResponse?.let {
              onError?.invoke(it)
            }
          }
          is RequestFailedException -> onError?.invoke(ErrorResponse(errorCode = e.errorCode, errorMessage = e.errorMessage))
          is UnknownHostException -> onError?.invoke(ErrorResponse(errorCode = "no_network", errorMessage = "No internet connection"))
          is SocketTimeoutException -> onError?.invoke(ErrorResponse(errorCode = "no_network", errorMessage = "No internet connection"))
          is ConnectionShutdownException -> onError?.invoke(ErrorResponse(errorCode = "no_network", errorMessage = "No internet connection"))
          is JsonDataException -> onError?.invoke(ErrorResponse(errorCode = "invalid_response", errorMessage = "An invalid response has been received"))
          is JsonEncodingException -> onError?.invoke(ErrorResponse(errorCode = "invalid_response", errorMessage = "An invalid response has been received"))
          else -> {
            onError?.invoke(ErrorResponse(errorCode = "unknown_error", errorMessage = "An unknown error has been thrown"))
          }
        }
      }

      return null
    }
  }
}
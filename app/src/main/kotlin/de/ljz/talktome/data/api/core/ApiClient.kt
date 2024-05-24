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
  retrofit: Retrofit,
) {
  val loginService: LoginService = retrofit.create(LoginService::class.java)
}
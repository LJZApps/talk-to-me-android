package de.twopeaches.meindeal.core.data.emitter

import com.lnzpk.chat_app.rewrite.core.data.api.core.NetworkError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
class NetworkErrorEmitter {

    private val _channel = MutableStateFlow<NetworkError?>(null)
    val channel = _channel.asStateFlow()

    suspend fun emitNoInternet() {
        _channel.emit(NetworkError.NoInternet)
    }

    suspend fun emitInvalidResponse() {
        _channel.emit(NetworkError.InvalidResponse)
    }

    suspend fun emitRequestFailed(errorMessage: String?) {
        _channel.emit(NetworkError.RequestFailed(errorMessage))
    }

    suspend fun emitHttpError(errorMessage: String?) {
        _channel.emit(NetworkError.HttpError(errorMessage))
    }

}
package com.lnzpk.chat_app.rewrite.core.data.emitter

import androidx.annotation.StringRes
import com.lnzpk.chat_app.rewrite.core.data.shared.StringWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
class ErrorEmitter {

    private val _channel = MutableStateFlow<StringWrapper?>(null)
    val channel = _channel.asStateFlow()

    suspend fun emit(value: String) {
        _channel.emit(StringWrapper.Value(value))
    }

    suspend fun emit(@StringRes value: Int) {
        _channel.emit(StringWrapper.Resource(value))
    }

    suspend fun clear() {
        _channel.emit(null)
    }

}
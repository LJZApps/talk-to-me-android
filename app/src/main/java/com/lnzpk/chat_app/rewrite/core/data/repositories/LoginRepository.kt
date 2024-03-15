package com.lnzpk.chat_app.rewrite.core.data.repositories

import com.lnzpk.chat_app.rewrite.core.data.api.core.ApiClient
import com.lnzpk.chat_app.rewrite.core.data.api.responses.login.LoginResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(
    private val apiClient: ApiClient
): BaseRepository() {

    suspend fun login(
        username: String,
        password: String,
        onSuccess: (suspend (LoginResponse) -> Unit)? = null,
        onError: (suspend (Exception) -> Unit)? = null
    ) {
        apiClient.call(
            block = {
                apiClient.loginService.login(username, password)
            },
            onSuccess = { response ->
                onSuccess?.invoke(response)
            },
            onError = { onError?.invoke(it) }
        )
    }
}
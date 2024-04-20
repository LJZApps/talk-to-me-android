package de.ljz.talktome.data.repositories

import de.ljz.talktome.data.api.core.ApiClient
import de.ljz.talktome.data.api.responses.login.LoginResponse
import de.ljz.talktome.data.api.responses.register.RegisterResponse
import java.util.*
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
            onError = {
                onError?.invoke(it)
            }
        )
    }

    suspend fun register(
        username: String,
        onSuccess: (suspend (RegisterResponse) -> Unit)? = null,
        onError: (suspend (Exception) -> Unit)? = null
    ) {
        apiClient.call(
            block = {
                apiClient.loginService.register(
                    displayName = "Leon Zapke",
                    username = username,
                    birthday = Date()
                )
            },
            onSuccess = { response ->
                onSuccess?.invoke(response)
            },
            onError = { exception ->
                onError?.invoke(exception)
            }
        )

        apiClient.callWithReturn(
            block = {
                apiClient.loginService.login(
                    "sdofk",
                    "pod"
                )
            }

        )
    }
}
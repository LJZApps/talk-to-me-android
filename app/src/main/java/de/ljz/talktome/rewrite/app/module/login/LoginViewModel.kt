package de.ljz.talktome.rewrite.app.module.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.ljz.talktome.rewrite.core.data.repositories.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ljz.talktome.rewrite.core.app.TAG
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {

    fun login() {
        viewModelScope.launch {
            loginRepository.login(
                username = "leonzapke@gmail.com",
                password = "Leon.230",
                onSuccess = {
                    Log.d(TAG, it.toString())
                },
                onError = {
                    Log.e(TAG, it.message.toString())
                }
            )
        }
    }

    fun register() {
        viewModelScope.launch {
            loginRepository.register(
                username = "LnZpk",
                onSuccess = { response ->
                    Log.d(TAG, response.toString())
                },
                onError = { exception ->
                    Log.d(TAG, exception.message.toString())
                }
            )
        }
    }
}
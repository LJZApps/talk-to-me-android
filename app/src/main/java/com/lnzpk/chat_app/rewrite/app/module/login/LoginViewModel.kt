package com.lnzpk.chat_app.rewrite.app.module.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lnzpk.chat_app.rewrite.core.data.repositories.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.lnzpk.chat_app.rewrite.core.app.TAG
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
}
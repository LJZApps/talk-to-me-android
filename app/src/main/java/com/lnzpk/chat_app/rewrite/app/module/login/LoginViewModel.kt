package com.lnzpk.chat_app.rewrite.app.module.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lnzpk.chat_app.rewrite.core.data.repositories.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
                password = "Leon.2302",
                onSuccess = {

                },
                onError = {

                }
            )
        }
    }
}
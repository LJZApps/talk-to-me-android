package com.lnzpk.chat_app.rewrite.app.module.login

import androidx.lifecycle.ViewModel
import com.lnzpk.chat_app.rewrite.core.data.repositories.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    loginRepository: LoginRepository
) : ViewModel() {

}
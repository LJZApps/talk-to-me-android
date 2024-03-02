package com.lnzpk.chat_app.rewrite.core.data.api.services

import com.lnzpk.chat_app.rewrite.core.data.repositories.login.LoginResponse
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginService {

    @POST("/api/v1/login")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String,
    ): LoginResponse
}
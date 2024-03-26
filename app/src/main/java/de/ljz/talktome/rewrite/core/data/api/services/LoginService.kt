package de.ljz.talktome.rewrite.core.data.api.services

import de.ljz.talktome.rewrite.core.data.api.responses.login.LoginResponse
import de.ljz.talktome.rewrite.core.data.api.responses.register.RegisterResponse
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface LoginService {

    @POST("/api/login")
    suspend fun login(
        @Query("email") username: String,
        @Query("password") password: String,
    ): LoginResponse

    @POST("/api/register")
    suspend fun register(
        @Query("display_name") displayName: String,
        @Query("username") username: String,
        @Query("birthday") birthday: Date
    ): RegisterResponse
}
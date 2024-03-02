package com.lnzpk.chat_app.rewrite.core.data.api

import android.util.Log
import arrow.core.Either
import de.twopeaches.disco2app.ticketscanner.core.data.shared.models.CallFailure
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> call(serviceCall: suspend () -> T): Either<T, CallFailure> {
    return try {
        Either.Left(serviceCall())
    } catch (e: Exception) {
        Log.e("Disco2App", "API call exception: ${e.stackTraceToString()}")

        when (e) {
            is IOException -> Either.Right(CallFailure.NoInternet)
            is HttpException -> Either.Right(CallFailure.RemoteException(statusCode = e.code()))
            else -> Either.Right(CallFailure.UnknownException)
        }
    }
}

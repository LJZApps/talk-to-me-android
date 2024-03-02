package de.twopeaches.disco2app.ticketscanner.core.data.shared.models

sealed class CallFailure {
    object NoInternet : CallFailure()
    data class RemoteException(val statusCode: Int) : CallFailure()
    object UnknownException : CallFailure()
}
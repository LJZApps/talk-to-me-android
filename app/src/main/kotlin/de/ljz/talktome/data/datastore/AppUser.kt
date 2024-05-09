package de.ljz.talktome.data.datastore

import android.util.Log
import androidx.datastore.core.Serializer
import de.ljz.talktome.core.application.TAG
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.util.Date

@Serializable
data class AppUser(
    @SerialName(value = "id")
    val id: Int = -1,

    @SerialName(value = "first_name")
    val firstName: String = "",

    @SerialName(value = "last_name")
    val lastName: String = "",

    @SerialName(value = "nickname")
    val nickname: String = "",

    @SerialName(value = "email_address")
    val emailAddress: String = "",

    @SerialName(value = "profile_picture_url")
    val profilePictureUrl: String = "",

    @SerialName(value = "registered_at")
    val registeredAt: Long = 0L,

    @SerialName(value = "rank")
    val rank: String = "",

    @SerialName(value = "points")
    val points: Int = 0,

    @SerialName(value = "comment_count")
    val commentCount: Int = 0,

    @SerialName(value = "deal_count")
    val dealCount: Int = 0,

    @SerialName(value = "friends_url")
    val friendsUrl: String = ""
) {
    fun isRegularUser(): Boolean {
        return id != -1
    }

    fun isGuest(): Boolean {
        return id == -1
    }

    fun getRegisteredAt(): Date {
        return Date(registeredAt)
    }
}

object AppUserSerializer : Serializer<AppUser> {
    override val defaultValue: AppUser
        get() = AppUser()

    override suspend fun readFrom(input: InputStream): AppUser {
        return try {
            Json.decodeFromString(
                deserializer = AppUser.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize AppUser: ${e.stackTraceToString()}")
            AppUser()
        }
    }

    override suspend fun writeTo(t: AppUser, output: OutputStream) {
        try {
            output.write(
                Json.encodeToString(
                    serializer = AppUser.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize AppUser: ${e.stackTraceToString()}")
        }
    }

}
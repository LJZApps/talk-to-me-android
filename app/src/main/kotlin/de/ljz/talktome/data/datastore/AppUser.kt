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

    @SerialName(value = "display_name")
    val displayName: String = "",

    @SerialName(value = "username")
    val username: String = "",

    @SerialName(value = "email")
    val email: String = "",

    @SerialName(value = "profile_picture_url")
    val profilePictureUrl: String = "",
)

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
package de.ljz.talktome.data.datastore

import android.util.Log
import androidx.datastore.core.Serializer
import de.ljz.talktome.core.application.TAG
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * This file belongs to meindeal
 *
 * Created by Daniel Reinhold on 15.02.24 09:51
 * Copyright © 2024 2peaches GmbH. All rights reserved.
 */

@Serializable
data class PushSettings(
    @SerialName("push_notifications_enabled")
    val pushNotificationsEnabled: Boolean = true,

    @SerialName("highlight_push_active")
    val highlightPushActive: Boolean = true,

    @SerialName("night_rest_active")
    val nightRestActive: Boolean = false,

    @SerialName("night_rest_from")
    val nightRestFrom: String? = null,

    @SerialName("night_rest_to")
    val nightRestTo: String? = null
)

object PushSettingsSerializer : Serializer<PushSettings> {
    override val defaultValue: PushSettings
        get() = PushSettings()

    override suspend fun readFrom(input: InputStream): PushSettings {
        return try {
            Json.decodeFromString(
                deserializer = PushSettings.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize PushSettings: ${e.stackTraceToString()}")
            PushSettings()
        }
    }

    override suspend fun writeTo(t: PushSettings, output: OutputStream) {
        try {
            output.write(
                Json.encodeToString(
                    serializer = PushSettings.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize PushSettings: ${e.stackTraceToString()}")
        }
    }

}
package de.ljz.talktome.data.api.core.adapters

import com.squareup.moshi.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

private const val FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX"

class StringToDateAdapter : JsonAdapter<Date>() {
  private val dateFormat = SimpleDateFormat(FORMAT, Locale.getDefault())

  @FromJson
  override fun fromJson(reader: JsonReader): Date? {
    val dateString = reader.nextString()
    return try {
      SimpleDateFormat(FORMAT, Locale.getDefault()).parse(dateString)
    } catch (e: ParseException) {
      null
    }
  }

  @ToJson
  override fun toJson(writer: JsonWriter, value: Date?) {
    if (value != null) {
      synchronized(dateFormat) {
        writer.value(value.toString())
      }
    }
  }

}

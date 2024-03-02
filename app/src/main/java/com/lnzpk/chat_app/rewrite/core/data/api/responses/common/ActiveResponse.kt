package de.twopeaches.meindeal.core.data.api.responses.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This file belongs to meindeal
 *
 * Created by Daniel Reinhold on 15.02.24 08:51
 * Copyright © 2024 2peaches GmbH. All rights reserved.
 */

@JsonClass(generateAdapter = true)
data class ActiveResponse(
    @Json(name = "active")
    val active: Boolean
)

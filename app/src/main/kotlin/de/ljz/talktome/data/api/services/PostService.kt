package de.ljz.talktome.data.api.services

import de.ljz.talktome.data.api.core.annotations.Authenticated
import retrofit2.http.GET

interface PostService {

  @Authenticated
  @GET("/api/v1/posts")
  suspend fun getPosts()
}
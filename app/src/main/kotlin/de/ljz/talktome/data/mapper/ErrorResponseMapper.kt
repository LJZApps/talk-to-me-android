package de.ljz.talktome.data.mapper

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.mappers.ApiErrorModelMapper
import com.skydoves.sandwich.message
import com.skydoves.sandwich.retrofit.statusCode
import de.ljz.talktome.data.api.responses.common.ErrorResponse

object ErrorResponseMapper : ApiErrorModelMapper<ErrorResponse> {

  override fun map(apiErrorResponse: ApiResponse.Failure.Error): ErrorResponse {
    return ErrorResponse(apiErrorResponse.statusCode.code, apiErrorResponse.message())
  }
}
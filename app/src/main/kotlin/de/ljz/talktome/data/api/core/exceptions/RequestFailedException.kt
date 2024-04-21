package de.ljz.talktome.data.api.core.exceptions

import java.io.IOException

class RequestFailedException(val errorCode: String?, val errorMessage: String?) : IOException()
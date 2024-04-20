package de.ljz.talktome.data.api.core.exceptions

import java.io.IOException

class RequestFailedException(val errorMessage: String?) : IOException()
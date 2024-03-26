package de.ljz.talktome.rewrite.core.data.api.core.exceptions

import java.io.IOException

class RequestFailedException(val errorMessage: String?) : IOException()
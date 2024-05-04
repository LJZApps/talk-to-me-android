package de.ljz.talktome.core.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ljz.talktome.data.sharedpreferences.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
  private val sessionManager: SessionManager
) : ViewModel() {
  private val _isLoggedIn = MutableStateFlow(false)
  val isLoggedIn = _isLoggedIn.asStateFlow()

  init {
    _isLoggedIn.value = sessionManager.isAccessTokenPresent()
  }
  // Function to check, if a user is logged in, is coming soon.
}
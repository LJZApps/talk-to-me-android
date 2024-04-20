package de.ljz.talktome.rewrite.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(

) : ViewModel() {
  private val _isLoggedIn = MutableStateFlow(true)
  val isLoggedIn = _isLoggedIn.asStateFlow()

  // Function to check, if a user is logged in, is coming soon.
}
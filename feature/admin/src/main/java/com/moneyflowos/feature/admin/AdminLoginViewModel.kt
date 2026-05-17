package com.moneyflowos.feature.admin

import androidx.lifecycle.ViewModel
import com.moneyflowos.core.security.AdminAuthManager
import com.moneyflowos.core.security.AdminSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminLoginViewModel @Inject constructor(
  private val auth: AdminAuthManager,
  private val session: AdminSession,
) : ViewModel() {
  fun isPinSet(): Boolean = auth.isPinSet()
  fun isValidPin(pin: String): Boolean = auth.isValidPin(pin)
  fun setPin(pin: String) = auth.setPin(pin)
  fun verifyPin(pin: String): Boolean = auth.verifyPin(pin)

  fun unlock() = session.unlock()
  fun lock() = session.lock()
}


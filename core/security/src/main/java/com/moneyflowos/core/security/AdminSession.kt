package com.moneyflowos.core.security

import com.moneyflowos.core.common.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdminSession(
  private val clock: Clock,
  private val ttlMillis: Long = 10 * 60 * 1000L,
) {
  private val _isUnlocked = MutableStateFlow(false)
  val isUnlocked: StateFlow<Boolean> = _isUnlocked

  private var unlockedUntilEpochMillis: Long = 0L

  fun unlock() {
    unlockedUntilEpochMillis = clock.nowEpochMillis() + ttlMillis
    _isUnlocked.value = true
  }

  fun lock() {
    unlockedUntilEpochMillis = 0L
    _isUnlocked.value = false
  }

  fun refreshIfValid() {
    val now = clock.nowEpochMillis()
    if (unlockedUntilEpochMillis <= now) {
      lock()
    }
  }
}


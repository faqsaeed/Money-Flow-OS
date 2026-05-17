package com.moneyflowos.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyflowos.core.domain.model.Session
import com.moneyflowos.core.domain.model.SessionStatus
import com.moneyflowos.core.domain.repo.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SessionViewModel @Inject constructor(
  private val sessionRepository: SessionRepository,
) : ViewModel() {
  val session: StateFlow<Session?> = sessionRepository.observeCurrentSession()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  fun pause() = viewModelScope.launch { sessionRepository.setStatus(SessionStatus.PAUSED) }
  fun resume() = viewModelScope.launch { sessionRepository.setStatus(SessionStatus.ACTIVE) }
  fun reset() = viewModelScope.launch { sessionRepository.resetSession() }
  fun ensure() = viewModelScope.launch { sessionRepository.ensureActiveSession() }
}


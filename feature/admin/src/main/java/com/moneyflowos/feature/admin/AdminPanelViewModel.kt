package com.moneyflowos.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyflowos.core.domain.model.Session
import com.moneyflowos.core.domain.repo.PeopleRepository
import com.moneyflowos.core.domain.repo.SessionRepository
import com.moneyflowos.core.security.AdminSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AdminPanelViewModel @Inject constructor(
  private val sessionRepository: SessionRepository,
  private val peopleRepository: PeopleRepository,
  private val adminSession: AdminSession,
) : ViewModel() {
  val isUnlocked: StateFlow<Boolean> = adminSession.isUnlocked

  val session: StateFlow<Session?> = sessionRepository.observeCurrentSession()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  fun lock() = adminSession.lock()

  fun resetSession() = viewModelScope.launch {
    val newSession = sessionRepository.resetSession()
    peopleRepository.rebuildPeopleLedger(newSession.id)
  }

  fun recalcPeople() = viewModelScope.launch {
    val s = sessionRepository.ensureActiveSession()
    peopleRepository.rebuildPeopleLedger(s.id)
  }
}


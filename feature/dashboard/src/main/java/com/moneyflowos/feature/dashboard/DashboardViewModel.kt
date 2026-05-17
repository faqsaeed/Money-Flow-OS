package com.moneyflowos.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyflowos.core.domain.model.Session
import com.moneyflowos.core.domain.repo.SessionRepository
import com.moneyflowos.core.domain.repo.TransactionRepository
import com.moneyflowos.core.domain.repo.TransactionTotals
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
  private val sessionRepository: SessionRepository,
  private val transactionRepository: TransactionRepository,
) : ViewModel() {
  val session: StateFlow<Session?> = sessionRepository.observeCurrentSession()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val totals: StateFlow<TransactionTotals> = session
    .flatMapLatest { s ->
      if (s == null) kotlinx.coroutines.flow.flowOf(TransactionTotals(0, 0, 0, 0))
      else transactionRepository.observeTotals(s.id)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionTotals(0, 0, 0, 0))

  val daily: StateFlow<List<com.moneyflowos.core.domain.repo.DailyNetFlowPoint>> = session
    .flatMapLatest { s ->
      if (s == null) kotlinx.coroutines.flow.flowOf(emptyList())
      else transactionRepository.observeDailyNetFlow(sessionId = s.id, days = 14)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  fun ensureSession() = viewModelScope.launch { sessionRepository.ensureActiveSession() }
}


package com.moneyflowos.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyflowos.core.domain.model.Channel
import com.moneyflowos.core.domain.model.Direction
import com.moneyflowos.core.domain.model.Session
import com.moneyflowos.core.domain.model.Transaction
import com.moneyflowos.core.domain.model.TransactionCategory
import com.moneyflowos.core.domain.repo.SessionRepository
import com.moneyflowos.core.domain.repo.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BulkEditViewModel @Inject constructor(
  sessionRepository: SessionRepository,
  private val transactionRepository: TransactionRepository,
) : ViewModel() {
  val session: StateFlow<Session?> = sessionRepository.observeCurrentSession()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val recent: StateFlow<List<Transaction>> = session.flatMapLatest { s ->
    if (s == null) kotlinx.coroutines.flow.flowOf(emptyList())
    else transactionRepository.observeRecent(sessionId = s.id, limit = 500)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  fun applyBulk(
    ids: List<Long>,
    direction: Direction?,
    channel: Channel?,
    category: TransactionCategory?,
    person: String?,
    reason: String?,
  ) = viewModelScope.launch {
    for (id in ids) {
      transactionRepository.applyCorrection(
        transactionId = id,
        adminId = "local_admin",
        reason = reason?.takeIf { it.isNotBlank() },
        correctedAmount = null,
        correctedDirection = direction,
        correctedCategory = category,
        correctedChannel = channel,
        correctedPersonOrMerchant = person?.takeIf { it.isNotBlank() },
      )
    }
  }
}


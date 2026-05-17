package com.moneyflowos.feature.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyflowos.core.domain.model.Channel
import com.moneyflowos.core.domain.model.Direction
import com.moneyflowos.core.domain.model.Transaction
import com.moneyflowos.core.domain.model.TransactionCategory
import com.moneyflowos.core.domain.repo.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TxEditorViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val repo: TransactionRepository,
) : ViewModel() {
  private val id: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L

  val tx: StateFlow<Transaction?> = repo.observeById(id)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  fun applyCorrection(
    correctedAmount: Long?,
    correctedDirection: Direction?,
    correctedCategory: TransactionCategory?,
    correctedChannel: Channel?,
    correctedPerson: String?,
    reason: String?,
  ) = viewModelScope.launch {
    repo.applyCorrection(
      transactionId = id,
      adminId = "local_admin",
      reason = reason?.takeIf { it.isNotBlank() },
      correctedAmount = correctedAmount,
      correctedDirection = correctedDirection,
      correctedCategory = correctedCategory,
      correctedChannel = correctedChannel,
      correctedPersonOrMerchant = correctedPerson?.takeIf { it.isNotBlank() },
    )
  }
}


package com.moneyflowos.feature.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyflowos.core.domain.model.Transaction
import com.moneyflowos.core.domain.repo.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  repo: TransactionRepository,
) : ViewModel() {
  private val id: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L
  val tx: StateFlow<Transaction?> = repo.observeById(id)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}


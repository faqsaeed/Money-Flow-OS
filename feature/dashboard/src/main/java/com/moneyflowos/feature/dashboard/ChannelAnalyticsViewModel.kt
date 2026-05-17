package com.moneyflowos.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyflowos.core.domain.model.Channel
import com.moneyflowos.core.domain.repo.ChannelTotal
import com.moneyflowos.core.domain.repo.SessionRepository
import com.moneyflowos.core.domain.repo.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ChannelAnalyticsViewModel @Inject constructor(
  sessionRepository: SessionRepository,
  transactionRepository: TransactionRepository,
) : ViewModel() {
  val channels: StateFlow<List<ChannelTotal>> = sessionRepository.observeCurrentSession()
    .flatMapLatest { s -> transactionRepository.observeChannelsBreakdown(s.id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}


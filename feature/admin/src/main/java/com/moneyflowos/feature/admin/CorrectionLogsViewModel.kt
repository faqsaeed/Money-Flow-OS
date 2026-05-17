package com.moneyflowos.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyflowos.core.domain.model.CorrectionLogEntry
import com.moneyflowos.core.domain.repo.CorrectionLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CorrectionLogsViewModel @Inject constructor(
  repo: CorrectionLogRepository,
) : ViewModel() {
  val logs: StateFlow<List<CorrectionLogEntry>> = repo.observeRecent(limit = 300)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}


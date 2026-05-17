package com.moneyflowos.feature.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyflowos.core.domain.model.Person
import com.moneyflowos.core.domain.repo.PeopleRepository
import com.moneyflowos.core.domain.repo.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PeopleViewModel @Inject constructor(
  sessionRepository: SessionRepository,
  peopleRepository: PeopleRepository,
) : ViewModel() {
  val people: StateFlow<List<Person>> = sessionRepository.observeCurrentSession()
    .flatMapLatest { s -> peopleRepository.observePeople(sessionId = s.id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}


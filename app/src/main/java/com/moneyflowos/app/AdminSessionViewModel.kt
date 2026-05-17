package com.moneyflowos.app

import androidx.lifecycle.ViewModel
import com.moneyflowos.core.security.AdminSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminSessionViewModel @Inject constructor(
  val session: AdminSession,
) : ViewModel()


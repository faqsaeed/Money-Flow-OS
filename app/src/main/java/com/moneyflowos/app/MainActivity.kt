package com.moneyflowos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.moneyflowos.app.ui.theme.MoneyFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MoneyFlowTheme {
        SmsPermissionsGate {
          MoneyFlowApp()
        }
      }
    }
  }
}

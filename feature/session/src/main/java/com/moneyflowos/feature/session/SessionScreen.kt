package com.moneyflowos.feature.session

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyflowos.core.domain.model.SessionStatus

@Composable
fun SessionRoute(
  onOpenAdmin: () -> Unit,
  vm: SessionViewModel = hiltViewModel(),
) {
  LaunchedEffect(Unit) { vm.ensure() }
  val session by vm.session.collectAsState()

  SessionScreen(
    status = session?.status,
    onPause = vm::pause,
    onResume = vm::resume,
    onReset = vm::reset,
    onOpenAdmin = onOpenAdmin,
  )
}

@Composable
private fun SessionScreen(
  status: SessionStatus?,
  onPause: () -> Unit,
  onResume: () -> Unit,
  onReset: () -> Unit,
  onOpenAdmin: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      text = "Session Control",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.SemiBold,
    )

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Status: ${status ?: "…" }", style = MaterialTheme.typography.titleMedium)
        Text(
          text = "ACTIVE → process SMS, PAUSED → ignore ingestion, RESET → archive + start fresh.",
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Button(onClick = onPause, enabled = status == SessionStatus.ACTIVE) { Text("Pause") }
      Button(onClick = onResume, enabled = status == SessionStatus.PAUSED) { Text("Resume") }
      OutlinedButton(onClick = onReset, enabled = status != null) { Text("Reset Session") }
    }

    Spacer(Modifier.height(8.dp))

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .combinedClickable(
          onClick = {},
          onLongClick = onOpenAdmin,
        ),
    ) {
      Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
        Text(text = "Admin Panel", style = MaterialTheme.typography.titleMedium)
        Text(
          text = "Long-press here to open (PIN protected).",
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
  }
}


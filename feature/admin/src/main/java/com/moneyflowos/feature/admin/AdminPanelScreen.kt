package com.moneyflowos.feature.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AdminPanelRoute(
  onOpenTxEditor: (Long) -> Unit,
  onOpenLogs: () -> Unit,
  onOpenBulk: () -> Unit,
  onExit: () -> Unit,
  vm: AdminPanelViewModel = hiltViewModel(),
) {
  val unlocked by vm.isUnlocked.collectAsState()
  val session by vm.session.collectAsState()

  if (!unlocked) {
    onExit()
    return
  }

  AdminPanelScreen(
    sessionId = session?.id,
    onOpenTxEditor = onOpenTxEditor,
    onOpenLogs = onOpenLogs,
    onOpenBulk = onOpenBulk,
    onRecalc = vm::recalcPeople,
    onResetSession = vm::resetSession,
    onLock = {
      vm.lock()
      onExit()
    },
  )
}

@Composable
private fun AdminPanelScreen(
  sessionId: Long?,
  onOpenTxEditor: (Long) -> Unit,
  onOpenLogs: () -> Unit,
  onOpenBulk: () -> Unit,
  onRecalc: () -> Unit,
  onResetSession: () -> Unit,
  onLock: () -> Unit,
) {
  var txIdText by remember { mutableStateOf("") }

  Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(text = "Admin Panel", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    Text(text = "Session: ${sessionId ?: "…"}", style = MaterialTheme.typography.bodyMedium)

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "Transaction Editor", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
          value = txIdText,
          onValueChange = { txIdText = it.filter(Char::isDigit).take(18) },
          label = { Text("Transaction ID") },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
        )
        Button(
          onClick = { txIdText.toLongOrNull()?.let(onOpenTxEditor) },
          enabled = txIdText.toLongOrNull() != null,
          modifier = Modifier.fillMaxWidth(),
        ) { Text("Open Editor") }
      }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "Audit & Recalc", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Button(onClick = onOpenLogs, modifier = Modifier.weight(1f)) { Text("Correction Logs") }
          Button(onClick = onRecalc, modifier = Modifier.weight(1f)) { Text("Recalculate") }
        }
        Button(onClick = onOpenBulk, modifier = Modifier.fillMaxWidth()) { Text("Bulk Edit") }
        Button(onClick = onResetSession, modifier = Modifier.fillMaxWidth()) { Text("Session Reset (Archive + New)") }
      }
    }

    Button(onClick = onLock, modifier = Modifier.fillMaxWidth()) { Text("Lock Admin Mode") }
  }
}

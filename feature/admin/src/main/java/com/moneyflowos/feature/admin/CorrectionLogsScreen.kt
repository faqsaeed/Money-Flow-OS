package com.moneyflowos.feature.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyflowos.core.domain.model.CorrectionLogEntry

@Composable
fun CorrectionLogsRoute(
  vm: CorrectionLogsViewModel = hiltViewModel(),
) {
  val logs by vm.logs.collectAsState()
  CorrectionLogsScreen(logs)
}

@Composable
private fun CorrectionLogsScreen(logs: List<CorrectionLogEntry>) {
  LazyColumn(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text(text = "Correction Audit Trail", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
      Text(text = "Raw SMS is immutable; corrections are overlays with full history.", style = MaterialTheme.typography.bodyMedium)
    }

    items(logs, key = { it.id }) { log ->
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = "Tx #${log.transactionId}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          if (!log.reason.isNullOrBlank()) Text(text = "Reason: ${log.reason}", style = MaterialTheme.typography.bodyMedium)
          Text(text = "Old: ${log.oldValuesJson}", style = MaterialTheme.typography.bodySmall)
          Text(text = "New: ${log.newValuesJson}", style = MaterialTheme.typography.bodySmall)
          Text(text = "Admin: ${log.adminId}", style = MaterialTheme.typography.labelLarge)
        }
      }
    }
  }
}


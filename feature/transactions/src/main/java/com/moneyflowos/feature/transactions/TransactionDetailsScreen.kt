package com.moneyflowos.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.moneyflowos.core.common.MoneyFormat

@Composable
fun TransactionDetailsRoute(
  canEdit: Boolean,
  onEdit: (Long) -> Unit,
  vm: TransactionDetailsViewModel = hiltViewModel(),
) {
  val tx by vm.tx.collectAsState()
  TransactionDetailsScreen(tx = tx, canEdit = canEdit, onEdit = onEdit)
}

@Composable
private fun TransactionDetailsScreen(
  tx: com.moneyflowos.core.domain.model.Transaction?,
  canEdit: Boolean,
  onEdit: (Long) -> Unit,
) {
  if (tx == null) {
    Column(modifier = Modifier.padding(16.dp)) { Text("Loading…") }
    return
  }

  Column(
    modifier = Modifier
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(text = "Transaction #${tx.id}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Amount: ${MoneyFormat.pkr(tx.effectiveAmount)}")
        Text("Direction: ${tx.effectiveDirection}")
        Text("Channel: ${tx.effectiveChannel}")
        Text("Category: ${tx.effectiveCategory}")
        Text("Person/Merchant: ${tx.effectivePersonOrMerchant ?: "—"}")
        Text("Trusted: ${tx.trustedSource}")
        Text("Corrected: ${tx.isCorrected}")
      }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Raw SMS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(tx.rawSms)
      }
    }

    if (canEdit) {
      Button(onClick = { onEdit(tx.id) }) {
        Text("Edit / Correct (Admin)")
      }
    }
  }
}


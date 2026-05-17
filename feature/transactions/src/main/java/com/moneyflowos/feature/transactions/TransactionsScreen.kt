package com.moneyflowos.feature.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyflowos.core.common.MoneyFormat
import com.moneyflowos.core.domain.model.Direction
import com.moneyflowos.core.domain.model.Transaction

@Composable
fun TransactionsRoute(
  onOpenTransaction: (Long) -> Unit,
  vm: TransactionsViewModel = hiltViewModel(),
) {
  val list by vm.recent.collectAsState()
  TransactionsScreen(list = list, onOpenTransaction = onOpenTransaction)
}

@Composable
private fun TransactionsScreen(
  list: List<Transaction>,
  onOpenTransaction: (Long) -> Unit,
) {
  var query by remember { mutableStateOf("") }
  val filtered = remember(list, query) {
    val q = query.trim().lowercase()
    if (q.isBlank()) list
    else list.filter { tx ->
      (tx.effectivePersonOrMerchant ?: "").lowercase().contains(q) ||
        tx.rawSms.lowercase().contains(q) ||
        tx.effectiveChannel.name.lowercase().contains(q) ||
        tx.effectiveCategory.name.lowercase().contains(q)
    }
  }

  Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text(text = "Transactions", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    OutlinedTextField(
      value = query,
      onValueChange = { query = it },
      modifier = Modifier.fillMaxWidth(),
      label = { Text("Search (person, raw SMS, channel, category)") },
      singleLine = true,
    )
    Spacer(Modifier.height(4.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      items(filtered, key = { it.id }) { tx ->
        TransactionRow(tx = tx, onClick = { onOpenTransaction(tx.id) })
      }
    }
  }
}

@Composable
private fun TransactionRow(tx: Transaction, onClick: () -> Unit) {
  val dir = tx.effectiveDirection
  val sign = if (dir == Direction.IN) "+" else "-"
  val color = if (dir == Direction.IN) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
  val title = tx.effectivePersonOrMerchant ?: tx.effectiveChannel.name
  val subtitle = "${tx.effectiveChannel.name} • ${tx.effectiveCategory.name}"

  Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
      }
      Text(
        text = "$sign ${MoneyFormat.pkr(tx.effectiveAmount)}",
        style = MaterialTheme.typography.titleMedium,
        color = color,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}


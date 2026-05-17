package com.moneyflowos.feature.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyflowos.core.common.MoneyFormat
import com.moneyflowos.core.domain.model.Channel
import com.moneyflowos.core.domain.model.Direction
import com.moneyflowos.core.domain.model.Transaction
import com.moneyflowos.core.domain.model.TransactionCategory

@Composable
fun BulkEditRoute(
  onDone: () -> Unit,
  vm: BulkEditViewModel = hiltViewModel(),
) {
  val list by vm.recent.collectAsState()
  BulkEditScreen(list = list, onApply = vm::applyBulk, onDone = onDone)
}

@Composable
private fun BulkEditScreen(
  list: List<Transaction>,
  onApply: (ids: List<Long>, direction: Direction?, channel: Channel?, category: TransactionCategory?, person: String?, reason: String?) -> Unit,
  onDone: () -> Unit,
) {
  var query by remember { mutableStateOf("") }
  var reason by remember { mutableStateOf("bulk correction") }
  var person by remember { mutableStateOf("") }
  var direction by remember { mutableStateOf<Direction?>(null) }
  var channel by remember { mutableStateOf<Channel?>(null) }
  var category by remember { mutableStateOf<TransactionCategory?>(null) }
  var selected by remember { mutableStateOf(setOf<Long>()) }

  val filtered = remember(list, query) {
    val q = query.trim().lowercase()
    if (q.isBlank()) list
    else list.filter { it.rawSms.lowercase().contains(q) || (it.effectivePersonOrMerchant ?: "").lowercase().contains(q) }
  }

  Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(text = "Bulk Edit", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    Text(text = "Select transactions, then apply overlay fields (deterministic).", style = MaterialTheme.typography.bodyMedium)

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it.take(50) },
          label = { Text("Filter (raw SMS / person)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        OutlinedTextField(
          value = person,
          onValueChange = { person = it.take(80) },
          label = { Text("Person / Merchant (optional)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        OutlinedTextField(
          value = reason,
          onValueChange = { reason = it.take(120) },
          label = { Text("Reason (audit)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )

        EnumNullablePicker("Direction", direction, Direction.entries) { direction = it }
        EnumNullablePicker("Channel", channel, Channel.entries) { channel = it }
        EnumNullablePicker("Category", category, TransactionCategory.entries) { category = it }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          Button(onClick = onDone, modifier = Modifier.weight(1f)) { Text("Done") }
          Button(
            onClick = {
              onApply(selected.toList(), direction, channel, category, person, reason)
              selected = emptySet()
            },
            enabled = selected.isNotEmpty(),
            modifier = Modifier.weight(1f),
          ) { Text("Apply (${selected.size})") }
        }
      }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      items(filtered, key = { it.id }) { tx ->
        val checked = selected.contains(tx.id)
        Card(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Checkbox(checked = checked, onCheckedChange = { isChecked ->
              selected = if (isChecked) selected + tx.id else selected - tx.id
            })
            Column(modifier = Modifier.weight(1f)) {
              Text(text = "#${tx.id} • ${tx.effectivePersonOrMerchant ?: tx.effectiveChannel.name}", fontWeight = FontWeight.SemiBold)
              Text(text = tx.rawSms.take(80), style = MaterialTheme.typography.bodySmall)
            }
            Text(text = MoneyFormat.pkr(tx.effectiveAmount), style = MaterialTheme.typography.labelLarge)
          }
        }
      }
    }
  }
}

@Composable
private fun <T : Enum<T>> EnumNullablePicker(
  title: String,
  value: T?,
  values: List<T>,
  onChange: (T?) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(text = title, style = MaterialTheme.typography.labelLarge)
    Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
      Text(value?.name ?: "— (no change)")
    }
    androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      androidx.compose.material3.DropdownMenuItem(
        text = { Text("— (no change)") },
        onClick = {
          expanded = false
          onChange(null)
        },
      )
      for (v in values) {
        androidx.compose.material3.DropdownMenuItem(
          text = { Text(v.name) },
          onClick = {
            expanded = false
            onChange(v)
          },
        )
      }
    }
  }
}


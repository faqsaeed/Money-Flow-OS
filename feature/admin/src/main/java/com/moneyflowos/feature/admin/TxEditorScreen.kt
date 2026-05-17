package com.moneyflowos.feature.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyflowos.core.common.MoneyFormat
import com.moneyflowos.core.domain.model.Channel
import com.moneyflowos.core.domain.model.Direction
import com.moneyflowos.core.domain.model.Transaction
import com.moneyflowos.core.domain.model.TransactionCategory

@Composable
fun TxEditorRoute(
  onDone: () -> Unit,
  vm: TxEditorViewModel = hiltViewModel(),
) {
  val tx by vm.tx.collectAsState()
  TxEditorScreen(
    tx = tx,
    onApply = vm::applyCorrection,
    onDone = onDone,
  )
}

@Composable
private fun TxEditorScreen(
  tx: Transaction?,
  onApply: (
    correctedAmount: Long?,
    correctedDirection: Direction?,
    correctedCategory: TransactionCategory?,
    correctedChannel: Channel?,
    correctedPerson: String?,
    reason: String?,
  ) -> Unit,
  onDone: () -> Unit,
) {
  if (tx == null) {
    Column(modifier = Modifier.padding(16.dp)) { Text("Loading…") }
    return
  }

  var amountText by remember { mutableStateOf(tx.effectiveAmount.toString()) }
  var personText by remember { mutableStateOf(tx.effectivePersonOrMerchant.orEmpty()) }
  var reasonText by remember { mutableStateOf("") }

  var dir by remember { mutableStateOf(tx.effectiveDirection) }
  var ch by remember { mutableStateOf(tx.effectiveChannel) }
  var cat by remember { mutableStateOf(tx.effectiveCategory) }

  Column(
    modifier = Modifier
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(text = "Edit Transaction #${tx.id}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Current (effective)", style = MaterialTheme.typography.titleMedium)
        Text("Amount: ${MoneyFormat.pkr(tx.effectiveAmount)}")
        Text("Direction: ${tx.effectiveDirection}")
        Text("Channel: ${tx.effectiveChannel}")
        Text("Category: ${tx.effectiveCategory}")
        Text("Person: ${tx.effectivePersonOrMerchant ?: "—"}")
      }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "Correction Overlay", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it.filter { c -> c.isDigit() }.take(18) },
          label = { Text("Amount (PKR rupees)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        EnumPicker(
          title = "Direction",
          value = dir,
          values = Direction.entries,
          onChange = { dir = it },
        )
        EnumPicker(
          title = "Channel",
          value = ch,
          values = Channel.entries,
          onChange = { ch = it },
        )
        EnumPicker(
          title = "Category",
          value = cat,
          values = TransactionCategory.entries,
          onChange = { cat = it },
        )

        OutlinedTextField(
          value = personText,
          onValueChange = { personText = it.take(80) },
          label = { Text("Person / Merchant") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
          value = reasonText,
          onValueChange = { reasonText = it.take(120) },
          label = { Text("Reason (optional)") },
          modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) { Text("Done") }
          Button(
            onClick = {
              val amount = amountText.toLongOrNull()
              onApply(amount, dir, cat, ch, personText, reasonText)
            },
            enabled = amountText.toLongOrNull() != null,
            modifier = Modifier.weight(1f),
          ) { Text("Apply") }
        }
      }
    }
  }
}

@Composable
private fun <T : Enum<T>> EnumPicker(
  title: String,
  value: T,
  values: List<T>,
  onChange: (T) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(text = title, style = MaterialTheme.typography.labelLarge)
    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
      Text(value.name)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      for (v in values) {
        DropdownMenuItem(
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


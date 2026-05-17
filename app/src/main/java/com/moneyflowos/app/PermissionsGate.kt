package com.moneyflowos.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun SmsPermissionsGate(content: @Composable () -> Unit) {
  val context = LocalContext.current
  var granted by remember { mutableStateOf(hasSmsPermissions(context)) }

  val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
    granted = result.values.all { it }
  }

  if (granted) {
    content()
    return
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(text = "SMS Permission Required", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    Text(
      text = "MONEY FLOW OS reads bank notification SMS locally on-device to build your dashboard. No cloud, no APIs.",
      style = MaterialTheme.typography.bodyMedium,
    )
    Button(
      onClick = {
        launcher.launch(
          arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
          ),
        )
      },
    ) { Text("Grant SMS Access") }
  }
}

private fun hasSmsPermissions(context: android.content.Context): Boolean {
  val receive = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
  val read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
  return receive && read
}


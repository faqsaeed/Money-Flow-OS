package com.moneyflowos.feature.admin

import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AdminLoginRoute(
  onUnlocked: () -> Unit,
  vm: AdminLoginViewModel = hiltViewModel(),
) {
  AdminLoginScreen(
    isPinSet = vm.isPinSet(),
    verifyPin = vm::verifyPin,
    setPin = vm::setPin,
    unlock = {
      vm.unlock()
      onUnlocked()
    },
    isValidPin = vm::isValidPin,
  )
}

@Composable
private fun AdminLoginScreen(
  isPinSet: Boolean,
  verifyPin: (String) -> Boolean,
  setPin: (String) -> Unit,
  unlock: () -> Unit,
  isValidPin: (String) -> Boolean,
) {
  val context = LocalContext.current
  var pin by remember { mutableStateOf("") }
  var pin2 by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }

  val canBiometric = remember {
    BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
      BiometricManager.BIOMETRIC_SUCCESS
  }

  Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(text = "Admin Mode", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    Text(text = "PIN required. Optional biometric unlock if available.", style = MaterialTheme.typography.bodyMedium)

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!isPinSet) {
          Text(text = "Set Admin PIN (4–8 digits)", style = MaterialTheme.typography.titleMedium)
          OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.filter(Char::isDigit).take(8) },
            label = { Text("New PIN") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
          )
          OutlinedTextField(
            value = pin2,
            onValueChange = { pin2 = it.filter(Char::isDigit).take(8) },
            label = { Text("Confirm PIN") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
          )
          Button(
            onClick = {
              error = null
              if (!isValidPin(pin)) {
                error = "PIN must be 4–8 digits."
                return@Button
              }
              if (pin != pin2) {
                error = "PINs do not match."
                return@Button
              }
              setPin(pin)
              unlock()
            },
            modifier = Modifier.fillMaxWidth(),
          ) { Text("Set PIN & Unlock") }
        } else {
          Text(text = "Enter PIN", style = MaterialTheme.typography.titleMedium)
          OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.filter(Char::isDigit).take(8) },
            label = { Text("PIN") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
          )
          Button(
            onClick = {
              error = null
              if (!verifyPin(pin)) {
                error = "Incorrect PIN."
                return@Button
              }
              unlock()
            },
            modifier = Modifier.fillMaxWidth(),
          ) { Text("Unlock") }

          if (canBiometric) {
            Button(
              onClick = {
                val activity = context as? ComponentActivity ?: return@Button
                val executor = ContextCompat.getMainExecutor(context)
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                  .setTitle("Unlock Admin Mode")
                  .setSubtitle("Biometric unlock")
                  .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                  .setNegativeButtonText("Cancel")
                  .build()
                val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                  override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    unlock()
                  }
                })
                prompt.authenticate(promptInfo)
              },
              modifier = Modifier.fillMaxWidth(),
            ) { Text("Unlock with Biometrics") }
          }
        }

        if (error != null) {
          Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }
      }
    }
  }
}


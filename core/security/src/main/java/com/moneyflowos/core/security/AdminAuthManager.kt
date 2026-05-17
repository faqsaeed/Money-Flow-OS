package com.moneyflowos.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AdminAuthManager(
  context: Context,
) {
  private val prefs = EncryptedSharedPreferences.create(
    context,
    "admin_auth",
    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
  )

  fun isPinSet(): Boolean = prefs.contains(KEY_PIN_HASH) && prefs.contains(KEY_PIN_SALT)

  fun setPin(pin: String) {
    require(isValidPin(pin)) { "Invalid PIN format" }
    val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
    val hash = pbkdf2(pin, salt)
    prefs.edit()
      .putString(KEY_PIN_SALT, b64(salt))
      .putString(KEY_PIN_HASH, b64(hash))
      .apply()
  }

  fun verifyPin(pin: String): Boolean {
    if (!isValidPin(pin)) return false
    val salt = prefs.getString(KEY_PIN_SALT, null)?.let(::b64d) ?: return false
    val expected = prefs.getString(KEY_PIN_HASH, null)?.let(::b64d) ?: return false
    val actual = pbkdf2(pin, salt)
    return constantTimeEquals(expected, actual)
  }

  fun clearPin() {
    prefs.edit().remove(KEY_PIN_SALT).remove(KEY_PIN_HASH).apply()
  }

  fun isValidPin(pin: String): Boolean = PIN_REGEX.matches(pin)

  private fun pbkdf2(pin: String, salt: ByteArray): ByteArray {
    val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_BITS)
    return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
  }

  private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
    if (a.size != b.size) return false
    var diff = 0
    for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
    return diff == 0
  }

  private fun b64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
  private fun b64d(text: String): ByteArray = Base64.getDecoder().decode(text)

  private companion object {
    private val PIN_REGEX = Regex("""^\d{4,8}$""")
    private const val PBKDF2_ITERATIONS = 120_000
    private const val PBKDF2_KEY_BITS = 256
    private const val KEY_PIN_SALT = "pin_salt"
    private const val KEY_PIN_HASH = "pin_hash"
  }
}


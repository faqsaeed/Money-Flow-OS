package com.moneyflowos.core.common

import java.util.Locale

object Normalization {
  fun normalizePersonKey(input: String): String {
    val trimmed = input.trim()
    val lowered = trimmed.lowercase(Locale.US)
    return lowered
      .replace(Regex("\\s+"), " ")
      .replace(Regex("[^a-z0-9 @._-]"), "")
      .trim()
  }
}


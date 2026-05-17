package com.moneyflowos.core.parser.rules

import java.math.BigDecimal
import java.math.RoundingMode

internal object MoneyParsing {
  // PKR amounts in *rupees* (long). Decimal fractions are rounded down deterministically.
  fun parseAmountRupees(raw: String): Long? {
    val cleaned = raw
      .replace(",", "")
      .replace("PKR", "", ignoreCase = true)
      .replace("Rs.", "", ignoreCase = true)
      .replace("Rs", "", ignoreCase = true)
      .trim()

    val normalized = cleaned.replace(Regex("[^0-9.]"), "")
    if (normalized.isBlank()) return null

    return runCatching {
      BigDecimal(normalized)
        .setScale(0, RoundingMode.DOWN)
        .longValueExact()
    }.getOrNull()
  }
}


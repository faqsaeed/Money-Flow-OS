package com.moneyflowos.core.common

import java.text.DecimalFormat

object MoneyFormat {
  private val df = DecimalFormat("#,##0")

  fun pkr(amountRupees: Long): String = "PKR ${df.format(amountRupees)}"
}


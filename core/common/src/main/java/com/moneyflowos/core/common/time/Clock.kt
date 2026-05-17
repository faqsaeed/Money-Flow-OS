package com.moneyflowos.core.common.time

fun interface Clock {
  fun nowEpochMillis(): Long
}

object SystemClock : Clock {
  override fun nowEpochMillis(): Long = java.lang.System.currentTimeMillis()
}


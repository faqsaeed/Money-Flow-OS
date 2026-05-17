package com.moneyflowos.core.domain.model

data class SmsEnvelope(
  val sender: String?,
  val messageBody: String,
  val receivedAtEpochMillis: Long,
)

data class ParsedTransactionDraft(
  val amount: Long,
  val direction: Direction,
  val channel: Channel,
  val personOrMerchant: String?,
  val category: TransactionCategory,
  val fee: Long?,
  val balanceAfter: Long?,
  val timestampEpochMillis: Long?,
  val parserRuleId: String,
)


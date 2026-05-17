package com.moneyflowos.core.domain.model

data class Transaction(
  val id: Long,
  val amount: Long,
  val direction: Direction,
  val channel: Channel,
  val personOrMerchant: String?,
  val personId: Long?,
  val category: TransactionCategory,
  val fee: Long?,
  val balanceAfter: Long?,
  val timestampEpochMillis: Long,
  val rawSms: String,
  val sessionId: Long,
  val trustedSource: Boolean,
  val sourceSender: String?,
  val sourceReceivedAtEpochMillis: Long,
  val isCorrected: Boolean,
  val correctedAmount: Long?,
  val correctedDirection: Direction?,
  val correctedCategory: TransactionCategory?,
  val correctedChannel: Channel?,
  val correctedPersonOrMerchant: String?,
  val correctedPersonId: Long?,
) {
  val effectiveAmount: Long get() = correctedAmount ?: amount
  val effectiveDirection: Direction get() = correctedDirection ?: direction
  val effectiveChannel: Channel get() = correctedChannel ?: channel
  val effectiveCategory: TransactionCategory get() = correctedCategory ?: category
  val effectivePersonOrMerchant: String? get() = correctedPersonOrMerchant ?: personOrMerchant
  val effectivePersonId: Long? get() = correctedPersonId ?: personId
}

data class Person(
  val id: Long,
  val name: String,
  val totalSent: Long,
  val totalReceived: Long,
  val transactionCount: Long,
  val firstSeenEpochMillis: Long,
  val lastSeenEpochMillis: Long,
)

data class Session(
  val id: Long,
  val status: SessionStatus,
  val startTimeEpochMillis: Long,
  val endTimeEpochMillis: Long?,
)

data class CorrectionLogEntry(
  val id: Long,
  val transactionId: Long,
  val oldValuesJson: String,
  val newValuesJson: String,
  val reason: String?,
  val timestampEpochMillis: Long,
  val adminId: String,
)


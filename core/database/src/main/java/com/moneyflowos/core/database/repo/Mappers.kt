package com.moneyflowos.core.database.repo

import com.moneyflowos.core.database.entity.CorrectionLogEntity
import com.moneyflowos.core.database.entity.PersonEntity
import com.moneyflowos.core.database.entity.SessionEntity
import com.moneyflowos.core.database.entity.TransactionEntity
import com.moneyflowos.core.domain.model.CorrectionLogEntry
import com.moneyflowos.core.domain.model.Person
import com.moneyflowos.core.domain.model.Session
import com.moneyflowos.core.domain.model.Transaction

internal fun TransactionEntity.toDomain(): Transaction {
  return Transaction(
    id = id,
    amount = amount,
    direction = direction,
    channel = channel,
    personOrMerchant = personOrMerchant,
    personId = personId,
    category = category,
    fee = fee,
    balanceAfter = balanceAfter,
    timestampEpochMillis = timestampEpochMillis,
    rawSms = rawSms,
    sessionId = sessionId,
    trustedSource = trustedSource,
    sourceSender = sourceSender,
    sourceReceivedAtEpochMillis = sourceReceivedAtEpochMillis,
    isCorrected = isCorrected,
    correctedAmount = correctedAmount,
    correctedDirection = correctedDirection,
    correctedCategory = correctedCategory,
    correctedChannel = correctedChannel,
    correctedPersonOrMerchant = correctedPersonOrMerchant,
    correctedPersonId = correctedPersonId,
  )
}

internal fun PersonEntity.toDomain(): Person {
  return Person(
    id = id,
    name = name,
    totalSent = totalSent,
    totalReceived = totalReceived,
    transactionCount = transactionCount,
    firstSeenEpochMillis = firstSeenEpochMillis,
    lastSeenEpochMillis = lastSeenEpochMillis,
  )
}

internal fun SessionEntity.toDomain(): Session {
  return Session(
    id = id,
    status = status,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
  )
}

internal fun CorrectionLogEntity.toDomain(): CorrectionLogEntry {
  return CorrectionLogEntry(
    id = id,
    transactionId = transactionId,
    oldValuesJson = oldValuesJson,
    newValuesJson = newValuesJson,
    reason = reason,
    timestampEpochMillis = timestampEpochMillis,
    adminId = adminId,
  )
}


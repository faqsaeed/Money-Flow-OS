package com.moneyflowos.core.domain.model

enum class Direction {
  IN,
  OUT,
}

enum class Channel {
  IBFT,
  RAAST,
  POS,
  ATM,
  MOBILE_LOAD,
  WALLET,
  LOAN,
  UNKNOWN,
}

enum class TransactionCategory {
  MONEY_IN,
  MONEY_OUT,
  IBFT,
  RAAST,
  POS,
  ATM_WITHDRAWAL,
  MOBILE_LOAD,
  LOAN_CREDIT,
  OTHER,
}

enum class SessionStatus {
  ACTIVE,
  PAUSED,
  ARCHIVED,
}


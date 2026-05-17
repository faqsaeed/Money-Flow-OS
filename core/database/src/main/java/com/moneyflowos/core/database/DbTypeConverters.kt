package com.moneyflowos.core.database

import androidx.room.TypeConverter
import com.moneyflowos.core.domain.model.Channel
import com.moneyflowos.core.domain.model.Direction
import com.moneyflowos.core.domain.model.SessionStatus
import com.moneyflowos.core.domain.model.TransactionCategory

class DbTypeConverters {
  @TypeConverter fun directionToString(value: Direction?): String? = value?.name
  @TypeConverter fun stringToDirection(value: String?): Direction? = value?.let(Direction::valueOf)

  @TypeConverter fun channelToString(value: Channel?): String? = value?.name
  @TypeConverter fun stringToChannel(value: String?): Channel? = value?.let(Channel::valueOf)

  @TypeConverter fun categoryToString(value: TransactionCategory?): String? = value?.name
  @TypeConverter fun stringToCategory(value: String?): TransactionCategory? = value?.let(TransactionCategory::valueOf)

  @TypeConverter fun sessionStatusToString(value: SessionStatus?): String? = value?.name
  @TypeConverter fun stringToSessionStatus(value: String?): SessionStatus? = value?.let(SessionStatus::valueOf)
}


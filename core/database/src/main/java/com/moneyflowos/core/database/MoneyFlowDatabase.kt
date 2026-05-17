package com.moneyflowos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.moneyflowos.core.database.dao.CorrectionLogDao
import com.moneyflowos.core.database.dao.PeopleDao
import com.moneyflowos.core.database.dao.SessionDao
import com.moneyflowos.core.database.dao.TransactionDao
import com.moneyflowos.core.database.entity.CorrectionLogEntity
import com.moneyflowos.core.database.entity.PersonEntity
import com.moneyflowos.core.database.entity.SessionEntity
import com.moneyflowos.core.database.entity.TransactionEntity

@Database(
  entities = [
    TransactionEntity::class,
    PersonEntity::class,
    SessionEntity::class,
    CorrectionLogEntity::class,
  ],
  version = 1,
  exportSchema = true,
)
@TypeConverters(DbTypeConverters::class)
abstract class MoneyFlowDatabase : RoomDatabase() {
  abstract fun transactionDao(): TransactionDao
  abstract fun peopleDao(): PeopleDao
  abstract fun sessionDao(): SessionDao
  abstract fun correctionLogDao(): CorrectionLogDao
}


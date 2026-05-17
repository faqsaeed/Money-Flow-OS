package com.moneyflowos.core.database

import android.content.Context
import androidx.room.Room

object MoneyFlowDatabaseFactory {
  const val DB_NAME: String = "moneyflowos.db"

  fun create(context: Context): MoneyFlowDatabase {
    return Room.databaseBuilder(context, MoneyFlowDatabase::class.java, DB_NAME)
      .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
      .build()
  }
}


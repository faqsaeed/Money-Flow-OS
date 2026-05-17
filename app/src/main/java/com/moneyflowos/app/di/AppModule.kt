package com.moneyflowos.app.di

import android.content.Context
import com.moneyflowos.core.common.time.Clock
import com.moneyflowos.core.common.time.SystemClock
import com.moneyflowos.core.database.MoneyFlowDatabase
import com.moneyflowos.core.database.MoneyFlowDatabaseFactory
import com.moneyflowos.core.database.repo.PeopleDaoFacade
import com.moneyflowos.core.database.repo.RoomCorrectionLogRepository
import com.moneyflowos.core.database.repo.RoomPeopleDaoFacade
import com.moneyflowos.core.database.repo.RoomPeopleRepository
import com.moneyflowos.core.database.repo.RoomSessionRepository
import com.moneyflowos.core.database.repo.RoomTransactionRepository
import com.moneyflowos.core.domain.repo.CorrectionLogRepository
import com.moneyflowos.core.domain.repo.PeopleRepository
import com.moneyflowos.core.domain.repo.SessionRepository
import com.moneyflowos.core.domain.repo.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
  @Provides @Singleton fun provideClock(): Clock = SystemClock

  @Provides @Singleton
  fun provideDb(@ApplicationContext context: Context): MoneyFlowDatabase = MoneyFlowDatabaseFactory.create(context)

  @Provides fun provideTransactionDao(db: MoneyFlowDatabase) = db.transactionDao()
  @Provides fun providePeopleDao(db: MoneyFlowDatabase) = db.peopleDao()
  @Provides fun provideSessionDao(db: MoneyFlowDatabase) = db.sessionDao()
  @Provides fun provideCorrectionLogDao(db: MoneyFlowDatabase) = db.correctionLogDao()

  @Provides @Singleton
  fun providePeopleDaoFacade(db: MoneyFlowDatabase): PeopleDaoFacade = RoomPeopleDaoFacade(db.peopleDao())

  @Provides @Singleton
  fun providePeopleRepository(db: MoneyFlowDatabase, clock: Clock): PeopleRepository =
    RoomPeopleRepository(peopleDao = db.peopleDao(), transactionDao = db.transactionDao(), clock = clock)

  @Provides @Singleton
  fun provideSessionRepository(db: MoneyFlowDatabase, clock: Clock): SessionRepository =
    RoomSessionRepository(sessionDao = db.sessionDao(), clock = clock)

  @Provides @Singleton
  fun provideCorrectionLogRepository(db: MoneyFlowDatabase): CorrectionLogRepository =
    RoomCorrectionLogRepository(db.correctionLogDao())

  @Provides @Singleton
  fun provideTransactionRepository(
    db: MoneyFlowDatabase,
    peopleRepository: PeopleRepository,
    peopleDaoFacade: PeopleDaoFacade,
    clock: Clock,
  ): TransactionRepository {
    return RoomTransactionRepository(
      transactionDao = db.transactionDao(),
      correctionLogDao = db.correctionLogDao(),
      peopleRepository = peopleRepository,
      peopleDaoFacade = peopleDaoFacade,
      clock = clock,
    )
  }
}


package com.moneyflowos.app.di

import android.content.Context
import com.moneyflowos.core.common.time.Clock
import com.moneyflowos.core.security.AdminAuthManager
import com.moneyflowos.core.security.AdminSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
  @Provides @Singleton
  fun provideAdminAuthManager(@ApplicationContext context: Context): AdminAuthManager = AdminAuthManager(context)

  @Provides @Singleton
  fun provideAdminSession(clock: Clock): AdminSession = AdminSession(clock = clock)
}


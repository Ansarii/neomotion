package com.neoninnovationlab.neomotion.demo.di

import android.content.Context
import com.neoninnovationlab.neomotion.livejourney.domain.LiveJourneyRepository
import com.neoninnovationlab.neomotion.livejourney.notification.LiveJourneyNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module for the NeoMotion demo app.
 *
 * Provides singletons that are shared across the entire app lifecycle.
 * The [LiveJourneyRepository] is the single source of truth for all journey
 * state — both the UI and the notification layer read from it.
 *
 * MVVM role: Infrastructure / DI configuration. No business logic.
 */
@Module
@InstallIn(SingletonComponent::class)
object NeoMotionModule {

    @Provides
    @Singleton
    fun provideLiveJourneyRepository(): LiveJourneyRepository =
        LiveJourneyRepository()

    @Provides
    @Singleton
    fun provideLiveJourneyNotificationManager(
        @ApplicationContext context: Context,
    ): LiveJourneyNotificationManager =
        LiveJourneyNotificationManager(context)
}

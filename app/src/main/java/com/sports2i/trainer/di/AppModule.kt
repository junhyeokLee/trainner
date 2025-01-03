package com.kadirkuruca.newsapp.di

import android.content.Context
import androidx.room.Room
import com.sports2i.trainer.db.TrackingDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

// 의존성을 위한 Dagger Hilt 라이브러리 사용
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Singleton
    @Provides
    fun provideTrackingDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        TrackingDatabase::class.java,
        TrackingDatabase.DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideEunDao(db: TrackingDatabase) = db.trackingDao()


}


@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
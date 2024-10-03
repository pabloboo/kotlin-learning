package com.pabloboo.runtracker.di

import android.content.Context
import androidx.room.Room
import com.pabloboo.runtracker.db.RunningDatabase
import com.pabloboo.runtracker.utils.Constants.RUNNING_DATABASE_NAME
import com.pabloboo.runtracker.utils.Constants.SHARED_PREF_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDAO()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

}
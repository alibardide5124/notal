package com.alibardide.notal.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.alibardide.notal.data.AppDatabase
import com.alibardide.notal.data.NoteDao
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
    fun providePrefs(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "notal-database"
        ).build()

    @Provides
    @Singleton
    fun provideNoteDao(appDatabase: AppDatabase): NoteDao =
        appDatabase.noteDao
}
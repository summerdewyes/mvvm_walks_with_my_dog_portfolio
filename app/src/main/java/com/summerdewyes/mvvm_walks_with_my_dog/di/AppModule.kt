package com.summerdewyes.mvvm_walks_with_my_dog.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.summerdewyes.mvvm_walks_with_my_dog.db.RunningDatabase
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.KEY_NAME
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.KEY_WEIGHT
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.RUNNING_DATABASE_NAME
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton


/**
 * 제일 처음 실행 됨
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(@ApplicationContext app: Context) =
        Room.databaseBuilder(app, RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(SharedPref : SharedPreferences) = SharedPref.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(SharedPref : SharedPreferences) = SharedPref.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(SharedPref : SharedPreferences) = SharedPref.getBoolean(
        KEY_FIRST_TIME_TOGGLE, true)


}
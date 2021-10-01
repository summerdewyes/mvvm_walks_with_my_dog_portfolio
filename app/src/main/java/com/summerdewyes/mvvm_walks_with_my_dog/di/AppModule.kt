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
 * 제일 처음 실행 됩니다.
 * BaseApplication.kt로 종속성을 주입하고 싶다고 앱에 알리고 실제로 수행 할 AppModule을 만들었습니다.
 */

@Module
@InstallIn(SingletonComponent::class) // 앱 모듈 내부의 객체가 생성되는 시점과 소멸되는 시점을 결정합니다. SingletonComponent는 앱의 전체 수명동안 존재하고 앱이 다시 파괴 될 때 함께 파괴됩니다.
object AppModule {

    // @Singleton 범위 지정으로 SingletonComponent의 인스턴스 당 한 번만 생성되며, 해당 바인딩에 대한 모든 요청은 동일한 인스턴스를 공유합니다.

    // 해당 데이터베이스를 사용할 클래스에 대해 실행 중인 데이터베이스를 생성합니다.
    // Dagger는 Context를 어디에서 가져와야 하는지 알지 못하기 때문에 @ApplicationContext 주석을 추가하여 해결할 수 있습니다.
    @Singleton
    @Provides
    fun provideRunningDatabase(@ApplicationContext app: Context) =
        Room.databaseBuilder(app, RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    // 실행 중인 데이터베이스를 생성하는 방법을 알고 있는 경우에만 Dao를 생성할 수 있습니다.
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
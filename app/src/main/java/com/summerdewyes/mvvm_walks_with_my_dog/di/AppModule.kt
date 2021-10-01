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
 * Component에 연결되어 의존성 객체를 생성하는 역할.
 * 이곳에서는 SingletonComponent 연결되어 앱의 전체 수명동안 존재하고 앱이 다시 파괴 될 때 함께 파괴됩니다.
 *
 * @Singleton 어노테이션은 객체의 수명(Scope)을 지정하고 Dagger에 알려주는 역할을 합니다,
 * 해당 Component의 객체가 살아있는 동안에는 계속 같은 객체를 사용하게 됩니다.
 */

@Module
@InstallIn(SingletonComponent::class) // 앱 모듈 내부의 객체가 생성되는 시점과 소멸되는 시점을 결정합니다.
object AppModule {

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
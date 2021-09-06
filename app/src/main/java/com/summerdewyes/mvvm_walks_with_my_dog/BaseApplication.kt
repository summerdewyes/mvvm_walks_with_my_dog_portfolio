package com.summerdewyes.mvvm_walks_with_my_dog

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * @HiltAndroidApp에 의해서 SingletonComponent가 생성된다.
 * 앱이 살아있는 동안 Dependency를 제공하는 역할
 */
@HiltAndroidApp
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
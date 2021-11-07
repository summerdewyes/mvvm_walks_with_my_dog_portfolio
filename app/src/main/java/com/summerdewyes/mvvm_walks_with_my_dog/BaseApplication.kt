package com.summerdewyes.mvvm_walks_with_my_dog

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * new branch!
 */

/**
 * @HiltAndroidApp에 의해서 SingletonComponent가 생성된다.
 * 앱이 살아있는 동안 Dependency를 제공하는 역할.
 */
@HiltAndroidApp
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree()) // 태그를 생략하고 로그를 찍을 수 있는 라이브러리입니다.
    }
}
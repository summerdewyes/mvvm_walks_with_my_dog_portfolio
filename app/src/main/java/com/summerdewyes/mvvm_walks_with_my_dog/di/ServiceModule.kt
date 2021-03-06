package com.summerdewyes.mvvm_walks_with_my_dog.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.summerdewyes.mvvm_walks_with_my_dog.R
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants
import com.summerdewyes.mvvm_walks_with_my_dog.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)


    /**
     * Base Notification이 생성 되면 MainActivity에 PendingIntent 전달
     */
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also { Intent ->
            Intent.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    /**
     * NotificationCompat.Builder로 Notification 객체를 만듭니다. 이 객체는 기본 알림 정보가 들어있습니다.
     */
    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_paw_theme)
        .setContentTitle("어야가자")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)
}
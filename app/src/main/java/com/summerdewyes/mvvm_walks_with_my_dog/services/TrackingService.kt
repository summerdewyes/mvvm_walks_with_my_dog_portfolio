package com.summerdewyes.mvvm_walks_with_my_dog.services


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.LocaleList
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.summerdewyes.mvvm_walks_with_my_dog.R
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.ACTION_PAUSE_SERVICE
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.ACTION_STOP_SERVICE
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.FASTEST_LOCATION_INTERVAL
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.LOCATION_UPDATE_INTERVAL
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.NOTIFICATION_CHANNEL_ID
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.NOTIFICATION_ID
import com.summerdewyes.mvvm_walks_with_my_dog.other.TrackingUtility
import com.summerdewyes.mvvm_walks_with_my_dog.ui.MainActivity
import timber.log.Timber

//Top level 변수
typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {

    var isFirstRun = true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf()) // mutableListOf()는 읽기, 쓰기가 가능합니다.
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues() //isTracking=false, pathPoints=mutableListOf() 초기화
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        // isTracking을 관찰하여 값이 TRUE이고 위치 퍼미션이 켜져 있으면 위치 업데이트를 요청합니다. 아니라면 업데이트를 중지합니다.
        isTracking.observe(this, {
            updateLocationTracking(it)
        })

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("서비스중..")
                    }
                    Timber.d("서비스 시작")
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("서비스 일시정지")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("서비스 중지")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


    // isTracking이 TRUE일 때  -> addPathPoint
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION : ${location.latitude}, ${location.latitude}")
                    }
                }
            }
        }
    }

    //pathPoints에 현재 위치를 담습니다.
    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.latitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }


    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    private fun startForegroundService() {
        addEmptyPolyline() // 서비스가 시작되면 pathPoints에 Polylines를 담습니다. null이라면 mutableListOf()를 return 합니다.
        isTracking.postValue(true) //서비스가 시작되면 isTracking은 TRUE가 됩니다.

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }


        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("어야가자")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build()) //서비스가 시작될 때 알림을 띄웁니다.
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also { Intent ->
            Intent.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )

    //알림 채널 생성
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}
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
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.summerdewyes.mvvm_walks_with_my_dog.R
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.ACTION_PAUSE_SERVICE
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.ACTION_STOP_SERVICE
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.FASTEST_LOCATION_INTERVAL
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.LOCATION_UPDATE_INTERVAL
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.NOTIFICATION_CHANNEL_ID
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.NOTIFICATION_ID
import com.summerdewyes.mvvm_walks_with_my_dog.other.Constants.TIMER_UPDATE_INTERVAL
import com.summerdewyes.mvvm_walks_with_my_dog.other.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 화면 출력이 필요없는 백그라운드에서 장시간 실행해야 할 업무를 담당합니다.
 */

//Top level 변수 -> 데이터 타입 별칭 선언
typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() { // LifecycleService는 LifecycleOwner를 구현한 Service의 확장 클래스 입니다.

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()

    // 알림 빌더
    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder // 기본 알림
    lateinit var curNotificationBuilder: NotificationCompat.Builder // 바뀌는 알림

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf()) // mutableListOf()는 읽기, 쓰기가 가능합니다.
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues() //isTracking=false, pathPoints=mutableListOf() 초기화
        fusedLocationProviderClient = FusedLocationProviderClient(this) // 위치 서비스 클라이언트 만들기

        /**
         * isTracking을 관찰하여 값이 TRUE이고 위치 퍼미션이 켜져 있으면 위치 업데이트를 요청합니다. 아니라면 업데이트를 중지합니다.
         */
        isTracking.observe(this, {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })

    }

    /**
     * Activity나 Fragment에서 Intent를 보내고 Service에서 처리하는 경우, 액션이 첨부된 인텐트를 보내고 서비스 클래스 내부에서 해당 액션이 무엇인지 확인한 후 그에 따라 행동 할 수 있습니다.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("서비스중..")
                        startTimer()
                    }
                    Timber.d("서비스 시작")
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("서비스 일시정지")
                    pauseService() // 일시정지일 때 isTracking = FALSE
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("서비스 중지")
                    killService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    /**
     * 타이머
     */
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        addEmptyPolyline() // 목록에 빈 폴리라인을 추가해야하고 타이머를 시작할 때마다 true 값을 게시하고 싶습니다.
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        // ANR 오류를 대비하여 코루틴 사용
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted

                // post the new lapTime
                timeRunInMillis.postValue(timeRun + lapTime)

                //1550ms >= 1000ms++
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {

                /**
                 * 위치 업데이트 요청
                 */
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL // 위치 업데이트 수신 간격
                    fastestInterval = FASTEST_LOCATION_INTERVAL // 위치 업데이트 수신에 가장 빠른 간격
                    priority = PRIORITY_HIGH_ACCURACY // 요청의 우선순위를 설정 -> 가장 정확한 위치를 요청
                }

                /**
                 * 위치 요청
                 */
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {

            /**
             * 위치 업데이트 중지
             */
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


    /**
     *  위치 업데이트 콜백 정의
     *  isTracking이 TRUE일 때  -> addPathPoint
     */
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION : ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    /**
     *  pathPoints의 마지막에 현재 위치를 담습니다.
     */
    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }

    /**
     * pathPoints의 첫번째 폴리라인
     */
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf()) // 빈 목록을 추가하여 변경 할 수 있는 목록을 제거
        pathPoints.postValue(this) // 현재 폴리라인의 객체를 참조하고 위에서 변경사항이 추가되었기 때문에 변경 사항에 대해 Fragment에 알립니다.
    } ?: pathPoints.postValue(mutableListOf(mutableListOf())) // 폴리라인 목록을 초기화하고 첫 번째 빈 폴리라인을 추가하여 좌표를 쉽게 추가하고 추적을 시작합니다.


    /**
     * NotificationChannel로 알림 채널 생성
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, // 채널 식별 값
            NOTIFICATION_CHANNEL_NAME, // 채널 이름
            IMPORTANCE_LOW // 알림의 중요도 -> 중간 중요도, 알림음이 울리지 않음.
        )

        notificationManager.createNotificationChannel(channel) // 채널을 NotificationManager에 등록
    }

    /**
     * Foreground Service 시작
     */
    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true) //서비스가 시작되면 isTracking은 TRUE가 됩니다.


        // 1. 알림 생성을 위한 NotificationManager 생성
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 2. 알림 채널 만들기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        // 3. 서비스가 포그라운드에서 실행되도록 요청, Notification Base 객체 생성
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, {
            if (!serviceKilled) {
                // Inject된 알림 객체로 타이머 observe
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                // 4. 알림 띄우기
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }

        })
    }

    /**
     * isTracking에 따라 Notification 객체에 전달할 Pending Intent를 생성, 알림 UI가 업데이트 됩니다.
     */
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "일시정지" else "재시작"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(
                this,
                1,
                pauseIntent,
                FLAG_UPDATE_CURRENT
            ) // FLAG_UPDATE_CURRENT == 이미 생성된 PendingIntent 가 존재하면 해당 intnet 의 extra data 만 변경
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_add_black, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }

    }
}
package com.summerdewyes.mvvm_walks_with_my_dog.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 데이터베이스 테이블 생성
 */
@Entity(tableName = "running_table")
data class Run(
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0,
) {
    /**
     * 기본키 (id) 자동 생성
     */
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
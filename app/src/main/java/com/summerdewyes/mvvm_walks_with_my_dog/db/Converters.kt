package com.summerdewyes.mvvm_walks_with_my_dog.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

/**
 * 구글맵을 캡쳐하면 확장자가 bitmap입니다. 룸 데이터베이스는 온전한 bitmap을 저장하지 못하기 때문에 변환이 필요합니다.
 */
class Converters {

    @TypeConverter
    fun toBitmap(bytes:ByteArray): Bitmap{
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}
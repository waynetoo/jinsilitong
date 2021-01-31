package com.waynetoo.videotv.room.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "ad_info")
data class AdInfo(
    @PrimaryKey
    var md5: String = "",
    var videoName: String = "",
    var id: String = "",
    var storeNo: String? = "",
    var downloadUrl: String = "",
    var modifiedTimes: String = "",
    var fileName: String = "",
    @Ignore
    var currentPosition: Long = 0L,
    var videoAd: Boolean = true
) : Parcelable {
    fun setData(local: AdInfo) {
        fileName = local.fileName
        videoAd = local.videoAd
    }
}


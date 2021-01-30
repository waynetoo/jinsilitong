package com.waynetoo.videotv.room.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable


@Parcelize
@Entity(tableName = "ad_info")
@Serializable
data class AdInfo(
    @PrimaryKey
    val videoName: String = "",
    val id: String = "",
    val storeNo: String? = "",
    val downloadUrl: String = "",
    val md5: String = "",
    val modifiedTimes: String = "",
    var filePath: String = ""
) : java.io.Serializable, Parcelable


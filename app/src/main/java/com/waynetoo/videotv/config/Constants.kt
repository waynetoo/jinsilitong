package com.waynetoo.videotv.config

import android.os.Environment
import com.waynetoo.lib_common.AppContext
import com.waynetoo.lib_common.extentions.Preference
import com.waynetoo.lib_common.extentions.getDeviceId
import com.waynetoo.videotv.room.entity.AdInfo

/**
 * @Author: weiyunl
 * @CreateDate: 2021/1/28 18:00
 * @Description:
 * @UpdateRemark:
 */
object Constants {
    //门店ID
    var storeNo: String by Preference(AppContext, "storeNo", "")

    // 设备唯一id
    var deviceId: String = ""
        get() {
            return if (storeNo.isBlank()) {
                ""
            } else {
                storeNo + AppContext.contentResolver.getDeviceId()
            }
        }

    //
    var playAdList: List<AdInfo> = arrayListOf()

    /**
     * 存放mp4
     */
    val filesMovies = AppContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
    val filesPic = AppContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
}
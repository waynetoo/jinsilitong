package com.waynetoo.videotv.config

import android.os.Environment
import com.waynetoo.lib_common.AppContext
import com.waynetoo.lib_common.extentions.Preference
import com.waynetoo.lib_common.extentions.getDeviceId
import com.waynetoo.videotv.model.AdInfo

/**
 * @Author: weiyunl
 * @CreateDate: 2021/1/28 18:00
 * @Description:
 * @UpdateRemark:
 */
object Constants {
    //门店ID
    var storeNo: String by Preference(AppContext, "storeNo", "")

    //USB 读取文件
    var usbFileRoot: String by Preference(AppContext, "usbFileRoot", "")
//    var usbFileRoot = Environment.getExternalStorageDirectory().absolutePath
//        ?: AppContext.externalCacheDir?.absolutePath ?: AppContext.cacheDir.absolutePath

    //保留前usb地址
//     var sdcardRoot="/sdcard"
    var sdcardRoot = Environment.getExternalStorageDirectory().absolutePath
        ?: AppContext.externalCacheDir?.absolutePath ?: AppContext.cacheDir.absolutePath
        ?: "/sdcard"
//    var sdcardRoot :String by Preference(AppContext, "usbFileRoot", "")

    const val USB_FILE_DIR = "jsltong"

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
    lateinit var playAdList: List<AdInfo>

    /**
     * 存放mp4  先检测外部存储，再检测内部存储
     */
//    val filesMovies = AppContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
//    val filesPic = AppContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//    lateinit var filesMovies: File
//    var filesPic = AppContext.cacheDir
//    val filesMovies = File(Environment.getExternalStorageDirectory(), "movies")
//    val filesPic = File(Environment.getExternalStorageDirectory(), "movies")
}
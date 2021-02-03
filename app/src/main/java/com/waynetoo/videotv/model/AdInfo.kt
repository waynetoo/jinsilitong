package com.waynetoo.videotv.model

data class AdInfo(
    var md5: String = "",
    //不包含后缀名的name
    var videoName: String = "",
    var id: String = "",
    var storeNo: String? = "",
    var downloadUrl: String = "",
    var modifiedTimes: String = "",
    //包含后缀名的name  本地存在后，才会有这个名字
    var fileName: String = "",
    var currentPosition: Long = 0L,
    var isUsbPath: Boolean = false
) {
    fun setData(file: LocalFileAd) {
        fileName = file.fileName
        isUsbPath = file.isUsbPath
    }
}

data class LocalFileAd(
    var md5: String = "",
    //包含后缀名的name
    var fileName: String = "",
    var filePath: String = "",
    var isUsbPath: Boolean = false
)
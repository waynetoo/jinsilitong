package com.waynetoo.videotv.utils

import android.util.Log
import com.liulishuo.okdownload.DownloadTask
import com.waynetoo.lib_common.AppContext
import com.waynetoo.lib_common.extentions.isVideo
import com.waynetoo.videotv.room.AdDatabase
import com.waynetoo.videotv.room.entity.AdInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest

/**
 * @Author: weiyunl
 * @CreateDate: 2021/1/29 13:05
 * @Description:
 * @UpdateRemark:
 */


fun checkUpdate() {
    var job = GlobalScope.launch(Dispatchers.Main) {
        var content = fetchData()
        Log.d("Coroutine", content)
    }


}

suspend fun fetchData(): String {
    delay(5000)
    return "content"
}

/**
 * 删除文件
 */
suspend fun deleteFiles(remoteList: List<AdInfo>) {
    val adDao = AdDatabase.getDatabase(AppContext).adDao()
    adDao.deletePathEmpty()
    val localList = adDao.getAdList()
    val deleteList =
        localList.filterNot { local -> remoteList.any { it.md5 == local.md5 } }
    println("deleteFiles:" + localList)
    adDao.deleteList(deleteList)
    deleteList.forEach {
        //删除数据库 和文件
        File(USBUtils.createFilePath(it.fileName)).delete()
    }
}

/**
 * 同步本地的path 2 Remote
 *
 * 修改，同步本地的文件
 */
suspend fun syncLocal2Remote(remoteList: List<AdInfo>) {
    val adDao = AdDatabase.getDatabase(AppContext).adDao()
    val localList = adDao.getAdList()
    println("syncLocal2Remote:" + localList)

    val createUsbDir = USBUtils.createUsbDir()
    createUsbDir.listFiles()?.forEach {
        println(" begin md5 " + it.name + "   " + System.currentTimeMillis())
        println(it.getFileMD5(16))
        println(" end   md5 " + it.name + "   " + System.currentTimeMillis())
    }

    remoteList.forEach { remote ->
        localList.find { it.md5 == remote.md5 }
            ?.let {
                remote.setData(it)
            }
    }
}

suspend fun getUpdateList(remoteList: List<AdInfo>): List<AdInfo> {
    val adDao = AdDatabase.getDatabase(AppContext).adDao()
    val localList = adDao.getAdList()

    val createUsbDir = USBUtils.createUsbDir()
    val fileName = createUsbDir.list()
    println("getUpdateList:" + localList)
    return remoteList.filterNot { remote ->
        localList.any {
            it.md5 == remote.md5 && fileName.any { name -> it.fileName == name }
        }
    }
}

suspend fun insertUpdateAd(playAdList: List<AdInfo>, task: DownloadTask) {
    val adDao = AdDatabase.getDatabase(AppContext).adDao()
    playAdList.find { it.downloadUrl == task.url }
        ?.let { adInfo ->
            task.file?.let {
                adInfo.videoAd = adInfo.downloadUrl.isVideo()
                adInfo.fileName = it.name

                adDao.insert(adInfo)
            }
        }
}


/**
 * 获取单个文件的MD5值
 * @param file 文件
 * @param radix  位 16 32 64
 *
 * @return
 */
fun File.getFileMD5(radix: Int): String {
    if (!isFile || !exists()) {
        return ""
    }
    var digest: MessageDigest? = null
    var `in`: FileInputStream? = null
    val buffer = ByteArray(8192)
    var len: Int = 0
    try {
        digest = MessageDigest.getInstance("MD5")
        `in` = FileInputStream(this)
        while (`in`.read(buffer, 0, 1024).also({ len = it }) != -1) {
            digest.update(buffer, 0, len)
        }
        `in`.close()
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
    val bigInt = BigInteger(1, digest.digest())
    return bigInt.toString(radix)
}

/**
 * 获取文件夹中文件的MD5值
 *
 * @param file
 * @param listChild
 * ;true递归子目录中的文件
 * @return
 */
fun File.getDirMD5(
    listChild: Boolean
): Map<String, String>? {
    if (!isDirectory) {
        return null
    }
    val map: MutableMap<String, String> =
        HashMap()
    var md5: String?
    val files = listFiles()
    for (i in files.indices) {
        val f = files[i]
        if (f.isDirectory && listChild) {
            map.putAll(f.getDirMD5(listChild)!!)
        } else {
            md5 = f.getFileMD5(16)
            if (md5 != null) {
                map[f.path] = md5
            }
        }
    }
    return map
}
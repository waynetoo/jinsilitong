package com.waynetoo.videotv.utils

import android.util.Log
import com.liulishuo.okdownload.DownloadTask
import com.waynetoo.videotv.model.AdInfo
import com.waynetoo.videotv.model.LocalFileAd
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.security.MessageDigest
import kotlin.experimental.and

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

suspend fun getLocalFiles() = withContext(Dispatchers.IO) {
    val createUsbDir = USBUtils.createUsbDir()
    val createSdcardDir = USBUtils.createSdcardDir()
//    val localList = arrayListOf<LocalFileAd>()
    val localList = HashMap<String, LocalFileAd>()
    //usb中的
    createUsbDir.listFiles()?.forEach {
        localList[it.fileMd5()] = LocalFileAd(
            it.fileMd5(),
            it.name,
            it.absolutePath,
            true
        )
    }
    //sdcard中的
    createSdcardDir.listFiles()?.forEach {
        localList[it.fileMd5()] = LocalFileAd(
            it.fileMd5(),
            it.name,
            it.absolutePath,
            false
        )
    }
    Logger.log("localFiles :$localList")
    localList
}

/**
 * 删除文件
 */
suspend fun deleteFiles(
    localFiles: HashMap<String, LocalFileAd>,
    remoteList: List<AdInfo>
) = withContext(Dispatchers.IO) {
    localFiles.filterKeys { key -> !remoteList.any { it.md5 == key } }
        .forEach {
            //删除数据库 和文件
            if (!it.value.isUsbPath) {
                Logger.log("删除文件：" + it.value.filePath)
                File(it.value.filePath).delete()
            }
        }
}

/**
 * 同步本地的path 2 Remote
 * 本地的 文件  copyFileName 到
 */
suspend fun syncLocal2RemoteAndObtainUpdateList(
    localFiles: HashMap<String, LocalFileAd>,
    remoteList: List<AdInfo>
) =
    withContext(Dispatchers.IO) {
        val updateList = arrayListOf<AdInfo>()
        remoteList.forEach { remote ->
            val find = localFiles[remote.md5]
            if (find == null) {
                updateList.add(remote)
            } else {
                remote.setData(find)
            }
        }
        Logger.log("updateList：$updateList")
        updateList
    }

fun insertUpdateAd(updateList: List<AdInfo>, task: DownloadTask) {
    updateList.find { it.downloadUrl == task.url }
        ?.let { adInfo ->
            task.file?.let {
                adInfo.fileName = it.name
                adInfo.isUsbPath = false
            }
        }
}

fun insertUpdateAdSync(updateList: List<AdInfo>, nowPlay: List<AdInfo>, task: DownloadTask) {
    updateList.find { it.downloadUrl == task.url }
        ?.let { adInfo ->
            task.file?.let {
                adInfo.fileName = it.name
                adInfo.isUsbPath = false
            }
        }
    nowPlay.find { it.downloadUrl == task.url }
        ?.let { adInfo ->
            task.file?.let {
                adInfo.fileName = it.name
                adInfo.isUsbPath = false
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
fun File.fileMd5(): String {
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
        while (`in`.read(buffer, 0, 2048).also { len = it } != -1) {
            digest.update(buffer, 0, len)
        }
        `in`.close()
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
    val bigInt = BigInteger(1, digest.digest())
    var md5 = bigInt.toString(16)
    while (md5.length < 32) {
        md5 = "0$md5"
    }
    return md5
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
            md5 = f.fileMd5()
            if (md5 != null) {
                map[f.path] = md5
            }
        }
    }
    return map
}
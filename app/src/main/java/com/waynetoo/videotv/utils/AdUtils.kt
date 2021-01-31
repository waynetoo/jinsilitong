package com.waynetoo.videotv.utils

import android.util.Log
import com.liulishuo.okdownload.DownloadTask
import com.waynetoo.lib_common.AppContext
import com.waynetoo.lib_common.extentions.isVideo
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.room.AdDatabase
import com.waynetoo.videotv.room.dao.AdDao
import com.waynetoo.videotv.room.entity.AdInfo
import kotlinx.coroutines.*
import java.io.File

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
suspend fun deleteFiles(
    remoteList: List<AdInfo>
) = withContext(Dispatchers.IO) {
    val adDao = AdDatabase.getDatabase(AppContext).adDao()
    val localList = adDao.getAdList()
    val deleteList =
        localList.filterNot { local -> remoteList.any { it.md5 == local.md5 } }
    println("deleteList  $deleteList")
    deleteList.forEach {
        //删除数据库 和文件
        adDao.delete(it)
        File(it.filePath).delete()
    }
}

/**
 * 同步本地的path 2 Remote
 */
suspend fun syncLocal2Remote(remoteList: List<AdInfo>) =
    withContext(Dispatchers.IO) {
        val adDao = AdDatabase.getDatabase(AppContext).adDao()
        adDao.deletePathEmpty()
        val localList = adDao.getAdList()
        remoteList.forEach { remote ->
            val find =
                localList.find { it.md5 == remote.md5 }
            find?.let {
                remote.filePath = it.filePath
                remote.videoAd = it.videoAd
            }
        }
    }

suspend fun getUpdateList(remoteList: List<AdInfo>) =
    withContext(Dispatchers.IO) {
        val adDao = AdDatabase.getDatabase(AppContext).adDao()
        val localList = adDao.getAdList()
        remoteList.filterNot { remote -> localList.any { it.md5 == remote.md5 } }
    }

suspend fun insertUpdateAd(playAdList: List<AdInfo>, task: DownloadTask) =
    withContext(Dispatchers.IO) {
        val adDao = AdDatabase.getDatabase(AppContext).adDao()
        playAdList.find { it.downloadUrl == task.url }
            ?.let {
                it.filePath = task.file?.absolutePath ?: ""
                it.videoAd = it.downloadUrl.isVideo()
                adDao.insert(it)
            }
    }

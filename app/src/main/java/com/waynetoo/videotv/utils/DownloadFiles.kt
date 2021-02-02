package com.waynetoo.videotv.utils

import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.SpeedCalculator
import com.liulishuo.okdownload.core.breakpoint.BlockInfo
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend
import com.waynetoo.lib_common.AppContext
import com.waynetoo.lib_common.extentions.toast
import com.waynetoo.videotv.model.AdInfo
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class DownloadFiles {

    @Volatile
    var updateCount: Int = 0
    lateinit var itemSuccessCallback: (task: DownloadTask) -> Unit
    lateinit var complete: () -> Unit
    var progressCallback: ((String) -> Unit)? = null

    constructor(
        callback: (task: DownloadTask) -> Unit,
        complete: () -> Unit
    ) {
        this.itemSuccessCallback = callback
        this.complete = complete
    }

    constructor(
        callback: (task: DownloadTask) -> Unit,
        complete: () -> Unit,
        progressCallback: (msg: String) -> Unit
    ) {
        this.itemSuccessCallback = callback
        this.complete = complete
        this.progressCallback = progressCallback
    }


    /**
     * 下载文件
     */
    fun downloadFiles(updateList: List<AdInfo>) {
        updateCount = updateList.size
        val tasks: MutableList<DownloadTask> = ArrayList()
        val storeFile = USBUtils.createUsbDir()
        for (ad in updateList) {
            val task = DownloadTask.Builder(ad.downloadUrl, storeFile)
//                .setReadBufferSize(1024 * 8)
//                .setFlushBufferSize(1024 * 32)
//                .setSyncBufferSize(1024 * 64)
                .setConnectionCount(5)
                .build()
            tasks.add(task)
        }
        DownloadTask.enqueue(tasks.toTypedArray(), downloadListener)  //同时异步执行多个任务
    }


    private val downloadListener: DownloadListener4WithSpeed =
        object : DownloadListener4WithSpeed() {

            override fun taskStart(task: DownloadTask) {
                println("taskStart=>" + task.filename)
            }

            override fun blockEnd(
                task: DownloadTask,
                blockIndex: Int,
                info: BlockInfo?,
                blockSpeed: SpeedCalculator
            ) {
                println("blockEnd=>" + task.filename)
            }

            override fun taskEnd(
                task: DownloadTask,
                cause: EndCause,
                realCause: java.lang.Exception?,
                taskSpeed: SpeedCalculator
            ) {
                println("taskEnd=>" + task.filename + " cause=" + cause)
                //下载完成
                if (cause == EndCause.COMPLETED) {
                    --updateCount
                    itemSuccessCallback.invoke(task)
                    AppContext.toast(task.filename + " 下载成功," + "剩余 " + updateCount + "个")
                    if (updateCount <= 0) {
                        complete.invoke()
                    }
                } else if (cause == EndCause.ERROR) {
                    println("taskEnd=>" + task.filename + " realCause=" + realCause)
                }
            }

            override fun progress(
                task: DownloadTask,
                currentOffset: Long,
                taskSpeed: SpeedCalculator
            ) {
                progressCallback?.let { callback ->
                    task.info?.let {
                        if (it.totalLength > 0) {
                            val percent: Int =
                                (currentOffset.toFloat() / it.totalLength * 100).toInt()
                            val msg =
                                task.filename + "  ->  进度：" + percent + "%" + "   速度：" + taskSpeed.speed()
                            callback.invoke(msg)
                        }
                    }
                }
            }

            override fun connectEnd(
                task: DownloadTask,
                blockIndex: Int,
                responseCode: Int,
                responseHeaderFields: MutableMap<String, MutableList<String>>
            ) {
//                println("connectEnd=>" + task.filename)
            }

            override fun connectStart(
                task: DownloadTask,
                blockIndex: Int,
                requestHeaderFields: MutableMap<String, MutableList<String>>
            ) {
//                println("connectStart=>" + task.filename)
            }

            override fun infoReady(
                task: DownloadTask,
                info: BreakpointInfo,
                fromBreakpoint: Boolean,
                model: Listener4SpeedAssistExtend.Listener4SpeedModel
            ) {
                println("infoReady=>" + task.filename)
            }

            override fun progressBlock(
                task: DownloadTask,
                blockIndex: Int,
                currentBlockOffset: Long,
                blockSpeed: SpeedCalculator
            ) {
                println("progressBlock=>" + task.filename)
            }
        }
}
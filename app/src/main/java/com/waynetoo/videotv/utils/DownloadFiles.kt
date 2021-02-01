package com.waynetoo.videotv.utils

import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.SpeedCalculator
import com.liulishuo.okdownload.core.breakpoint.BlockInfo
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend
import com.waynetoo.lib_common.AppContext
import com.waynetoo.lib_common.extentions.toast
import com.waynetoo.videotv.model.AdInfo

class DownloadFiles {

    @Volatile
    var updateCount: Int = 0
    lateinit var itemSuccessCallback: (task: DownloadTask) -> Unit
    lateinit var complete: () -> Unit

    constructor(
        callback: (task: DownloadTask) -> Unit,
        complete: () -> Unit
    ) {
        this.itemSuccessCallback = callback
        this.complete = complete
    }

    /**
     * 下载文件
     */
    fun downloadFiles(updateList: List<AdInfo>) {
        OkDownload.with().downloadDispatcher().cancelAll()

        updateCount = updateList.size
        val tasks: MutableList<DownloadTask> = ArrayList()
        val storeFile = USBUtils.createUsbDir()
        for (ad in updateList) {
            val task = DownloadTask.Builder(ad.downloadUrl, storeFile)
                .setMinIntervalMillisCallbackProcess(5_000)
                .setReadBufferSize(1024 * 8)
                .setFlushBufferSize(1024 * 32)
                .setSyncBufferSize(1024 * 64)
                .setPreAllocateLength(true)
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
                println("taskEnd=>" + task.filename)
                //下载完成
                if (cause == EndCause.COMPLETED) {
                    --updateCount
                    itemSuccessCallback.invoke(task)
                    AppContext.toast(task.filename + " 下载成功," + "剩余 " + updateCount + "个")
                    if (updateCount <= 0) {
                        complete.invoke()
                    }
                }
            }

            override fun progress(
                task: DownloadTask,
                currentOffset: Long,
                taskSpeed: SpeedCalculator
            ) {
                println("progress=>" + task.filename + "  " + currentOffset)
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
package com.waynetoo.videotv.utils

import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.waynetoo.lib_common.extentions.isVideo
import com.waynetoo.lib_common.extentions.toast
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.room.entity.AdInfo
import kotlinx.coroutines.launch

class DownloadFiles {

    @Volatile
    var updateCount: Int = 0
    lateinit var itemSuccessCallback: (task: DownloadTask, remainder: Int) -> Unit
    lateinit var complete: () -> Unit

    constructor(
        callback: (task: DownloadTask, remainder: Int) -> Unit,
        complete: () -> Unit
    ) {
        this.itemSuccessCallback = callback
        this.complete = complete
    }

    /**
     * 下载文件
     */
    fun downloadFiles(updateList: List<AdInfo>) {
        updateCount = updateList.size
        val tasks: MutableList<DownloadTask> = ArrayList()
        val storeFile = USBUtils.createUsbDir()
        for (ad in updateList) {
            val task = DownloadTask.Builder(ad.downloadUrl, storeFile).build()
            tasks.add(task)
        }
        DownloadTask.enqueue(tasks.toTypedArray(), downloadListener)  //同时异步执行多个任务
    }


    private val downloadListener: DownloadListener = object : DownloadListener {
        override fun connectTrialEnd(
            task: DownloadTask,
            responseCode: Int,
            responseHeaderFields: MutableMap<String, MutableList<String>>
        ) {
//            println("connectTrialEnd" +task.filename)
        }

        override fun fetchEnd(task: DownloadTask, blockIndex: Int, contentLength: Long) {
            println("fetchEnd " + task.filename)
        }

        override fun downloadFromBeginning(
            task: DownloadTask,
            info: BreakpointInfo,
            cause: ResumeFailedCause
        ) {
            println("downloadFromBeginning " + task.filename)
        }

        override fun taskStart(task: DownloadTask) {
//            println("taskStart"+task.filename)
        }

        override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
            println("taskEnd " + task.file?.absolutePath)
            //下载完成
            if (cause == EndCause.COMPLETED) {
                itemSuccessCallback.invoke(task, updateCount--)
                if (updateCount <= 0) {
                    complete.invoke()
                }
            }
        }

        override fun connectTrialStart(
            task: DownloadTask,
            requestHeaderFields: MutableMap<String, MutableList<String>>
        ) {
            println("connectTrialStart  " + task.filename)
//            this@InitActivity.toast("connectTrialStart  " + task.toString())
        }

        override fun downloadFromBreakpoint(task: DownloadTask, info: BreakpointInfo) {
            println("downloadFromBreakpoint " + task.filename)
        }

        override fun fetchStart(task: DownloadTask, blockIndex: Int, contentLength: Long) {
            println("fetchStart" + task.filename)
//            this@InitActivity.toast("fetchStart" + task.toString())
        }

        override fun fetchProgress(task: DownloadTask, blockIndex: Int, increaseBytes: Long) {
            println("fetchProgress" + task.filename)
//            this@InitActivity.toast("fetchProgress" + blockIndex.toString())
        }

        override fun connectEnd(
            task: DownloadTask,
            blockIndex: Int,
            responseCode: Int,
            responseHeaderFields: MutableMap<String, MutableList<String>>
        ) {
            println("connectEnd" + task.filename)
        }

        override fun connectStart(
            task: DownloadTask,
            blockIndex: Int,
            requestHeaderFields: MutableMap<String, MutableList<String>>
        ) {
            println("connectStart" + task.filename)
//            this@InitActivity.toast("connectStart" + task.filename)
        }
    }
}
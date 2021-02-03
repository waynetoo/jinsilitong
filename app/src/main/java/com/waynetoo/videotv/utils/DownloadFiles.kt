package com.waynetoo.videotv.utils

import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.SpeedCalculator
import com.liulishuo.okdownload.core.breakpoint.BlockInfo
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend
import com.waynetoo.videotv.model.AdInfo

class DownloadFiles {

    @Volatile
    var updateCount: Int = 0
    lateinit var taskEnd: (task: DownloadTask, cause: EndCause) -> Unit
    var complete: (() -> Unit?)? = null
    var progressCallback: ((String) -> Unit)? = null

    var currentDownloadfileName: String? = null

    constructor(
        taskEnd: (task: DownloadTask, cause: EndCause) -> Unit,
        progressCallback: (msg: String) -> Unit
    ) {
        this.taskEnd = taskEnd
        this.progressCallback = progressCallback
    }

    constructor(
        taskEnd: (task: DownloadTask, cause: EndCause) -> Unit,
        complete: () -> Unit,
        progressCallback: (msg: String) -> Unit
    ) {
        this.taskEnd = taskEnd
        this.complete = complete
        this.progressCallback = progressCallback
    }


    /**
     * 下载文件
     * 下载到sd卡中
     */
    fun downloadFiles(updateList: List<AdInfo>) {
        updateCount = updateList.size
        val tasks: MutableList<DownloadTask> = ArrayList()
        val storeFile = USBUtils.createSdcardDir()
        for (ad in updateList) {
            val task = DownloadTask.Builder(ad.downloadUrl, storeFile).build()
            tasks.add(task)
        }
        DownloadTask.enqueue(tasks.toTypedArray(), downloadListener)  //同时异步执行多个任务
    }

    fun downloadFile(adInfo: AdInfo) {
        updateCount = 1
        val tasks: MutableList<DownloadTask> = ArrayList()
        val storeFile = USBUtils.createSdcardDir()
        val task = DownloadTask.Builder(adInfo.downloadUrl, storeFile).build()
        tasks.add(task)
        DownloadTask.enqueue(tasks.toTypedArray(), downloadListener)  //同时异步执行多个任务
    }


    private val downloadListener: DownloadListener4WithSpeed =
        object : DownloadListener4WithSpeed() {

            override fun taskStart(task: DownloadTask) {
                println("taskStart=>" + task.filename)
//                AppContext.toast("taskStart=>" + task.filename)
//
//                progressCallback?.let { callback ->
//                    callback.invoke("taskStart=>" + task.filename + "\n")
//                }

            }

            override fun blockEnd(
                task: DownloadTask,
                blockIndex: Int,
                info: BlockInfo?,
                blockSpeed: SpeedCalculator
            ) {
                println("blockEnd=>" + task.filename)
//                progressCallback?.let { callback ->
//                    callback.invoke("blockEnd=>" + task.filename + "\n")
//                }
//                AppContext.toast("blockEnd=>" + task.filename)
            }

            override fun taskEnd(
                task: DownloadTask,
                cause: EndCause,
                realCause: java.lang.Exception?,
                taskSpeed: SpeedCalculator
            ) {
                currentDownloadfileName = null
                println("taskEnd=>" + task.filename + " cause=" + cause)
                //下载完成
                --updateCount
                taskEnd.invoke(task, cause)
                if (cause == EndCause.COMPLETED) {
//                  AppContext.toast(task.filename + " 下载成功," + "剩余 " + updateCount + "个")
                } else if (cause == EndCause.ERROR) {
                    println("taskEnd=>" + task.filename + " realCause=" + realCause)
                }

                if (updateCount <= 0) {
                    complete?.invoke()
                }
//                progressCallback?.let { callback ->
//                    callback.invoke("taskEnd=>" + task.filename + " realCause=" + realCause + "   e:" + realCause + "\n")
//                }
//                AppContext.toast("taskEnd=>" + task.filename + " realCause=" + realCause  +"   e:"+realCause)
            }

            override fun progress(
                task: DownloadTask,
                currentOffset: Long,
                taskSpeed: SpeedCalculator
            ) {
                progressCallback?.let { callback ->
//                    task.info?.let {
//                        if (it.totalLength > 0) {
//                            val percent: Int =
//                                (currentOffset.toFloat() / it.totalLength * 100).toInt()
//                            val msg =
//                                task.filename + "  ->  进度：" + percent + "%" + "   速度：" + taskSpeed.speed()
//                            callback.invoke(taskSpeed.speed())
//                        }
//                    }
                    callback.invoke(taskSpeed.speed())
                }
            }

            override fun connectEnd(
                task: DownloadTask,
                blockIndex: Int,
                responseCode: Int,
                responseHeaderFields: MutableMap<String, MutableList<String>>
            ) {
//                println("connectEnd=>" + task.filename)
//                AppContext.toast("connectEnd=>" + task.filename)
//                progressCallback?.let { callback ->
//                    callback.invoke("connectEnd=>" + task.filename + "\n")
//                }
            }

            override fun connectStart(
                task: DownloadTask,
                blockIndex: Int,
                requestHeaderFields: MutableMap<String, MutableList<String>>
            ) {
//                println("connectStart=>" + task.filename)
//                AppContext.toast("connectStart=>" + task.filename)
                progressCallback?.let { callback ->
                    callback.invoke("connectStart=>" + task.filename + "\n")
                }
            }

            override fun infoReady(
                task: DownloadTask,
                info: BreakpointInfo,
                fromBreakpoint: Boolean,
                model: Listener4SpeedAssistExtend.Listener4SpeedModel
            ) {
                currentDownloadfileName = task.filename
                println("infoReady=>" + task.filename)
//                AppContext.toast("infoReady=>" + task.filename)
//                progressCallback?.let { callback ->
//                    callback.invoke("infoReady=>" + task.filename + "\n")
//                }
            }

            override fun progressBlock(
                task: DownloadTask,
                blockIndex: Int,
                currentBlockOffset: Long,
                blockSpeed: SpeedCalculator
            ) {
                println("progressBlock=>" + task.filename)
//                AppContext.toast("progressBlock=>" + task.filename)
//                progressCallback?.let { callback ->
//                    callback.invoke("progressBlock=>" + task.filename + "\n")
//                }
            }
        }
}
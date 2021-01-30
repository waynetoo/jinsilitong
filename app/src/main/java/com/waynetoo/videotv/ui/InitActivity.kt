package com.waynetoo.videotv.ui

import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.View
import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.waynetoo.lib_common.AppContext
import com.waynetoo.lib_common.extentions.*
import com.waynetoo.lib_common.lifecycle.BaseActivity
import com.waynetoo.videotv.R
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.presenter.BinderPresenter
import com.waynetoo.videotv.receiver.USBBroadcastReceiver
import com.waynetoo.videotv.room.AdDatabase
import com.waynetoo.videotv.room.dao.AdDao
import com.waynetoo.videotv.room.entity.AdInfo
import kotlinx.android.synthetic.main.activity_binder.*
import kotlinx.coroutines.launch
import java.lang.Exception


/**
 *
 * 绑定门店id页面
 * 步骤：
 * 1，检查app是否绑定门店id，如果没有绑定 -->绑定id
 * 2，绑定完成后，请求广告列表，检查广告是否有更新 ，如果没有更新 -->进入播放广告页面
 * 3，有更新->下载更新 ----->下载完成后 -->进入播放广告页面
 */
class InitActivity : BaseActivity<BinderPresenter>() {
    private val TAG = "waynetoo"
    private var usbBroadcastReceiver: USBBroadcastReceiver? = null
    lateinit var adDao: AdDao

    @Volatile
    var updateCount: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_binder)
        initListeners()
        checkStoreNo()
        registerReceiver()
    }

    private fun registerReceiver() {
        usbBroadcastReceiver = USBBroadcastReceiver()
        val usbDeviceStateFilter = IntentFilter()
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbBroadcastReceiver, usbDeviceStateFilter)
    }

    private fun initListeners() {
        btn_sure.setOnClickListener {
            checkInput {
                presenter.bindStore(it)
            }
        }
    }

    private fun checkStoreNo() {
        Constants.storeNo.isEmpty().yes {
            // 绑定no
            ll_binder.visibility = View.VISIBLE
            msg.visibility = View.GONE
        }.otherwise {
            ll_binder.visibility = View.GONE
            msg.visibility = View.VISIBLE
            // 请求 广告列表
            presenter.getAdList()
        }
    }

    fun toastError(msg: String) {
        closeProgressDialog()
        toast(msg)
    }

    private fun checkInput(valid: (id: String) -> Unit) {
        if (et_store_no.text.isNullOrBlank()) {
            toast(getString(R.string.login_tel_hint))
        } else if (et_store_no.text.toString().length != 12) {
            toast(R.string.store_id_number_illegal)
        } else {
            valid(et_store_no.text.toString())
        }
    }

    fun bindSuccess(storeNo: String) {
        Constants.storeNo = storeNo
        checkStoreNo()
    }

    /**
     * 获取广告成功
     */
    fun getAdListSuccess(remoteList: List<AdInfo>) {
        if (remoteList.isEmpty()) {
            msg.text = "广告列表为空，请添加广告 。。"
            return
        }
        adDao = AdDatabase.getDatabase(AppContext).adDao()
        launch {
            msg.text = "校验广告中。。。"
            showProgressDialog()
            adDao.deletePathEmpty()
            val localList = adDao.getAdList()
            remoteList.forEach { remote ->
                val find =
                    localList.find { it.videoName == remote.videoName && it.md5 == remote.md5 }
                find?.let {
                    remote.filePath = it.filePath
                }
            }
//            println("localList " + localList)
            val updateList =
                remoteList.filterNot { remote -> localList.any { it.videoName == remote.videoName && it.md5 == remote.md5 } }
            println("updateList" + updateList)


            //播放列表
            Constants.playAdList = remoteList
            if (updateList.isEmpty()) {
                startActivity(Intent(this@InitActivity, MainActivity::class.java))
                finish()
            } else {
                msg.text = "下载广告中。。。"
                //更新列表
                downloadFiles(updateList)

                // 删除广告
                val deleteList =
                    localList.filterNot { local -> remoteList.any { it.videoName == local.videoName } }
                println("deleteList" + deleteList)
            }
        }
    }

    /**
     * 下载文件
     */
    private fun downloadFiles(updateList: List<AdInfo>) {
        updateCount = updateList.size
        val tasks: MutableList<DownloadTask> = ArrayList()
        for (ad in updateList) {
            val storeFile = if (ad.downloadUrl.isVideo()) {
                Constants.filesMovies!!
            } else {
                Constants.filesPic!!
            }
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
            println("fetchEnd" + task.filename)
        }

        override fun downloadFromBeginning(
            task: DownloadTask,
            info: BreakpointInfo,
            cause: ResumeFailedCause
        ) {
            println("downloadFromBeginning" + task.filename)
        }

        override fun taskStart(task: DownloadTask) {
//            println("taskStart"+task.filename)
        }

        override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
            println("taskEnd1 " + task.filename)
            println("taskEnd2 " + task.file?.absolutePath)
            println("taskEnd3 " + Constants.playAdList)
            //下载完成
            if (cause == EndCause.COMPLETED) {
                launch {
                    toast(task.filename + " 下载成功")
                    Constants.playAdList.find { it.videoName == task.filename?.getFileNameNoEx() }
                        ?.let {
                            println("taskEnd4 " + task.file?.absoluteFile)
                            updateCount--
                            it.filePath = task.file?.absolutePath ?: ""
                            adDao.insert(it)
                        }
                    if (updateCount <= 0) {
                        downloadSuccess()
                    }
                }
            }
        }

        override fun connectTrialStart(
            task: DownloadTask,
            requestHeaderFields: MutableMap<String, MutableList<String>>
        ) {
//            println("connectTrialStart"+task.filename)
        }

        override fun downloadFromBreakpoint(task: DownloadTask, info: BreakpointInfo) {
            println("downloadFromBreakpoint" + task.filename)
        }

        override fun fetchStart(task: DownloadTask, blockIndex: Int, contentLength: Long) {
            println("fetchStart" + task.filename)
        }

        override fun fetchProgress(task: DownloadTask, blockIndex: Int, increaseBytes: Long) {
            println("fetchProgress" + task.filename)
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
        }
    }

    private fun downloadSuccess() {
        startActivity(Intent(this@InitActivity, MainActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbBroadcastReceiver)
    }
}

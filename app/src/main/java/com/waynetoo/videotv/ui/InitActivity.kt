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
import com.waynetoo.lib_common.extentions.otherwise
import com.waynetoo.lib_common.extentions.toast
import com.waynetoo.lib_common.extentions.yes
import com.waynetoo.lib_common.lifecycle.BaseActivity
import com.waynetoo.videotv.R
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.presenter.BinderPresenter
import com.waynetoo.videotv.receiver.USBBroadcastReceiver
import com.waynetoo.videotv.room.AdDatabase
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
        val adDao = AdDatabase.getDatabase(AppContext).adDao()
        launch {
            msg.text = "校验广告中。。。"
            val localList = adDao.getAdList()
            val updateList =
                remoteList.filterNot { remote -> localList.any { it.videoName == remote.videoName && it.md5 == remote.md5 } }
            println("updateList" + updateList)
            if (updateList.isEmpty()) {
                //播放列表
                Constants.playAdList = remoteList
                startActivity(Intent(this@InitActivity, MainActivity.javaClass))
            } else {
                msg.text = "更新广告中。。。"
                //更新列表
//                updateList
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
        val tasks: MutableList<DownloadTask> = ArrayList()
        for (ad in updateList) {
            val task = DownloadTask.Builder(ad.downloadUrl!!, Constants.filesMovies!!).build()
            tasks.add(task)
        }
        DownloadTask.enqueue(tasks.toTypedArray(), downloadListener); //同时异步执行多个任务
    }

    private val downloadListener: DownloadListener = object : DownloadListener {

        override fun connectTrialEnd(
            task: DownloadTask,
            responseCode: Int,
            responseHeaderFields: MutableMap<String, MutableList<String>>
        ) {
            println("connectTrialEnd")
        }

        override fun fetchEnd(task: DownloadTask, blockIndex: Int, contentLength: Long) {
            println("fetchEnd")
        }

        override fun downloadFromBeginning(
            task: DownloadTask,
            info: BreakpointInfo,
            cause: ResumeFailedCause
        ) {
            println("downloadFromBeginning")
        }

        override fun taskStart(task: DownloadTask) {
            println("taskStart")
        }

        override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
            println("taskEnd")
        }

        override fun connectTrialStart(
            task: DownloadTask,
            requestHeaderFields: MutableMap<String, MutableList<String>>
        ) {
            println("connectTrialStart")
        }

        override fun downloadFromBreakpoint(task: DownloadTask, info: BreakpointInfo) {
            println("downloadFromBreakpoint")
        }

        override fun fetchStart(task: DownloadTask, blockIndex: Int, contentLength: Long) {
            println("fetchStart")
        }

        override fun fetchProgress(task: DownloadTask, blockIndex: Int, increaseBytes: Long) {
            println("fetchProgress")
        }

        override fun connectEnd(
            task: DownloadTask,
            blockIndex: Int,
            responseCode: Int,
            responseHeaderFields: MutableMap<String, MutableList<String>>
        ) {
            println("connectEnd")
        }

        override fun connectStart(
            task: DownloadTask,
            blockIndex: Int,
            requestHeaderFields: MutableMap<String, MutableList<String>>
        ) {
            println("connectStart")
        }
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbBroadcastReceiver)
    }
}

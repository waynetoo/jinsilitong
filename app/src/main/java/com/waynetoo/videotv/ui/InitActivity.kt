package com.waynetoo.videotv.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.waynetoo.lib_common.extentions.checkPermissions
import com.waynetoo.lib_common.extentions.isIntentExisting
import com.waynetoo.lib_common.extentions.toast
import com.waynetoo.lib_common.lifecycle.BaseActivity
import com.waynetoo.videotv.R
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.model.AdInfo
import com.waynetoo.videotv.presenter.BinderPresenter
import com.waynetoo.videotv.receiver.USBBroadcastReceiver
import com.waynetoo.videotv.utils.*
import com.waynetoo.videotv.utils.DocumentsUtils.saveTreeUri
import kotlinx.android.synthetic.main.activity_binder.*
import kotlinx.coroutines.launch
import java.io.File


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
//        registerReceiver()
        checkPermissions(
            getString(R.string.storage_status_tips),
            {
                checkStoreNoAndUsb()
                printMsg()
            },
            { finish() },
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun printMsg() {
        print_msg.text = "sdk：" + Build.VERSION.SDK_INT + "\n" +
                "U盘path:" + Constants.usbFileRoot + "\n" +
                "  rootPath可写：" + File(Constants.usbFileRoot).canWrite() + "\n" +
                "  rootPath可读：" + File(Constants.usbFileRoot).canRead() + "\n" +

                " video文件路径：" + USBUtils.createUsbDir() + "\n" +
                "  可写：" + USBUtils.createUsbDir().canWrite() + "\n" +
                "  可读：" + USBUtils.createUsbDir().canRead()
    }

//    private fun registerReceiver() {
//        usbBroadcastReceiver = USBBroadcastReceiver()
//        val usbDeviceStateFilter = IntentFilter(Intent.ACTION_MEDIA_MOUNTED)
//        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED)
//        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_REMOVED)
//        usbDeviceStateFilter.addDataScheme("file");//没有这行监听不起作用
//        registerReceiver(usbBroadcastReceiver, usbDeviceStateFilter)
//    }

    private fun initListeners() {
        btn_sure.setOnClickListener {
            checkInput {
                presenter.bindStore(it)
            }
        }
    }

    /**
     * 检查门店和USB
     */
    private fun checkStoreNoAndUsb() {
        //先获取USb的读取权限
        if (Constants.storeNo.isEmpty()) {
            // 绑定no
            ll_binder.visibility = View.VISIBLE
            msg.visibility = View.GONE
            et_store_no.requestFocus()
            et_store_no.setText("320201215431")
        } else if (!USBUtils.isUsbEnable()) {
            toast(R.string.usb_notice)
            ll_binder.visibility = View.VISIBLE
            msg.visibility = View.GONE
            btn_sure.requestFocus()
            et_store_no.setText(Constants.storeNo)
        } else {
            // 请求 广告列表
            getAdList()
        }
    }

    fun toastError(msg: String) {
        closeProgressDialog()
        toast(msg)
    }

    private fun checkInput(valid: (id: String) -> Unit) {
        if (!isConnectIsNormal) {
            toast(getString(R.string.net_err))
        } else if (et_store_no.text.isNullOrBlank()) {
            toast(getString(R.string.login_tel_hint))
        } else if (et_store_no.text.toString().length != 12) {
            toast(R.string.store_id_number_illegal)
        } else if (!USBUtils.isUsbEnable()) {
            toast(R.string.usb_notice)
        } else {
            valid(et_store_no.text.toString())
        }
    }

    private fun getAdList() {
        checkUsbWritable {
            presenter.getAdList()
        }
    }

    /**
     *  * 其中 DocumentsUtils.checkWritableRootPath() 方法用来检查 SD 卡根目录是否有写入权限，
     * 如果没有则跳转到权限请求；DocumentsUtils.saveTreeUri() 方法保存返回的 Uri 信息到本地存储，以便之后查询。
     */
    private fun checkUsbWritable(valid: () -> Unit) {
        println(" ======checkUsbWritable=========")
        //false 表示可以写，true 表示不可以写
        if (DocumentsUtils.checkWritableRootPath(this, Constants.usbFileRoot)) {
            showOpenDocumentTree()
        } else {
            valid.invoke()
        }
    }

    fun bindSuccess(storeNo: String) {
        Constants.storeNo = storeNo
        checkStoreNoAndUsb()
    }

    /**
     * 获取广告成功
     */
    fun getAdListSuccess(remoteList: List<AdInfo>) {
        ll_binder.visibility = View.GONE
        msg.visibility = View.VISIBLE
        //播放列表
        Constants.playAdList = remoteList
        if (remoteList.isEmpty()) {
            msg.text = "广告列表为空，请添加广告 。。"
            return
        }
        launch {
            msg.text = "校验广告中。。。"
            showProgressDialog()

            val localFiles = getLocalFiles()
            val updateList = syncLocal2RemoteAndObtainUpdateList(localFiles, remoteList)
            // 删除广告
            deleteFiles(localFiles, remoteList)

            if (updateList.isEmpty()) {
                startActivity(Intent(this@InitActivity, MainActivity::class.java))
                finish()
            } else {
                msg.text = "下载广告中。。。"
                //更新列表
                DownloadFiles({ task ->
                    insertUpdateAd(updateList, task)
                }, {
                    downloadSuccess()
                }, {
                    progress.text = it
                }).downloadFiles(updateList)
            }
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
//        unregisterReceiver(usbBroadcastReceiver)
    }

    private fun showOpenDocumentTree() {
        var intent: Intent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val sm: StorageManager = getSystemService(StorageManager::class.java)
            val volume = sm.getStorageVolume(File(Constants.usbFileRoot))
            if (volume != null) {
                intent = volume.createAccessIntent(null)
            }
        }
        if (intent == null) {
            if (isIntentExisting(Intent.ACTION_OPEN_DOCUMENT_TREE)) {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            } else {
                toast("ACTION_OPEN_DOCUMENT_TREE not exist")
            }
        }
        if (intent != null) {
            startActivityForResult(intent, DocumentsUtils.OPEN_DOCUMENT_TREE_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            DocumentsUtils.OPEN_DOCUMENT_TREE_CODE -> if (data != null && data.data != null) {
                val uri: Uri = data.data
                saveTreeUri(this, Constants.usbFileRoot, uri)
                println("saveTreeUri  " + Constants.usbFileRoot + " --> " + uri)
                getAdList()
            }
            else -> {
            }
        }
    }

    /**
     * 通过监听keyUp   实现双击返回键退出程序
     * @param keyCode
     * @param event
     * @return
     */
    //记录用户首次点击返回键的时间
    private var firstTime: Long = 0
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() === KeyEvent.ACTION_UP) {
            val secondTime = System.currentTimeMillis()
            if (secondTime - firstTime > 2000) {
                Toast.makeText(applicationContext, "再按一次退出程序", Toast.LENGTH_SHORT).show()
                firstTime = secondTime
                return true
            } else {
                finish()
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}

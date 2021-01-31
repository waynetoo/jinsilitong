package com.waynetoo.videotv.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import com.waynetoo.lib_common.AppContext
import com.waynetoo.lib_common.extentions.toast
import com.waynetoo.lib_common.lifecycle.BaseActivity
import com.waynetoo.videotv.R
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.mqtt.MyMqttService
import com.waynetoo.videotv.presenter.MainPresenter
import com.waynetoo.videotv.receiver.USBBroadcastReceiver
import com.waynetoo.videotv.room.AdDatabase
import com.waynetoo.videotv.room.entity.AdInfo
import com.waynetoo.videotv.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class MainActivity : BaseActivity<MainPresenter>() {
    private val TAG = "MainActivity"
    private var mIntent: Intent? = null
    private var usbBroadcastReceiver: USBBroadcastReceiver? = null
    lateinit var playAdList: List<AdInfo>
    lateinit var currentPlay: AdInfo
    var isInsertAd: Boolean = false

    companion object {
        const val WHAT_INSERT_AD = 0x01
        const val WHAT_DELAY_PIC_END = 0x02
        const val PIC_SHOW_TIME = 10_000L
    }

    val handler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                WHAT_INSERT_AD -> {
                    try {
                        var code = msg.data.getString("code")
                        val insertAd = playAdList.find { it.videoName == code }
                        if (insertAd == null) {
                            toast("没有找到此条码的广告")
                        } else {
                            //插入广告
                            isInsertAd = true
                            if (playerView.isPlaying) {
                                playerView.pause()
                                currentPlay.currentPosition = playerView.currentPosition
                            }
                            play(insertAd)
                        }
                    } catch (ex: Throwable) {
                        ex.printStackTrace()
                    }
                }
                WHAT_DELAY_PIC_END -> {
                    if (isInsertAd) {
                        isInsertAd = false
                        playNext(true)
                    } else {
                        playNext()
                    }
                }
                else -> {
                    Log.d(TAG, "handler else")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initVideoComponent()
        initData()
        initMqtt()
//        registerReceiver()
    }

    private fun initData() {
        playAdList = Constants.playAdList
        currentPlay = playAdList[0]
        play(currentPlay)
    }
//
//    private fun registerReceiver() {
//        usbBroadcastReceiver = USBBroadcastReceiver()
//        val usbDeviceStateFilter = IntentFilter()
//        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
//        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
//        registerReceiver(usbBroadcastReceiver, usbDeviceStateFilter)
//    }

    private fun initMqtt() {
        mIntent = Intent(this@MainActivity, MyMqttService::class.java)
        //开启服务
        startService(mIntent)
    }

    fun toastError(msg: String) {
        closeProgressDialog()
        toast(msg)
    }

    fun getAdListSuccess(remoteList: List<AdInfo>) {
        if (remoteList.isEmpty()) {
            toast("广告列表为空，请添加广告 。。")
            startActivity(
                Intent(this, InitActivity::class.java)
            )
            finish()
            return
        }
        //播放列表
        Constants.playAdList = remoteList
        launch {
            syncLocal2Remote(remoteList)
            // 删除广告
//            deleteFiles(remoteList)
            val updateList = getUpdateList(remoteList)
            println("updateList$updateList")
            if (updateList.isNotEmpty()) {
                toast("下载广告中。。。")
                //更新列表
                DownloadFiles({ task, remainder ->
                    launch {
                        insertUpdateAd(Constants.playAdList, task)
                        toast(task.filename + " 下载成功," + "剩余 " + remainder + "个")
                    }
                }, {
                    initData()
                }).downloadFiles(updateList)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun initVideoComponent() {
        lifecycle.addObserver(playerView)
        playerView.apply {
            initPlayer()
            playerCallback = { playWhenReady, state ->
                if (state == Player.STATE_ENDED) {  // 播放结束
                    if (isInsertAd) {
                        isInsertAd = false
                        playNext(true)
                    } else {
                        playNext()
                    }
                } else {
                    if (state == Player.STATE_READY && playWhenReady) {  // 播放中
                        println("playerCallback  播放中")
                    } else if (state == Player.STATE_READY && !playWhenReady) {  // 暂停中
                        println("playerCallback  暂停中")
                        //?
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(mIntent)
//        unregisterReceiver(usbBroadcastReceiver)
    }

    //播放下一个
    fun playNext(view: View) {
        playNext()
    }

    private fun playNext(isRestore: Boolean = false) {
        if (playAdList.isNotEmpty()) {
            if (!isRestore) {
                val currentIndex = playAdList.indexOf(currentPlay)
                val index = (currentIndex + 1) % playAdList.size
//                toast(index.toString())
                currentPlay = playAdList[index]
                currentPlay.currentPosition = 0L
            }
            play(currentPlay)
        }
    }

    /**
     * 播放
     */
    private fun play(adInfo: AdInfo) {
        println("play :" + adInfo.filePath)
        if (adInfo.videoAd) {
            playerView.setSource(adInfo.filePath)
            playerView.start()
            playerView.seekTo(adInfo.currentPosition)
            println("seekTo :" + adInfo.currentPosition)
            if (!playerView.isVisible) {
                playerView.visibility = View.VISIBLE
                imageView.visibility = View.GONE
            }
        } else {
            //暂停视频
            Glide.with(this).load(File(adInfo.filePath)).into(imageView)
            handler.removeMessages(WHAT_DELAY_PIC_END)
            handler.sendEmptyMessageDelayed(WHAT_DELAY_PIC_END, PIC_SHOW_TIME)
            if (playerView.isVisible) {
                playerView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
            }
        }
    }

    //接收扫码机信息
    fun scanCode(view: View) {
//        6901028936477
//        扫码 获得的code
        val code =   playAdList[(playAdList.indices).random() % playAdList.size].videoName
        handler.removeMessages(WHAT_INSERT_AD)
        val msg = Message.obtain()
        msg.what = WHAT_INSERT_AD
        val bundle = Bundle()
        bundle.putString("code", code)
        msg.data = bundle;//mes利用Bundle传递数据
        handler.sendMessage(msg)
    }

    fun undateAd(view: View) {
        presenter.getAdList()
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

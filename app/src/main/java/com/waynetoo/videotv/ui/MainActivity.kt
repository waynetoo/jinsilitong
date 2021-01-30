package com.waynetoo.videotv.ui

import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.View
import com.google.android.exoplayer2.Player
import com.waynetoo.lib_common.extentions.toast
import com.waynetoo.lib_common.lifecycle.BaseActivity
import com.waynetoo.videotv.R
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.mqtt.MyMqttService
import com.waynetoo.videotv.presenter.MainPresenter
import com.waynetoo.videotv.receiver.USBBroadcastReceiver
import com.waynetoo.videotv.room.dao.AdDao
import com.waynetoo.videotv.room.entity.AdInfo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity<MainPresenter>() {
    private val TAG = "MainActivity"
    private var mIntent: Intent? = null
    private var usbBroadcastReceiver: USBBroadcastReceiver? = null
    lateinit var playAdList: List<AdInfo>
    lateinit var currentPlay: AdInfo

    companion object {
//        const val STREAM_URL = "http://vimg.zijinshan.org/portal/news/video/1554103229199.mp4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initVideoComponent()
        initData()
        initMqtt()
        registerReceiver()
    }

    private fun initData() {
        playAdList = Constants.playAdList
        currentPlay = Constants.playAdList[0]
        playerView.setSource(currentPlay.filePath)
    }

    private fun registerReceiver() {
        usbBroadcastReceiver = USBBroadcastReceiver()
        val usbDeviceStateFilter = IntentFilter()
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbBroadcastReceiver, usbDeviceStateFilter)
    }

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
        println(this)
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
                    playNext()
                } else {
                    if (state == Player.STATE_READY && playWhenReady) {  // 播放中

                    } else if (state == Player.STATE_READY && !playWhenReady) {  // 暂停中

                    }
                }
            }
            onVideoStateReady = {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(mIntent)
        unregisterReceiver(usbBroadcastReceiver)
    }

    fun playNext(view: View) {
        playNext()
    }

    fun playNext() {
        val currentIndex = playAdList.indexOf(currentPlay)
        val index = (currentIndex + 1) % playAdList.size
        currentPlay = Constants.playAdList[index]
        toast(index.toString())
        playerView.setSource(currentPlay.filePath)
    }

    fun scanCode(view: View) {

    }

    fun undateAd(view: View) {

    }
}

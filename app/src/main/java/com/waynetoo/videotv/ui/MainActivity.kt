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
import androidx.annotation.Nullable
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import com.jeremyliao.liveeventbus.LiveEventBus
import com.waynetoo.lib_common.extentions.isVideo
import com.waynetoo.lib_common.extentions.toast
import com.waynetoo.lib_common.lifecycle.BaseActivity
import com.waynetoo.videotv.R
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.model.AdInfo
import com.waynetoo.videotv.mqtt.MyMqttService
import com.waynetoo.videotv.presenter.MainPresenter
import com.waynetoo.videotv.receiver.USBBroadcastReceiver
import com.waynetoo.videotv.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File


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
        initObserve()
//        registerReceiver()
    }

    private fun initObserve() {
        LiveEventBus
            .get("LV_RECEIVE_MSG", String::class.java)
            .observe(this, Observer<String> {
                toast(it)
                //320121109489#6901028111027
                val split = it.split("#")
                if (split.size > 1) {
                    if (split[0] == Constants.storeNo) {
                        scanCode(split[1])
                    }
                } else {
                    presenter.getAdList()
                }
            })
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

            val localFiles = getLocalFiles()
            //与播放列表对比  远程有 ，播放列表没有
            val updateList = syncLocal2RemoteAndObtainUpdateList(localFiles, remoteList)
            // 删除广告
//            deleteFiles(remoteList)

            //播放列表有,远程没有
            val updatePlayList =
                playAdList.filterNot { play -> remoteList.any { it.md5 == play.md5 } }
//            println("updateList$updateList")
            if (updateList.isNotEmpty()) {
                //图片不考察
                if (playerView.isPlaying) {
                    playerView.pause()
                }
                toast("下载更新中... 暂停播放...")
                //更新列表
                DownloadFiles({ task ->
                    runBlocking {
                        insertUpdateAd(Constants.playAdList, task)
                    }
                }, {
                    initData()
                }).downloadFiles(updateList)
            } else if (updatePlayList.isNotEmpty()) {
                toast("广告更新，请稍后...")
                initData()
            } else {
                toast("暂无更新...")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initMqtt()
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
//                        println("playerCallback  播放中")
                    } else if (state == Player.STATE_READY && !playWhenReady) {  // 暂停中
//                        println("playerCallback  暂停中")
                        //?
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopService(mIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
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
//                println(index.toString())
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
//        println("play :" + USBUtils.createFilePath(adInfo.fileName))
//        toast("play :" + USBUtils.createFilePath(adInfo.fileName))
        if (adInfo.fileName.isVideo()) {
            playerView.setSource(USBUtils.createFilePath(adInfo.fileName))
            playerView.start()
            playerView.seekTo(adInfo.currentPosition)
//            println("seekTo :" + adInfo.currentPosition)
            if (!playerView.isVisible) {
                playerView.visibility = View.VISIBLE
                imageView.visibility = View.GONE
            }
        } else {
            //暂停视频
            Glide.with(this).load(File(USBUtils.createFilePath(adInfo.fileName))).into(imageView)
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
        val code = playAdList[(playAdList.indices).random() % playAdList.size].videoName
        scanCode(code)
    }

    private fun scanCode(code: String) {
        handler.removeMessages(WHAT_INSERT_AD)
        val msg = Message.obtain()
        msg.what = WHAT_INSERT_AD
        val bundle = Bundle()
        bundle.putString("code", code)
        msg.data = bundle//mes利用Bundle传递数据
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
                System.exit(0)
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}

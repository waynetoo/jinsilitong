package com.waynetoo.videotv.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import com.jeremyliao.liveeventbus.LiveEventBus
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.core.cause.EndCause
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
import java.io.File
import kotlin.system.exitProcess


class MainActivity : BaseActivity<MainPresenter>() {
    private val TAG = "MainActivity"
    private var mIntent: Intent? = null
    private var usbBroadcastReceiver: USBBroadcastReceiver? = null
    lateinit var playAdList: List<AdInfo>
    lateinit var currentPlay: AdInfo

    @Volatile
    var isInsertAd: Boolean = false

    @Volatile
    var flushAdList: Boolean = false

    //需要升级的
    var downLoadList: List<AdInfo>? = null
    lateinit var download: DownloadFiles


    companion object {
        const val WHAT_INSERT_AD = 0x01
        const val WHAT_DELAY_PIC_END = 0x02
        const val PIC_SHOW_TIME = 10_000L
        const val WHAT_DOWN_LOAD = 0x03
    }

    val handler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                WHAT_INSERT_AD -> {
                    try {
                        var code = msg.data.getString("code")
                        val insertAd =
                            playAdList.find { it.videoName == code && !TextUtils.isEmpty(it.fileName) }
                        if (insertAd == null) {
                            toast("没有找到此条码的广告")
                        } else {
                            //插入广告
//                            appendMsg("插入广告 =>" + insertAd.fileName)
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
                WHAT_DOWN_LOAD -> {
//                    appendMsg(" 准备下载0  =>"+downLoadList)
                    //当前不在播放的视频
                    val find = downLoadList?.find {
                        TextUtils.isEmpty(it.fileName) && it.videoName != currentPlay.videoName
                    }
                    if (null == find) {
                        val find = downLoadList?.find {
                            TextUtils.isEmpty(it.fileName)
                        }
                        //确实没有了
                        if (find == null) {
                            flushAdList = true
                            tv_speed.text = ""
                            Logger.log(" 下载完成 --确实没有了")
                            appendMsg(" 下载完成 -- 等待 更新广告列表")
                        } else {
//                            appendMsg(" 下载广告正在播放 15s后重试->  ")
                            //15s后继续下载
                            sendEmptyMessageAtTime(WHAT_DOWN_LOAD, 15_000)
                        }
                    } else {
//                        appendMsg(" 准备下载2 =>$find")
                        download.downloadFile(find)
                    }
                }
                else -> {
                    Logger.log("handler else")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setContentView(R.layout.activity_main)
        initVideoComponent()
        initDownLoaderListener()
        initData()
        initObserve()
        initMqtt()
        presenter.getAdList()
//        registerReceiver()
    }

    private fun initDownLoaderListener() {
        download = DownloadFiles({ task, cause ->
            if (cause == EndCause.COMPLETED) {
                insertUpdateAdSync(Constants.playAdList, playAdList, task)
                println("insertUpdateAdSync playAdList :$playAdList")
                appendMsg("${task.filename} 下载 成功")
                //下载下一个
                handler.sendEmptyMessage(WHAT_DOWN_LOAD)
            } else if (cause == EndCause.CANCELED) {
//                appendMsg("取消下载 downLoadList$downLoadList")
            } else {
//                appendMsg("下载异常  cause" + cause +"  10s后重试 ")
                handler.sendEmptyMessageDelayed(WHAT_DOWN_LOAD, 10_000)
            }
        }, {
            tv_speed.text = it
        })
    }

    private fun initObserve() {
        LiveEventBus
            .get("LV_RECEIVE_MSG", String::class.java)
            .observe(this, Observer<String> {
//                toast(it)
                //320121109489#6901028111027
                val split = it.split("#")
                if (split.size > 1) {
                    if (split[0] == Constants.storeNo) {
                        if (split[1].contains("updateAd", true)) {
//                            appendMsg("开始更新 ：" + it)
                            presenter.getAdList()
                        } else {
                            scanCode(split[1])
                        }
                    }
                }
            })
    }

    private fun initData() {
        playAdList = Constants.playAdList
        Logger.log("initData->playAdList=> $playAdList")
//        appendMsg("initData->playAdList=> $playAdList")
        currentPlay = playAdList.first { !TextUtils.isEmpty(it.fileName) }
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
            startActivity(Intent(this, InitActivity::class.java))
            finish()
            return
        }
        //播放列表
        Constants.playAdList = remoteList
        launch {
            val localFiles = getLocalFiles()
            //与播放列表对比  远程有 ，播放列表没有
            downLoadList = syncLocal2RemoteAndObtainUpdateList(localFiles, remoteList)
//            appendMsg("需要下载的文件：downLoadList ：$downLoadList")
            if (downLoadList!!.isNotEmpty()) {
                Logger.log("准备下载 downLoadList$downLoadList")
                appendMsg("准备下载 downLoadList$downLoadList")
                //删除了数据
                OkDownload.with().downloadDispatcher().cancelAll()
                handler.sendEmptyMessage(WHAT_DOWN_LOAD)
            } else {
                //播放列表有,远程没有
                val updatePlayList =
                    playAdList.filterNot { play -> remoteList.any { it.md5 == play.md5 && it.id == play.id } }
                if (updatePlayList.isNotEmpty()) {
                    Logger.log("updateList$updatePlayList")
                    Logger.log("广告有删除 或者变更顺序")
                    flushAdList = true
                } else {
                    Logger.log("广告没有更新")
                }
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
                    Logger.log("playerCallback  播放结束" + currentPlay.fileName)
                    if (isInsertAd) {
                        isInsertAd = false
                        playNext(true)
                    } else {
                        playNext()
                    }
                } else {
                    if (state == Player.STATE_READY && playWhenReady) {  // 播放中
                        Logger.log("playerCallback  播放中")
//                        appendMsg("playerCallback==>:" + currentPlay.fileName + "   播放中"  )
                    } else if (state == Player.STATE_READY && !playWhenReady) {  // 暂停中
                        Logger.log("playerCallback  暂停中")
//                        appendMsg("playerCallback ==>:" + currentPlay.fileName + "   暂停中"  )
                        //?
                    }
                }


                when (state) {
                    Player.STATE_BUFFERING -> {
//                        Log.d(TAG_VIDEO, "onPlayerStateChanged - STATE_BUFFERING")
                        appendMsg("playerCallback ==>:" + currentPlay.fileName + "   STATE_BUFFERING")
                    }
                    Player.STATE_READY -> {
//                        Log.d(TAG_VIDEO, "onPlayerStateChanged - STATE_READY")
                        appendMsg("playerCallback ==>:" + currentPlay.fileName + "   STATE_READY    " + playWhenReady)
                    }
                    Player.STATE_IDLE -> {
                        appendMsg("playerCallback==>:" + currentPlay.fileName + "   STATE_IDLE")
                    }
                    Player.STATE_ENDED -> {
                        appendMsg("playerCallback ==>:" + currentPlay.fileName + "   STATE_ENDED")
                    }
                }

            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        OkDownload.with().downloadDispatcher().cancelAll()
        stopService(mIntent)
//        unregisterReceiver(usbBroadcastReceiver)
    }

    //播放下一个
    fun playNext(view: View) {
        playNext()
    }

    private fun playNext(isRestore: Boolean = false) {
//        appendMsg("begin  0播放下一个  ")
        //是否是恢复
        if (!isRestore) {
//            appendMsg("begin  1播放下一个  ")
            //如果需要刷新list ，重新播放
            if (flushAdList) {
                flushAdList = false
//                appendMsg(" flushAdList ==> initData")
                initData()
                return
            }
//            appendMsg("begin  3播放下一个  ")
// Logger.log(index.toString())
            currentPlay = getNextAd()
            currentPlay.currentPosition = 0L
        }
        play(currentPlay)
    }

    /**
     * 获取下一个可以播放的广告
     */
    private fun getNextAd(): AdInfo {
        Logger.log("getNextAd1 ==>:" + currentPlay.fileName)

        appendMsg("getNextAd1 ==>:" + currentPlay.fileName)
        var currentIndex = playAdList.indexOf(currentPlay)
        var next = playAdList[(++currentIndex) % playAdList.size]
        while (TextUtils.isEmpty(next.fileName) || checkDownloadName(next)) {
            next = playAdList[(++currentIndex) % playAdList.size]
        }
        Logger.log("getNextAd2 ==>:" + next.fileName)
        appendMsg("getNextAd2 ==>:" + next.fileName)
        return next
    }

    private fun checkDownloadName(next: AdInfo): Boolean {
        return download.currentDownloadfileName == next.fileName
    }

    /**
     * 播放
     */
    private fun play(adInfo: AdInfo) {
        Logger.log("play ==>:" + adInfo.fileName)
        appendMsg("开始播放 ==>:" + USBUtils.createFilePath(adInfo))
//        toast("play :" + USBUtils.createFilePath(adInfo.fileName))
        if (adInfo.fileName.isVideo()) {
            playerView.setSource(USBUtils.createFilePath(adInfo))
            playerView.start()
            playerView.seekTo(adInfo.currentPosition)
//            Logger.log("seekTo :" + adInfo.currentPosition)
            if (!playerView.isVisible) {
                playerView.visibility = View.VISIBLE
                imageView.visibility = View.GONE
            }
        } else {
            //暂停视频
            Glide.with(this).load(File(USBUtils.createFilePath(adInfo))).into(imageView)
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

    //
    fun appendMsg(mgs: String) {
        print_msg.append(mgs + "\n")
        print_msg.post { scroller.fullScroll(View.FOCUS_DOWN); }
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
                exitProcess(0)
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}

package com.waynetoo.videotv.mqtt

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import com.jeremyliao.liveeventbus.LiveEventBus
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.utils.Logger
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

/**
 * Desc  ${MQTT服务}
 */
class MyMqttService : Service() {
    val TAG = MyMqttService::class.java.simpleName
    private var mMqttConnectOptions: MqttConnectOptions? = null
    var HOST = "tcp://39.99.150.10:61613" //服务器地址（协议+地址+端口号）
    var USERNAME = "admin" //用户名
    var PASSWORD = "password" //密码

    val CLIENTID by lazy {
        Constants.deviceId
    }
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Build.getSerial() else Build.SERIAL //客户端ID，一般以客户端唯一标识符表示，这里用设备序列号表示

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.log(TAG + "onStartCommand  " + flags + "  intent=" + intent)
        init()
        return START_STICKY
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * 响应 （收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等）
     *
     * @param message 消息
     */
    fun response(message: String) {
        val topic = RESPONSE_TOPIC
        val qos = 2
        val retained = false
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient!!.publish(
                topic,
                message.toByteArray(),
                qos,
                retained
            )
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    /**
     * 初始化
     */
    private fun init() {
        val serverURI = HOST //服务器地址（协议+地址+端口号）
        mqttAndroidClient = MqttAndroidClient(this, serverURI, CLIENTID)
            .apply {
                setCallback(mqttCallback) //设置监听订阅消息的回调
                mMqttConnectOptions = MqttConnectOptions()
                    .apply {
                        isCleanSession = true //设置是否清除缓存
//        mMqttConnectOptions!!.connectionTimeout = 10 //设置超时时间，单位：秒   默认30s
//        mMqttConnectOptions!!.keepAliveInterval = 20 //设置心跳包发送间隔，单位：秒  默认60
                        userName = USERNAME //设置用户名
                        password = PASSWORD.toCharArray() //设置密码
                    }
            }
        Logger.log("CLIENTID: $CLIENTID")

        // last will message
        var doConnect = true
        val message = "{\"terminal_uid\":\"$CLIENTID\"}"
        val topic = PUBLISH_TOPIC
        val qos = 2
        val retained = false
        if (message != "" || topic != "") {
            // 最后的遗嘱
            try {
                mMqttConnectOptions!!.setWill(
                    topic,
                    message.toByteArray(),
                    qos,
                    retained
                )
            } catch (e: Exception) {
                Log.i(TAG, "Exception Occured", e)
                doConnect = false
                iMqttActionListener.onFailure(null, e)
            }
        }
        if (doConnect) {
            doClientConnection()
        }
    }

    /**
     * 连接MQTT服务器
     */
    private fun doClientConnection() {
        Log.i(TAG, "doClientConnection")
        mqttAndroidClient?.let {
            if (!it.isConnected && isConnectIsNomarl) {
                try {
                    it.connect(
                        mMqttConnectOptions,
                        null,
                        iMqttActionListener
                    )
                } catch (e: MqttException) {
                    Log.i(TAG, "doClientConnection", e)
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 判断网络是否连接
     */
    private val isConnectIsNomarl: Boolean
        private get() {
            val connectivityManager = this.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivityManager.activeNetworkInfo
            return if (info != null && info.isAvailable) {
                val name = info.typeName
                Log.i(TAG, "当前网络名称：$name")
                true
            } else {
                Log.i(TAG, "没有可用网络")
                /*没有可用网络的时候，延迟3秒再尝试重连*/
                Handler().postDelayed({ doClientConnection() }, 3000)
                false
            }
        }

    //MQTT是否连接成功的监听
    private val iMqttActionListener: IMqttActionListener = object : IMqttActionListener {
        override fun onSuccess(arg0: IMqttToken) {
            Log.i(TAG, "连接成功 subscribe -> $PUBLISH_TOPIC")
            try {
                mqttAndroidClient?.subscribe(
                    PUBLISH_TOPIC,
                    2
                ) //订阅主题，参数：主题、服务质量
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }

        override fun onFailure(arg0: IMqttToken, arg1: Throwable) {
            arg1.printStackTrace()
            Log.i(TAG, "连接失败 ")
            doClientConnection() //连接失败，重连（可关闭服务器进行模拟）
        }
    }

    //订阅主题的回调
    private val mqttCallback: MqttCallback = object : MqttCallback {
        @Throws(Exception::class)
        override fun messageArrived(
            topic: String,
            message: MqttMessage
        ) {
            Log.i(TAG, "收到消息： " + String(message.payload))
            //收到消息，这里弹出Toast表示。如果需要更新UI，可以使用广播或者EventBus进行发送
            LiveEventBus
                .get(LV_RECEIVE_MSG)
                .post(String(message.payload))
            //收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等
//            response("message arrived")
        }

        override fun deliveryComplete(arg0: IMqttDeliveryToken) {}
        override fun connectionLost(arg0: Throwable) {
            Log.i(TAG, "连接断开 ")
            doClientConnection() //连接断开，重连
        }
    }

    override fun onDestroy() {
        Logger.log("$TAG   onDestroy  " + mqttAndroidClient?.isConnected)
        super.onDestroy()
        mqttAndroidClient?.unsubscribe(RESPONSE_TOPIC)
        mqttAndroidClient?.unregisterResources()
        mqttAndroidClient?.let {
            if (it.isConnected) {
                try {
                    it.disconnect() //断开连接
                } catch (e: MqttException) {
                    Logger.log(TAG + "onDestroy  disconnect ")
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        private var mqttAndroidClient: MqttAndroidClient? = null
        val PUBLISH_TOPIC by lazy {
            Constants.storeNo //发布主题
        }
        var RESPONSE_TOPIC = "message_arrived" //响应主题
        const val LV_RECEIVE_MSG = "LV_RECEIVE_MSG" //响应主题

        /**
         * 发布 （模拟其他客户端发布消息）
         *
         * @param message 消息
         */
        fun publish(message: String) {
            if (mqttAndroidClient == null || !mqttAndroidClient!!.isConnected) {
                return
            }

            val topic = PUBLISH_TOPIC
            val qos = 2
            val retained = false
            try {
                //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
                mqttAndroidClient?.publish(
                    topic,
                    message.toByteArray(),
                    qos,
                    retained
                )
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }
}
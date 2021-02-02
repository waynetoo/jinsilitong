package com.waynetoo.videotv.utils

import android.content.Context
import android.net.ConnectivityManager
import com.waynetoo.lib_common.AppContext

/**
 * @Author: wyl
 * @CreateDate: 2021/2/2 17:41
 * @Description:
 * @UpdateRemark:
 */
/**
 * 判断网络是否连接
 */
val isConnectIsNormal: Boolean
    get() {
        val connectivityManager = AppContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return if (info != null && info.isAvailable) {
            val name = info.typeName
            println("当前网络名称：$name")
            true
        } else {
            println("没有可用网络")
            /*没有可用网络的时候，延迟3秒再尝试重连*/
            false
        }
    }
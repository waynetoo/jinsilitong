package com.waynetoo.videotv.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.waynetoo.lib_common.extentions.toast
import com.waynetoo.videotv.config.Constants
import java.io.File


/**
 * @Author: weiyunl
 * @CreateDate: 2021/1/29 16:10
 * @Description:
 * @UpdateRemark:
 */
class USBBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_MEDIA_MOUNTED -> {
//                val oldPath = Constants.usbFileRoot
                intent.data?.path?.let {
                    val usbRoot = File(it)
                    if (usbRoot.exists()) {
                        Constants.usbFileRoot = usbRoot.absolutePath
//                        if (it != oldPath) {
//                            context.toast("插入U盘,旧地址：$oldPath ->  新地址：$it")
//                        } else {
                        context.toast("插入U盘,$it")
//                        }
                    }
                }
            }
            Intent.ACTION_MEDIA_UNMOUNTED, Intent.ACTION_MEDIA_REMOVED -> {
                context.toast("拔出：" + intent.data?.path)
            }
        }
    }
}
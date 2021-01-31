package com.waynetoo.videotv.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.widget.Toast
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
        val action = intent.action
        when (action) {
            Intent.ACTION_MEDIA_MOUNTED -> {
//                context.toast("插入U盘:" + uri?.path)
                intent.data?.path?.let {
                    val usbRoot = File(it)
                    if (usbRoot.exists()) {
                        Constants.usbFileRoot = usbRoot.absolutePath
                    }
                    context.toast("插入U盘:" + usbRoot.absolutePath + "  U盘路径是否正确：" + usbRoot.exists())
                }
            }
            Intent.ACTION_MEDIA_UNMOUNTED, Intent.ACTION_MEDIA_REMOVED -> {
                context.toast("拔出：" + intent.data?.path)
            }
        }
    }
}
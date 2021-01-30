package com.waynetoo.videotv.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.widget.Toast
import com.waynetoo.lib_common.extentions.toast


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
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                val uri: Uri? = intent.data
                context.toast("插入U盘1。" + uri?.path)
//                context.toast("插入U盘2。" + intent.dataString)
                val device_add =
                    intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                context.toast("插入U盘3。" + device_add)
                if (device_add != null) {
//                    EventBusUtil.sendEvent(MessageEvent(1))
                }
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                Toast.makeText(context, "拔出", Toast.LENGTH_LONG).show();
//                EventBusUtil.sendEvent(MessageEvent(0))
            }
        }
    }
}
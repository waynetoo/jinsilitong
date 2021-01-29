package com.waynetoo.videotv.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.widget.Toast


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
                Toast.makeText(context, "插入Upan", Toast.LENGTH_LONG).show();
                val device_add =
                    intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
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
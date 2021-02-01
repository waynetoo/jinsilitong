package com.waynetoo.lib_common.component

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.WindowManager
import com.waynetoo.lib_common.R

/**
 * on 2018/1/7.
 */
class TransparentDialog : androidx.fragment.app.DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!, R.style.TransparentDialog)
            .setView(R.layout.dialog_transparent)
            .create()
    }

    override fun onStart() {
        super.onStart()
        dialog ?: return
        isCancelable = true
        dialog!!.setCanceledOnTouchOutside(false)
        val params = dialog!!.window!!.attributes.apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        dialog!!.window!!.attributes = params
    }

    // 防止 Fragment already added 异常
    override fun show(manager: androidx.fragment.app.FragmentManager, tag: String?) {
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager?.beginTransaction()?.remove(this)?.add(this, tag)?.commitAllowingStateLoss()
//            super.show(manager, tag)
        } catch (e: Exception) {
            //同一实例使用不同的tag会异常,这里捕获一下
            e.printStackTrace()
        }
    }

    fun show(fm: androidx.fragment.app.FragmentManager) = show(fm, "transparent_progress_dialog")
}
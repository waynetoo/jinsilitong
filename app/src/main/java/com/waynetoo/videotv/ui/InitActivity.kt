package com.waynetoo.videotv.ui

import android.content.Intent
import android.os.Bundle
import com.waynetoo.lib_common.extentions.otherwise
import com.waynetoo.lib_common.extentions.toast
import com.waynetoo.lib_common.extentions.yes
import com.waynetoo.lib_common.lifecycle.BaseActivity
import com.waynetoo.videotv.R
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.model.Topic
import com.waynetoo.videotv.presenter.BinderPresenter
import kotlinx.android.synthetic.main.activity_binder.*

/**
 *
 * 绑定门店id页面
 * 步骤：
 * 1，检查app是否绑定门店id，如果没有绑定 -->绑定id
 * 2，绑定完成后，请求广告列表，检查广告是否有更新 ，如果没有更新 -->进入播放广告页面
 * 3，有更新->下载更新 ----->下载完成后 -->进入播放广告页面
 */
class InitActivity : BaseActivity<BinderPresenter>() {
    private val TAG = "waynetoo"
    private var mIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_binder)
        initListeners()
        checkStoreNo()
    }

    private fun initListeners() {
        btn_sure.setOnClickListener {
            presenter.getTopicDetail(et_store_no.text.toString())
        }
    }

    private fun checkStoreNo() {
        Constants.storeNo.isEmpty().yes {
            // 绑定no
        }.otherwise {
            // 请求 广告列表

        }
    }

    fun toastError(msg: String) {
        closeProgressDialog()
        toast(msg)
    }

    fun getDetailSuccess(topic: Topic) {
        topic.run {
        }
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}

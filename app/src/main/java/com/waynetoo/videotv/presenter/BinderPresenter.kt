package com.waynetoo.videotv.presenter

import com.waynetoo.lib_common.extentions.dispatchDefault
import com.waynetoo.lib_common.lifecycle.BasePresenter
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.repository.Service
import com.waynetoo.videotv.repository.commonSubscribe
import com.waynetoo.videotv.ui.InitActivity

/**
 */
class BinderPresenter : BasePresenter<InitActivity>() {
    fun bindStore(storeNo: String) {
        Service.client.bindStore(storeNo)
            .dispatchDefault()
            .commonSubscribe(
                onSuccess = {
                    if (it == null) {
                        return@commonSubscribe
                    }
                    view.bindSuccess(it.storeNo)
                }, onFailure = {
                    view.toastError(it.msg)
                },
                onError = {
                    it.message?.let { msg -> view.toastError(msg) }
                }
            )
    }

    /**
     * 获取广告
     */
    fun getAdList() {
        Service.client.getAdVideos(Constants.storeNo)
            .dispatchDefault()
            .commonSubscribe(
                onSuccess = {
                    if (it == null) {
                        return@commonSubscribe
                    }
                    view.getAdListSuccess(it)
                }, onFailure = {
                    view.toastError(it.msg)
                },
                onError = {
                    it.message?.let { msg -> view.toastError(msg) }
                }
            )
    }
}
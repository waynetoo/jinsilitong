package com.waynetoo.videotv.presenter

import com.waynetoo.lib_common.extentions.dispatchDefault
import com.waynetoo.lib_common.lifecycle.BasePresenter
import com.waynetoo.videotv.repository.Service
import com.waynetoo.videotv.repository.commonSubscribe
import com.waynetoo.videotv.ui.MainActivity

/**
 */
class MainPresenter : BasePresenter<MainActivity>() {
    fun getTopicDetail(topicId: String) {
        Service.client.getTopicDetail(topicId)
            .dispatchDefault()
            .commonSubscribe(
                onSuccess = {
                    if (it == null) {
                        return@commonSubscribe
                    }
                    view.getDetailSuccess(it)
                }, onFailure = {
                    view.toastError(it.msg)
                },
                onError = {
                    it.message?.let { msg -> view.toastError(msg) }
                }
            )
    }
}
package com.waynetoo.lib_common.lifecycle

import android.os.Bundle

/**
 * Created by 陶海啸
 * on 2019/1/17
 */
interface IPresenter<out View : IMvpView<IPresenter<View>>> {
    val view: View
}

interface IMvpView<out Presenter : IPresenter<IMvpView<Presenter>>> {
    val presenter: Presenter
}

interface ILifecycle {
    fun onCreate(savedInstanceState: Bundle?)

    fun onPause()

    fun onStop()

    fun onDestroy()
}
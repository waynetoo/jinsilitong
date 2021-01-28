package com.waynetoo.lib_common.lifecycle

import android.os.Bundle
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


/**
 * Created by 陶海啸
 * on 2019/1/17
 */
abstract class BasePresenter<out V : IMvpView<BasePresenter<V>>> : IPresenter<V>, ILifecycle {
    private val disposes = CompositeDisposable()

    override lateinit var view: @UnsafeVariance V

    override fun onCreate(savedInstanceState: Bundle?) = Unit

    override fun onPause() = Unit

    override fun onStop() = Unit

    override fun onDestroy() {
        disposes.clear()
    }

    protected fun Disposable.addDispose() {
        disposes.add(this)
    }
}
package com.waynetoo.lib_common.lifecycle

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by 陶海啸
 * on 2019/3/8
 */
open class RxViewModel : ViewModel() {
    private val disposes = CompositeDisposable()

    override fun onCleared() {
        disposes.clear()
        super.onCleared()
    }

    protected fun Disposable.addDispose() {
        disposes.add(this)
    }
}
package com.waynetoo.lib_common.extentions

import java.util.*
import kotlin.reflect.KProperty

/**
 * Created by pwy on 2019/3/26.
 */
class ResettableLazyManager {
    // we synchronize to make sure the timing of a reset() call and new inits do not collide
    val managedDelegates = LinkedList<Resettable>()

    fun register(managed: Resettable) {
        synchronized (managedDelegates) {
            managedDelegates.add(managed)
        }
    }

    fun reset() {
        synchronized (managedDelegates) {
            managedDelegates.forEach { it.reset() }
            managedDelegates.clear()
        }
    }
}

interface Resettable {
    fun reset()
}

class ResettableLazy<PROPTYPE>(val manager: ResettableLazyManager, val init: ()->PROPTYPE): Resettable {
    @Volatile var lazyHolder = makeInitBlock()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): PROPTYPE {
        return lazyHolder.value
    }

    override fun reset() {
        lazyHolder = makeInitBlock()
    }

    fun makeInitBlock(): Lazy<PROPTYPE> {
        return lazy {
            manager.register(this)
            init()
        }
    }
}

fun <PROPTYPE> resettableLazy(manager: ResettableLazyManager, init: ()->PROPTYPE): ResettableLazy<PROPTYPE> {
    return ResettableLazy(manager, init)
}

fun resettableManager(): ResettableLazyManager = ResettableLazyManager()

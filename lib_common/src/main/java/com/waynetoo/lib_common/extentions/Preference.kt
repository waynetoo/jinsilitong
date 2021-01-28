package com.waynetoo.lib_common.extentions

import android.content.Context
import java.lang.IllegalArgumentException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * on 2019/3/14
 */
class Preference<T>(
   context: Context, private val name: String, private val default: T, prefName: String = "CMSDefault"
) : ReadWriteProperty<Any?, T> {

    private val prefs by lazy {
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getPreference(name)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(name, value)
    }

    private fun getPreference(key: String): T {
        return when (default) {
            is Int -> prefs.getInt(key, default)
            is Float -> prefs.getFloat(key, default)
            is Long -> prefs.getLong(key, default)
            is String -> prefs.getString(key,default)
            is Boolean -> prefs.getBoolean(key, default)
            else -> {
                throw IllegalArgumentException("格式不支持")
            }
        } as T
    }

    private fun putPreference(key: String, value: T) {
        val editor = prefs.edit()
        with(editor) {
            when (value) {
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                else -> {
                    throw IllegalArgumentException("格式不支持")
                }
            }
        }
        editor.apply()
    }

}
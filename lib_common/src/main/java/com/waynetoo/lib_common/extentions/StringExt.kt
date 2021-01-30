package com.waynetoo.lib_common.extentions

import android.text.ParcelableSpan
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import com.tencent.bugly.crashreport.BuglyLog
import java.util.regex.Pattern

/**
 * Created by pwy on 2019-09-04.
 */
/**
 * 拼接不同颜色的字符串
 */
fun CharSequence.formatStringColor(color: Int, start: Int, end: Int): SpannableString {
    return this.setSpan(ForegroundColorSpan(color), start, end)
}

/**
 * 拼接不同大小的字符串
 */
fun CharSequence.formatStringSize(size: Int, start: Int, end: Int): SpannableString {
    return this.setSpan(AbsoluteSizeSpan(size), start, end)
}

private fun CharSequence.setSpan(span: ParcelableSpan, start: Int, end: Int): SpannableString {
    val spannableString = SpannableString(this)
    spannableString.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    return spannableString
}

private fun String.parseSrcFromHtml(reg: String): List<String> {
    BuglyLog.i("HTML", this)
    val srcList = arrayListOf<String>()
    val pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(this)
    //使用find()方法查找第一个匹配的对象
    //使用循环找出 html里所有的video标签
    while (matcher.find()) {
        val matchGroup = matcher.group(0)
        BuglyLog.i("HTML", matchGroup)
        val m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(matchGroup)
        while (m.find()) {
            val src = m.group(1)
            BuglyLog.i("HTML", src)
            srcList.add(src)
        }
    }
    return srcList
}

fun String.parseImgSrc(): List<String> {
    return parseSrcFromHtml("<(img|IMG)(.*?)(/>|></img>|>)")
}

fun String.parseVideoSrc(): List<String> {
    return parseSrcFromHtml("<(video|VIDEO)(.*?)(/>|></video>|>)")
}

/**
 * 手机号正则验证
 */
fun String.isPhoneNumber(): Boolean {
    val pattern = """[1][3-9][0-9]{9}${'$'}"""
    return Regex(pattern).matches(this)
}

fun String.isVideo(): Boolean {
    val pattern = """.+(.MP4|.mp4|.avi|.AVI)${'$'}"""
    return Regex(pattern).matches(this)
}

fun String.isPicture(): Boolean {
    val pattern = """.+(.JPEG|.jpeg|.JPG|.jpg|.png|.PNG)${'$'}"""
    return Regex(pattern).matches(this)
}
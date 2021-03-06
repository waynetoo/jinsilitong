package com.waynetoo.lib_common.extentions

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.waynetoo.lib_common.AppContext
import java.io.File
import java.io.IOException
import java.io.InputStream


/**
 * on 2019/4/9
 */
fun readAssetsToString(file: String): String {
    val inputStream: InputStream
    try {
        inputStream = AppContext.assets.open(file)
    } catch (e: IOException) {
        e.printStackTrace()
        return ""
    }
    val bytes = inputStream.readBytes()
    return String(bytes, Charsets.UTF_8)
}

fun Context.getRealPathFromUri(uri: Uri): String? {
    val sdkVersion = Build.VERSION.SDK_INT
    return if (sdkVersion >= 19) { // api >= 19
        getRealPathFromUriAboveApi19(this, uri)
    } else { // api < 19
        getRealPathFromUriBelowAPI19(this, uri)
    }
}

/**
 * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
 *
 * @param context 上下文对象
 * @param uri     图片的Uri
 * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
 */
private fun getRealPathFromUriBelowAPI19(context: Context, uri: Uri): String? {
    return getDataColumn(context, uri, null, null)
}

/**
 * 适配api19及以上,根据uri获取图片的绝对路径
 *
 * @param context 上下文对象
 * @param uri     图片的Uri
 * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
 */
@SuppressLint("NewApi")
private fun getRealPathFromUriAboveApi19(context: Context, uri: Uri): String? {
    var filePath: String? = null
    if (DocumentsContract.isDocumentUri(context, uri)) {
        // 如果是document类型的 uri, 则通过document id来进行处理
        val documentId = DocumentsContract.getDocumentId(uri)
        if (isMediaDocument(uri)) { // MediaProvider
            // 使用':'分割
            val id =
                documentId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]

            val selection = MediaStore.Images.Media._ID + "=?"
            val selectionArgs = arrayOf(id)
            filePath = getDataColumn(
                context,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                selection,
                selectionArgs
            )
        } else if (isDownloadsDocument(uri)) { // DownloadsProvider
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                java.lang.Long.valueOf(documentId)
            )
            filePath = getDataColumn(context, contentUri, null, null)
        }
    } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
        // 如果是 content 类型的 Uri
        filePath = getDataColumn(context, uri, null, null)
    } else if ("file" == uri.scheme) {
        // 如果是 file 类型的 Uri,直接获取图片对应的路径
        filePath = uri.path
    }
    return filePath
}

/**
 * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
 * @return
 */
private fun getDataColumn(
    context: Context,
    uri: Uri,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    var path: String? = null

    val projection = arrayOf(MediaStore.Images.Media.DATA)
    var cursor: Cursor? = null
    try {
        cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
            path = cursor.getString(columnIndex)
        }
    } catch (e: Exception) {
        cursor?.close()
    }

    return path
}

/**
 * @param uri the Uri to check
 * @return Whether the Uri authority is MediaProvider
 */
private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

/**
 * @param uri the Uri to check
 * @return Whether the Uri authority is DownloadsProvider
 */
private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}


fun String.getFileNameNoEx(): String {
    if (length > 0) {
        val dot = lastIndexOf('.')
        if (dot > -1 && dot < length) {
            return substring(0, dot)
        }
    }
    return ""
}
package com.waynetoo.videotv.utils

import android.os.Environment
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.model.AdInfo
import kotlinx.io.InputStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object USBUtils {
    private var usbRoot: String = ""
    fun GetUsbPath(): String? {
        var strMountInfo = ""
        // 1.首先获得系统已加载的文件系统信息
        try {
            // 创建系统进程生成器对象
            val objProcessBuilder = ProcessBuilder()
            // 执行 mount -h 可以看到 mount : list mounted filesystems
            // 这条命令可以列出已加载的文件系统
            objProcessBuilder.command("mount") // 新的操作系统程序和它的参数
            // 设置错误输出都将与标准输出合并
            objProcessBuilder.redirectErrorStream(true)
            // 基于当前系统进程生成器的状态开始一个新进程，并返回进程实例
            val objProcess = objProcessBuilder.start()
            // 阻塞线程至到本地操作系统程序执行结束，返回本地操作系统程序的返回值
            objProcess.waitFor()
            // 得到进程对象的输入流，它对于进程对象来说是已与本地操作系统程序的标准输出流(stdout)相连接的
            val objInputStream: InputStream = objProcess.inputStream
            val buffer = ByteArray(1024)
            // 读取 mount 命令程序返回的信息文本
            while (-1 != objInputStream.read(buffer)) {
                strMountInfo = strMountInfo + String(buffer)
            }
            // 关闭进程对象的输入流
            objInputStream.close()
            // 终止进程并释放与其相关的任何流
            objProcess.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // 2.然后再在系统已加载的文件系统信息里查找 SD 卡路径
        // mount 返回的已加载的文件系统信息是以一行一个信息的形式体现的，
        // 所以先用换行符拆分字符串
        val lines = strMountInfo.split("\n".toRegex()).toTypedArray()
        // 清空该字符串对象，下面将用它来装载真正有用的 SD 卡路径列表
        strMountInfo = ""
        for (i in lines.indices) {
            // 如果该行内有 /mnt/和 vfat 字符串，说明可能是内/外置 SD 卡的挂载路径
            if (-1 != lines[i].indexOf(" /mnt/") &&  // 前面要有空格，以防断章取义
                -1 != lines[i].indexOf(" vfat ")
            ) // 前后均有空格
            {
                // 再以空格分隔符拆分字符串
                val blocks =
                    lines[i].split("\\s".toRegex()).toTypedArray() // \\s 为空格字符
                for (j in blocks.indices) {
                    // 如果字符串中含有/mnt/字符串，说明可能是我们要找的 SD 卡挂载路径
                    if (-1 != blocks[j].indexOf("/mnt/")) {
                        // 排除重复的路径
                        if (-1 == strMountInfo.indexOf(blocks[j])) {
                            // 用分号符(;)分隔 SD 卡路径列表，
                            strMountInfo += blocks[j] //此处位一个插入一个U盘时的路径，如果U盘过多可能拼到一起。
                        }
                    }
                }
            }
        }
        return strMountInfo
    }

    /**
     *
     */
    fun checkSDCard(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun getExtSDCardPath(): List<String>? {
        val lResult: MutableList<String> = ArrayList()
        try {
            val rt = Runtime.getRuntime()
            val proc = rt.exec("mount")
            val `is` = proc.inputStream
            val isr = InputStreamReader(`is`)
            val br = BufferedReader(isr)
            var line = ""
            while (br.readLine().also({ line = it }) != null) {
                if (line.contains("extSdCard")) {
                    val arr = line.split(" ".toRegex()).toTypedArray()
                    val path = arr[1]
                    val file = File(path)
                    if (file.isDirectory()) {
                        lResult.add(path)
                    }
                }
            }
            isr.close()
        } catch (e: java.lang.Exception) {
        }
        return lResult
    }

//    fun initDownloadRoot(context: Context) {
//        if (checkSDCard()) {
////            Constants.filesMovies =
////                context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
//            context.toast("sdcard 可用：  "+Constants.filesMovies)
//        } else {
////            Constants.filesMovies = context.filesDir
//            context.toast("sdcard 不可用")
//        }
//    }

    fun isUsbEnable(): Boolean {
        return Constants.usbFileRoot.isNotEmpty() && File(Constants.usbFileRoot).exists()
    }

    fun createUsbDir(): File {
        println("Constants.usbFileRoot =" + Constants.usbFileRoot)
        val storeFile = File(Constants.usbFileRoot, Constants.USB_FILE_DIR)
        if (!storeFile.exists()) {
            storeFile.mkdir()
        }
        return storeFile
    }

    fun createSdcardDir(): File {
        println("Constants.sdcardRoot =" + Constants.sdcardRoot)
        val storeFile = File(Constants.sdcardRoot, Constants.USB_FILE_DIR)
        if (!storeFile.exists()) {
            storeFile.mkdir()
        }
        return storeFile
    }

    /**
     * 建立文件名称
     */
    fun createFilePath(adInfo: AdInfo): String {
        return if (adInfo.isUsbPath) {
            Constants.usbFileRoot + File.separator + Constants.USB_FILE_DIR + File.separator + adInfo.fileName
        } else {
            Constants.sdcardRoot + File.separator + Constants.USB_FILE_DIR + File.separator + adInfo.fileName
        }
    }
}
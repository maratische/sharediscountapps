package maratische.android.sharediscountapps

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.Writer

class APP: Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("location", "location", NotificationManager.IMPORTANCE_DEFAULT)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        // Устанавливаем обработчик для всех неперехваченных исключений
//        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
//            handleUncaughtException(thread, throwable)
//
//             После обработки исключения, передаем управление стандартному обработчику
//            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
//        }
    }
    private fun handleUncaughtException(thread: Thread, throwable: Throwable) {
        val myDir = applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(myDir, "errors.txt")
        if (!file.exists()) file.createNewFile()
        try {
            var time = System.currentTimeMillis()
            val out = BufferedWriter(FileWriter(file,true))
            out.write("$time;${throwable.message}")
            out.flush()
            out.close()

            val file = File(myDir, "$time.txt")
            if (!file.exists()) file.createNewFile()
            var stream = PrintStream(FileOutputStream(file))
            throwable.printStackTrace(stream)
            stream.flush()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
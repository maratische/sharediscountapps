package maratische.android.sharediscountapps

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.PrintStream
import java.util.Date

class ErrorService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra("message")
        if (message != null) {
            val myDir = applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(myDir, "errors.txt")
            if (!file.exists()) file.createNewFile()
            try {
                var time = System.currentTimeMillis()
                val out = BufferedWriter(FileWriter(file,true))
                out.write("$time;${message}")
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            sendBroadcast(Intent(MainActivity4.UPDATE_UI))
        }
        return super.onStartCommand(intent, flags, startId)
    }

}
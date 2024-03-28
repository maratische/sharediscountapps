package maratische.android.sharediscountapps

import android.R.attr.label
import android.R.attr.text
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import maratische.android.sharediscountapps.SettingsUtil.Companion.saveOffset
import maratische.android.sharediscountapps.SettingsUtil.Companion.saveTelegramKey
import maratische.android.sharediscountapps.adapter.AppItemAdapter
import maratische.android.sharediscountapps.adapter.AppUserAdapter
import maratische.android.sharediscountapps.adapter.SimpleItemAdapter
import maratische.android.sharediscountapps.model.AppItem
import maratische.android.sharediscountapps.model.ErrorItem
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class MainActivity4 : AppCompatActivity() {
    private var startStop = false//start true, stop false
    private lateinit var recyclerView: RecyclerView
    private lateinit var listApps: RecyclerView
    private lateinit var listUsers: RecyclerView

    companion object {
        const val UPDATE_UI = "UPDATE_UI"
        const val UPDATE_KEY = "UPDATE_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ),
            111
        )
        setContentView(R.layout.activity_main3)

        //отображение списка приложений
        listApps = findViewById(R.id.listApps)
        listApps.layoutManager = LinearLayoutManager(this)
        listApps.adapter = AppItemAdapter(ArrayList())

        //отображение ошибок
        listUsers = findViewById(R.id.listUsers)
        listUsers.layoutManager = LinearLayoutManager(this)
        listUsers.adapter = AppUserAdapter(ArrayList())

        //отображение ошибок
        recyclerView = findViewById(R.id.errorRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SimpleItemAdapter(ArrayList())

        var buttonStart = findViewById<Button>(R.id.buttonStart)
        buttonStart.setOnClickListener {
            userActivity()
            buttonStart.text = if (startStop) {
                "Ping"
            } else {
                "Pong"
            }
            startStop = !startStop
            updateUi()
        }

        var telegramToken = findViewById<EditText>(R.id.telegramToken)
        telegramToken.setText(SettingsUtil.loadTelegramKey(applicationContext))
        var telegramOffset = findViewById<EditText>(R.id.telegramOffset)
        telegramOffset.setText(SettingsUtil.loadOffset(applicationContext).toString())
        findViewById<Button>(R.id.buttonSettings).setOnClickListener {
            userActivity()
            try {
                saveOffset(telegramOffset.text.toString().toLong(), applicationContext)
            } catch (e: NumberFormatException) {
                error("Error on parse offset ${e.message}")
            }
            saveTelegramKey(telegramToken.text.toString(), applicationContext)
            telegramOffset.text
        }
        findViewById<Button>(R.id.buttonExportSettings).setOnClickListener {
            val exportSettings = SettingsUtil.export(applicationContext)
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("settings", exportSettings)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(applicationContext,"Exported",Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.buttonImportSettings).setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.let {
                val importValue = it
                try {
                    SettingsUtil.import(importValue, applicationContext)
                    Toast.makeText(applicationContext, "Import $importValue", Toast.LENGTH_LONG)
                        .show()
                    updateUi()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "Imported $importValue", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

        Intent(applicationContext, MyAccessibilityService::class.java).apply {
            startService(this)
        }
        TelegramService.scheduleJob(this);

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        userActivity()
        return super.onTouchEvent(event)
    }

    private fun userActivity() {
        setCurrentBrightness()
        setZeroBrightness()
    }

    private fun updateUi() {
        val appsStart = SettingsUtil.listOfCards().map {
            AppItem(it.first, SettingsUtil.loadSettings(it.second, applicationContext), it.second)
        }.toList()
        (listApps.adapter as AppItemAdapter).setItems(appsStart)

        Thread {
            val myDir = applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(myDir, "errors.txt")
            if (!file.exists()) file.createNewFile()
            try {
                var time = System.currentTimeMillis()
                val out = BufferedReader(FileReader(file))
                val errors = out.readLines().map { ErrorItem(it) }
                    .filter { it.date > System.currentTimeMillis() - 1000 * 60 * 60 * 24 }
//                    .sortedBy { it.date }
                    .groupBy { it.message }
                    .map { (key, value) -> value.maxByOrNull { it.date }!! }
                    .sortedByDescending { it.date }
                    .toList()
                (recyclerView.adapter as SimpleItemAdapter).setItems(errors)
                out.close()
            } catch (e: Exception) {
                ;
            }
            //run code on background thread
//            MainActivity4.this.runOnUiThread {}
        }.start()

        val settingsUsers = SettingsUtil.loadAppTelegramUsers(applicationContext)
        (listUsers.adapter as AppUserAdapter).setItems(settingsUsers.users)
    }

    // Определение форматтера даты и времени
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private fun formatTimeFromLong(timeInMillis: Long): String =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault())
            .format(formatter)

    var currentBrightness: Int = 0;
    var handler = Handler(Looper.getMainLooper())

    @Volatile
    var timeOfActivity: Long = 0;

    override fun onStart() {
        super.onStart()
        registerBroadCastReceiver()
        updateUi()
        // Получение текущей яркости
        val britghtness = Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        )
        if (britghtness > 0) {
            currentBrightness = britghtness
        }
        setZeroBrightness()
    }

    var screenSaverTask: ScreenSaverTask? = null
    fun setZeroBrightness() {
        timeOfActivity = System.currentTimeMillis() + 3000 - 100;
        screenSaverTask?.let { it.cancel() }
        screenSaverTask = ScreenSaverTask(timeOfActivity, contentResolver)
        var h = handler.postDelayed(screenSaverTask!!, 3000)

    }

    class ScreenSaverTask(var timeOfActivity: Long, var contentResolver: ContentResolver) : Runnable {
        var isCancel = false

        fun cancel() {
            isCancel = true
        }
        override fun run() {
            if (!isCancel && timeOfActivity < System.currentTimeMillis()) {
                try {
                    Settings.System.putInt(
                        contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        0
                    );
                } catch (e : Exception) {
                    ;
                }
            }
        }

    }

    fun setCurrentBrightness() {
        try {
            val britghtness = Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            if (britghtness <  currentBrightness) {
                Settings.System.putInt(
                    getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    currentBrightness
                );
            }
        } catch (e : Exception) {
            ;
        }
        screenSaverTask?.let { it.cancel() }
    }

    override fun onStop() {
        super.onStop()
        unRegisterBroadCastReceiver()
        setCurrentBrightness();
    }

    private fun registerBroadCastReceiver() {
        val filter = IntentFilter()
        filter.addAction(UPDATE_UI)
        registerReceiver(broadcastReceiver, filter, null, null)
    }

    private fun unRegisterBroadCastReceiver() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
    }

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateUi()
        }
    }
}
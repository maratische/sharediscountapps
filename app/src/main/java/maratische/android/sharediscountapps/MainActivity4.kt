package maratische.android.sharediscountapps

import android.content.BroadcastReceiver
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import maratische.android.sharediscountapps.MyAccessibilityService.MyTakeScreenshotCallback
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
        var buttonSettings = findViewById<Button>(R.id.buttonSettings)
        buttonSettings.setOnClickListener {
            userActivity()
            try {
                saveOffset(telegramOffset.text.toString().toLong(), applicationContext)
            } catch (e: NumberFormatException) {
                error("Error on parse offset ${e.message}")
            }
            saveTelegramKey(telegramToken.text.toString(), applicationContext)
            telegramOffset.text
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
        val appsStart = listOf(
            AppItem("activity", SettingsUtil.loadSettings("start", applicationContext), "start"),
            AppItem("Weather", SettingsUtil.loadSettings("weather", applicationContext), "weather"),
            AppItem("Auchan", SettingsUtil.loadSettings("auchan", applicationContext), "auchan"),
            AppItem("Spar", SettingsUtil.loadSettings("spar", applicationContext), "spar"),
            AppItem("Verniy", SettingsUtil.loadSettings("verniy", applicationContext), "verniy"),
            AppItem("Magnit", SettingsUtil.loadSettings("magnit", applicationContext), "magnit"),
            AppItem(
                "Pyaterka",
                SettingsUtil.loadSettings("pyaterka", applicationContext),
                "pyaterka"
            ),
        )
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

    fun setZeroBrightness() {
        timeOfActivity = System.currentTimeMillis() + 3000 - 100;
        var h = handler.postDelayed(Runnable {
            if (timeOfActivity < System.currentTimeMillis()) {
                try {
                Settings.System.putInt(
                    getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    0
                );
                } catch (e : Exception) {
                    ;
                }
            }
        }, 3000)
    }

    fun setCurrentBrightness() {
        try {
            Settings.System.putInt(
                getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                currentBrightness
            );
        } catch (e : Exception) {
            ;
        }
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
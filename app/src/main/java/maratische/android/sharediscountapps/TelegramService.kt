package maratische.android.sharediscountapps

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import maratische.android.sharediscountapps.SettingsUtil.Companion.loadOffset
import maratische.android.sharediscountapps.SettingsUtil.Companion.saveOffset
import maratische.android.sharediscountapps.TimeUtil.Companion.formatTimeFromLong
import maratische.android.sharediscountapps.model.AppTelegramUser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class TelegramService : JobService() {

    private val TAG = "TelegramService"
    private var client = OkHttpClient()
    var gson = Gson()
    private var baseUrl = "https://api.telegram.org/bot"

    companion object {
        val JSON: MediaType = "application/json".toMediaType()
        private val JOB_ID = 1001
        fun scheduleJob(context: Context) {
            val jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            val componentName = ComponentName(context, TelegramService::class.java)
            val jobInfo = JobInfo.Builder(JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(1000)
                .setOverrideDeadline(5000)
//                .setPeriodic(1000) // Интервал выполнения задачи в миллисекундах (5 секунд)
                .setPersisted(true) // Задача будет сохранена даже после перезагрузки устройства
                .build()
            jobScheduler.schedule(jobInfo)
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        // Планирование следующей задачи через 5 секунд
        scheduleJob(this)

        getAllTelegramUpdates()

        // Сообщаем системе, что задача завершена
        jobFinished(params, false)
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra("key")
        if (message != null) {
            val requests = SettingsUtil.loadRequests(this)
            requests[message]?.let {
                val chatId = it
                sendPhoto(chatId, message)
                requests.remove(message)
            }
            SettingsUtil.saveRequests(requests,this)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        // Вернуть true, чтобы перепланировать задачу, если она была прервана
        return true
    }

    fun getAllTelegramUpdates() {
        try {
            val offset =
                1L + loadOffset(applicationContext)
            val request: Request = Request.Builder()
                .url("$baseUrl${SettingsUtil.loadTelegramKey(applicationContext)}/getUpdates?offset=$offset")
                .post(EMPTY_REQUEST)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "onFailure: ${e.message}")
                    sendError("onFailure: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    var responseBody = response.body?.string()
                    val entity: GetUpdates =
                        gson.fromJson(responseBody, GetUpdates::class.java)
                    if (entity?.ok == true && entity.result?.isNotEmpty() ?: false) {
                        entity.result.sortedBy { it.update_id }.forEach {
                            processGetUpdatesItem(it)
                        }
                    }
                }

            })
        } catch (e: Exception) {
            e.message
            sendError("onFailure: ${e.message}")
        }
    }

    fun processGetUpdatesItem(item: GetUpdatesItem) {
        val username = item.message?.from?.username
        if (username?.isNotEmpty() == true) {
            //process user
            var users = SettingsUtil.loadAppTelegramUsers(applicationContext)
            var user =
                users.users.filter { it.username == username }.firstOrNull() ?: AppTelegramUser(
                    username,
                    item.message?.from?.id ?: 0,
                    System.currentTimeMillis(),
                    false
                )
            user.timeLast = System.currentTimeMillis()
            users.users.add(user)
            SettingsUtil.saveAppTelegramUsers(users, applicationContext)
            sendBroadcast(Intent(MainActivity4.UPDATE_UI))
            if (!user.approved) {
                return
            }
            //process message
            var text = item.message?.text?.lowercase()
            if (text == "погода" || text == "/weather" || text == "/pogoda") {
                sendPhoto(item.message?.chat?.id!!, "weather")
            } else if (text == "/auchan" || text == "auchan" || text == "ашан") {
                sendPhoto(item.message?.chat?.id!!, "auchan")
            } else if (text == "/spar" || text == "spar" || text == "спар") {
                sendPhoto(item.message?.chat?.id!!, "spar")
            } else if (text == "/magnit" || text == "magnit" || text == "магнит") {
                sendPhoto(item.message?.chat?.id!!, "magnit")
            } else if (text == "/verniy" || text == "verniy" || text == "верныйs") {
                sendPhoto(item.message?.chat?.id!!, "verniy")
            } else if (text == "/5ka" || text == "пятерочка" || text == "пятерка") {
                sendPhoto(item.message?.chat?.id!!, "pyaterka")
            } else if (text == "/5ka_new") {
                requestPhoto(item.message?.chat?.id!!, "pyaterka")
            } else {
                sendMessage(item.message?.chat?.id!!, "hi! ${item.message?.text}")
            }
            saveOffset(item.update_id, applicationContext)
        }
    }

    private fun requestPhoto(chatId: Long, key: String) {
        val requests = SettingsUtil.loadRequests(this)
        requests[key] = chatId
        SettingsUtil.saveRequests(requests , this)

        var settingsStart = SettingsUtil.loadSettings("start", this)
        settingsStart.timeLast = 0
        SettingsUtil.saveSettings("start", settingsStart, this)
        var settings = SettingsUtil.loadSettings(key, this)
        settings.timeLast = 0
        SettingsUtil.saveSettings(key, settings, this)
        sendBroadcast(Intent(MainActivity4.UPDATE_UI))
    }

    private fun sendMessage(chatId: Long, text: String) {
        try {
            val multipartBody: MultipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM) // Header to show we are sending a Multipart Form Data
                .addFormDataPart(
                    "chat_id",
                    "$chatId"
                ) // other string params can be like userId, name or something
                .addFormDataPart(
                    "text",
                    "$text"
                ) // other string params can be like userId, name or something
                .build()
            val request: Request = Request.Builder()
                .url("$baseUrl${SettingsUtil.loadTelegramKey(applicationContext)}/sendMessage")
                .post(multipartBody)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "onFailure: ${e.message}")
                    sendError("onFailure: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                }
            })
        } catch (e: Exception) {
            sendError("onFailure: ${e.message}")
            e.message
        }
    }

    private fun sendPhoto(chatId: Long, key: String) {
        var app = SettingsUtil.loadSettings(key, applicationContext)
        var filename = "$key.jpg"
        try {
            val myDir = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            var file = File(myDir, filename)
            val fis = FileInputStream(file)
            var fileSize = fis.available()
            val fileBytes = ByteArray(fileSize)
            fis.read(fileBytes)
            fis.close()
            val fileBody: RequestBody = fileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

            val multipartBody: MultipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM) // Header to show we are sending a Multipart Form Data
                .addFormDataPart("photo", filename, fileBody) // file param
                .addFormDataPart(
                    "chat_id",
                    "$chatId"
                ) // other string params can be like userId, name or something
                .addFormDataPart("caption", "$key ${formatTimeFromLong(app.timeLastSucessfull)}")
                .build()
            val request: Request = Request.Builder()
                .url("$baseUrl${SettingsUtil.loadTelegramKey(applicationContext)}/sendPhoto")
                .post(multipartBody)
                .build()
            val response = client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "onFailure: ${e.message}")
                    sendError("onFailure: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.e(TAG, "onResponse: ${response}")
                }
            })
        } catch (e: Exception) {
            sendError("onFailure: ${e.message}")
            e.message
        }
    }

    private fun sendError(message: String) {
        val intent = Intent(
            applicationContext,
            ErrorService::class.java
        )
        intent.putExtra("message", message)
        startService(intent)
    }

}

data class GetUpdates(
    var ok: Boolean,
    var result: List<GetUpdatesItem>
)

data class GetUpdatesItem(
    var update_id: Long,
    var message: Message?
)

data class Message(
    var message_id: Long,
    var from: MessageUser?,
    var chat: MessageChat?,
    var date: Long,
    var text: String?
)

data class MessageUser(
    var id: Long,
    var is_bot: Boolean?,
    var first_name: String?,
    var username: String?,
    var language_code: String?
)

data class MessageChat(
    var id: Long,
    var first_name: String?,
    var username: String?,
    var language_code: String?
)
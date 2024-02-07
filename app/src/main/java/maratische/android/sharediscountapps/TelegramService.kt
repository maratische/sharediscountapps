package maratische.android.sharediscountapps

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
    private val baseUrl = "https://api.telegram.org/bot"
    private val replyMarkup = "{\"keyboard\":[[" +
            "{\"text\":\"/cards\",\"callback_data\":\"/cards\",\"hide\":false}," +
            "{\"text\":\"/cards_new\",\"hide\":false}" +
            "],[" +
            "{\"text\":\"/podpiska\",\"callback_data\":\"/5ka\",\"podpiska\":false}," +
            "{\"text\":\"/help\",\"hide\":false}" +
            "]]}"
    private val replyMarkupCards = "{\"keyboard\":[[" +
            "{\"text\":\"/5ka\",\"callback_data\":\"/5ka\",\"hide\":false}," +
            "{\"text\":\"/spar\",\"hide\":false}," +
            "{\"text\":\"/magnit\",\"hide\":false}" +
            "],[" +
            "{\"text\":\"/auchan\",\"hide\":false}," +
            "{\"text\":\"/verniy\",\"hide\":false}," +
            "{\"text\":\"/pogoda\",\"callback_data\":\"/pogoda\",\"hide\":false}" +
            "]]}"
    private val replyMarkupCardsNew = "{\"keyboard\":[[" +
            "{\"text\":\"/5ka_new\",\"callback_data\":\"/5ka_new\",\"hide\":false}," +
            "{\"text\":\"/spar_new\",\"hide\":false}," +
            "{\"text\":\"/magnit_new\",\"hide\":false}" +
            "],[" +
            "{\"text\":\"/auchan_new\",\"hide\":false}," +
            "{\"text\":\"/verniy_new\",\"hide\":false}" +
            "]]}"
    private val replyMarkupPodpiska = "{\"keyboard\":[[" +
            "{\"text\":\"/5ka pyaterkasubscribe\",\"callback_data\":\"/5ka subscribe\",\"hide\":false}," +
            "{\"text\":\"/spar sparsubscribe\",\"hide\":false}," +
            "{\"text\":\"/magnit magnitsubscribe\",\"hide\":false}" +
            "],[" +
            "{\"text\":\"/auchan auchansubscribe\",\"hide\":false}," +
            "{\"text\":\"/verniy verniysubscribe\",\"hide\":false}" +
            "]]}"
    private val help =
        "нажатие на кнопку с названием магазина - возвращает текущий код для магазина\n" +
                "если послать комманду '/5ka subscribe' - подписка на код, при генерации нового кода он будет сразу присылаться в телеграм в беззвучном режиме\n" +
                "'/5ka unsubscribe' - отписка\n" +
                "'/5ka_new' - запускается генерация нового кода и как только будет получен - он высылается"

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
            val subscribers = SettingsUtil.loadSubscribers(this)
            subscribers[message]?.let {
                val listOfSubscribers = it
                for (chatId in listOfSubscribers) {
                    sendPhoto(chatId, message, true)
                    if (chatId == requests[message]) {
                        requests.remove(message)
                    }
                }
            }
            requests[message]?.let {
                val chatId = it
                sendPhoto(chatId, message, false)
                requests.remove(message)
                SettingsUtil.saveRequests(requests, this)
            }
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
            Handler(Looper.getMainLooper()).post(Runnable {
                Toast.makeText(
                    this@TelegramService.applicationContext,
                    text,
                    Toast.LENGTH_LONG
                ).show()
            })
            var commandSecond: String? = null
            if (text?.contains(" ") == true) {
                commandSecond = text?.substring(text.indexOf(" ") + 1)
                text = text?.substring(0, text.indexOf(" "))
            }
            var commandFirst: String? = null
            if (text == "/help") {
                sendMessage(item.message?.chat?.id!!, help)
            } else if (text == "погода" || text == "/weather" || text == "/pogoda") {
                sendPhoto(item.message?.chat?.id!!, "weather", false)
                commandFirst = "weather"
            } else if (text == "/auchan" || text == "auchan" || text == "ашан") {
                sendPhoto(item.message?.chat?.id!!, "auchan", false)
                commandFirst = "auchan"
            } else if (text == "/spar" || text == "spar" || text == "спар") {
                sendPhoto(item.message?.chat?.id!!, "spar", false)
                commandFirst = "spar"
            } else if (text == "/magnit" || text == "magnit" || text == "магнит") {
                sendPhoto(item.message?.chat?.id!!, "magnit", false)
                commandFirst = "magnit"
            } else if (text == "/verniy" || text == "verniy" || text == "верный") {
                sendPhoto(item.message?.chat?.id!!, "verniy", false)
                commandFirst = "verniy"
            } else if (text == "/5ka" || text == "пятерочка" || text == "пятерка") {
                sendPhoto(item.message?.chat?.id!!, "pyaterka", false)
                commandFirst = "pyaterka"

            } else if (text == "/auchan_new") {
                requestPhoto(item.message?.chat?.id!!, "auchan")
                commandFirst = "auchan"
            } else if (text == "/spar_new") {
                requestPhoto(item.message?.chat?.id!!, "spar")
                commandFirst = "spar"
            } else if (text == "/magnit_new") {
                requestPhoto(item.message?.chat?.id!!, "magnit")
                commandFirst = "magnit"
            } else if (text == "/verniy_new") {
                requestPhoto(item.message?.chat?.id!!, "verniy")
                commandFirst = "verniy"
            } else if (text == "/5ka_new") {
                requestPhoto(item.message?.chat?.id!!, "pyaterka")
                commandFirst = "pyaterka"
            } else if (text == "/cards") {
                sendMessage(item.message?.chat?.id!!, "cards", replyMarkupCards)
            } else if (text == "/cards_new") {
                sendMessage(item.message?.chat?.id!!, "cards", replyMarkupCardsNew)
            } else if (text == "/podpiska") {
                val subscribers = SettingsUtil.loadSubscribers(this)
                var reply = replyMarkupPodpiska
                reply = subscribeUnsubscribe(subscribers, "auchan", item, reply)
                reply = subscribeUnsubscribe(subscribers, "spar", item, reply)
                reply = subscribeUnsubscribe(subscribers, "magnit", item, reply)
                reply = subscribeUnsubscribe(subscribers, "verniy", item, reply)
                reply = subscribeUnsubscribe(subscribers, "pyaterka", item, reply)
                sendMessage(item.message?.chat?.id!!, "podpiska", reply)
            } else {
                sendMessage(item.message?.chat?.id!!, "hi! ${item.message?.text}")
            }

            if (commandFirst != null && commandSecond == "subscribe" || commandSecond == "подписка") {
                subscribe(item.message?.chat?.id!!, commandFirst!!)
            }
            if (commandFirst != null && commandSecond == "unsubscribe" || commandSecond == "отподписка") {
                unsubscribe(item.message?.chat?.id!!, commandFirst!!)
            }
            saveOffset(item.update_id, applicationContext)
        }
    }

    private fun subscribeUnsubscribe(
        subscribers: HashMap<String, HashSet<Long>>,
        name: String,
        item: GetUpdatesItem,
        reply: String
    ): String {
        var reply1 = reply
        if (subscribers[name]?.contains(item.message?.chat?.id!!) == true) {
            reply1 = reply1.replace("${name}subscribe", "unsubscribe");
        } else {
            reply1 = reply1.replace("${name}subscribe", "subscribe");
        }
        return reply1
    }

    private fun subscribe(chatId: Long, key: String) {
        val requests = SettingsUtil.loadSubscribers(this)
        if (requests[key] == null) {
            requests[key] = HashSet()
        }
        requests[key]?.add(chatId)
        SettingsUtil.saveSubscribers(requests, this)
    }

    private fun unsubscribe(chatId: Long, key: String) {
        val requests = SettingsUtil.loadSubscribers(this)
        if (requests[key] == null) {
            requests[key] = HashSet()
        }
        var element = requests[key]!!
        element.remove(chatId)
        requests[key] = element
        SettingsUtil.saveSubscribers(requests, this)
    }

    private fun requestPhoto(chatId: Long, key: String) {
        val requests = SettingsUtil.loadRequests(this)
        requests[key] = chatId
        SettingsUtil.saveRequests(requests, this)

        var settingsStart = SettingsUtil.loadSettings("start", this)
        settingsStart.timeLast = 0
        SettingsUtil.saveSettings("start", settingsStart, this)
        var settings = SettingsUtil.loadSettings(key, this)
        settings.timeLast = 0
        SettingsUtil.saveSettings(key, settings, this)
        sendBroadcast(Intent(MainActivity4.UPDATE_UI))
    }

    private fun sendMessage(chatId: Long, text: String, markup: String = replyMarkup) {
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
                .addFormDataPart("reply_markup", markup)
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
                    Log.e(TAG, "onResponse")
                }
            })
        } catch (e: Exception) {
            sendError("onFailure: ${e.message}")
            e.message
        }
    }

    private fun sendPhoto(chatId: Long, key: String, disableNotification: Boolean) {
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
                .addFormDataPart("disable_notification", disableNotification.toString())
                .addFormDataPart(
                    "chat_id",
                    "$chatId"
                ) // other string params can be like userId, name or something
                .addFormDataPart("caption", "$key ${formatTimeFromLong(app.timeLastSucessfull)}")
//                .addFormDataPart("reply_markup", "{\"inline_keyboard\":[[{\"text\":\"test button\",\"callback_data\":\"test\",\"hide\":false}]]}")
                .addFormDataPart("reply_markup", replyMarkup)
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
package maratische.android.sharediscountapps

import android.app.job.JobService
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import maratische.android.sharediscountapps.model.AppSettings
import maratische.android.sharediscountapps.model.AppTelegramUser
import maratische.android.sharediscountapps.model.AppTelegramUsers

class SettingsUtil {
    companion object {
        var gson = Gson()

        fun loadSettings(key: String, context: Context): AppSettings {
            try {
                val json = context.getSharedPreferences("TAG", AppCompatActivity.MODE_PRIVATE)
                    .getString(key, "{}")
                val entity: AppSettings =
                    gson.fromJson(json, AppSettings::class.java)
                return entity
            } catch (e: Exception) {
                return AppSettings(0,0, false)
            }
        }

        fun saveSettings(key: String, appSettings: AppSettings, context: Context) {
                val json = gson.toJson(appSettings)
                context.getSharedPreferences("TAG", AppCompatActivity.MODE_PRIVATE).edit().putString(key, json).commit()
        }

        fun loadAppTelegramUsers(context: Context): AppTelegramUsers {
            try {
                val json = context.getSharedPreferences("TAG", AppCompatActivity.MODE_PRIVATE)
                    .getString("AppTelegramUser", "{}")
                val entity: AppTelegramUsers =
                    gson.fromJson(json, AppTelegramUsers::class.java)
                return entity
            } catch (e: Exception) {
                return AppTelegramUsers(HashSet())
            }
        }
        fun saveAppTelegramUsers(userSettings: AppTelegramUsers, context: Context) {
            val json = gson.toJson(userSettings)
            context.getSharedPreferences("TAG", AppCompatActivity.MODE_PRIVATE).edit().putString("AppTelegramUser", json).commit()
        }

        fun loadOffset(context: Context) = context.getSharedPreferences("TAG", JobService.MODE_PRIVATE).getLong("telegram_offset", 0)

        fun saveOffset(offset: Long, context: Context) {
            context.getSharedPreferences("TAG", JobService.MODE_PRIVATE).edit()
                .putLong("telegram_offset", offset).commit()
        }

        fun loadTelegramKey(context: Context) = context.getSharedPreferences("TAG", JobService.MODE_PRIVATE).getString("telegram_key", "")

        fun saveTelegramKey(key: String, context: Context) {
            context.getSharedPreferences("TAG", JobService.MODE_PRIVATE).edit()
                .putString("telegram_key", key).commit()
        }

    }
}
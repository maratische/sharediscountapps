package maratische.android.sharediscountapps.model

data class AppTelegramUsers(var users: HashSet<AppTelegramUser> = HashSet())
data class AppTelegramUser(var username: String, var telegramId: Long, var timeLast: Long, var approved: Boolean, var admin: Boolean, var chatId: Long = 0) {
    override fun hashCode(): Int {
        return username.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this.username?.equals((other as AppTelegramUser).username) == true
    }
}

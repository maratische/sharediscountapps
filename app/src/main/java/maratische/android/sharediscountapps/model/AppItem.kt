package maratische.android.sharediscountapps.model

data class AppItem(val name: String, val timeStart: Long, val timeSuccess: Long, var active: Boolean, val key: String) {
    constructor(name: String, settings: AppSettings, key: String) : this(name, settings.timeLast, settings.timeLastSucessfull, settings.active, key )
}

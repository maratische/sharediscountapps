package maratische.android.sharediscountapps.model

import maratische.android.sharediscountapps.TimeUtil.Companion.formatTimeFromLong

data class ErrorItem(var date: Long, var message: String) {
    constructor(text: String) : this(0,"") {
        if (text != null) {
            date = text.substring(0, text.indexOf(";")).toLong()
            message = text.substring(text.indexOf(";") + 1)
        }
    }
}
package maratische.android.sharediscountapps.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import maratische.android.sharediscountapps.MainActivity4
import maratische.android.sharediscountapps.R
import maratische.android.sharediscountapps.SettingsUtil
import maratische.android.sharediscountapps.TimeUtil
import maratische.android.sharediscountapps.model.AppTelegramUser

class AppUserAdapter(private val items: ArrayList<AppTelegramUser>) :
    RecyclerView.Adapter<AppUserAdapter.AppUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_adapter, parent, false)
        return AppUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppUserViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: HashSet<AppTelegramUser>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    class AppUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.text1)
        private val timeView: TextView = itemView.findViewById(R.id.time)
        private val checkboxActive: CheckBox = itemView.findViewById(R.id.checkbox_active)
        private var checkedChangeListener: OnCheckedChangeListener? = null


        fun bind(item: AppTelegramUser) {
            textView.text = item.username
            timeView.text = TimeUtil.formatTimeFromLong(item.timeLast)
            checkedChangeListener?.setProgrammaticChange(true)
            checkboxActive.isChecked = item.approved
            checkedChangeListener?.setProgrammaticChange(false)
            checkedChangeListener = OnCheckedChangeListener(item)
            checkboxActive.setOnCheckedChangeListener(checkedChangeListener)
//            checkboxActive.setOnCheckedChangeListener { _, isChecked ->
//                var settings = SettingsUtil.loadSettings(item.key, itemView.context)
//                settings.active = isChecked
//                SettingsUtil.saveSettings(item.key, settings, itemView.context)
//            }
        }
    }

    class OnCheckedChangeListener(private var item: AppTelegramUser): CompoundButton.OnCheckedChangeListener {
        private var isProgrammaticChange = false
        fun setProgrammaticChange(isProgrammaticChange: Boolean) {
            this.isProgrammaticChange = isProgrammaticChange
        }
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            if (!isProgrammaticChange) {
                var userSettings = SettingsUtil.loadAppTelegramUsers(buttonView!!.context)
                userSettings.users.filter { it.username == item.username }.forEach {
                    it.approved = isChecked
                }
                SettingsUtil.saveAppTelegramUsers(userSettings, buttonView!!.context)
            }
        }

    }

    var checkBoxListener: CompoundButton.OnCheckedChangeListener =
        object : CompoundButton.OnCheckedChangeListener {
            private var isProgrammaticChange = false
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                if (!isProgrammaticChange) {
                    // Ваш код для обработки изменения состояния CheckBox
                    if (isChecked) {
                        // CheckBox включен
                        // Ваш код
                    } else {
                        // CheckBox выключен
                        // Ваш код
                    }
                }
            }

            fun setProgrammaticChange(isProgrammaticChange: Boolean) {
                this.isProgrammaticChange = isProgrammaticChange
            }
        }
}

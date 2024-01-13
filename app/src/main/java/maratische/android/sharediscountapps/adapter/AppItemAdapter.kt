package maratische.android.sharediscountapps.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import maratische.android.sharediscountapps.ImageActivity
import maratische.android.sharediscountapps.MainActivity4
import maratische.android.sharediscountapps.R
import maratische.android.sharediscountapps.SettingsUtil
import maratische.android.sharediscountapps.TimeUtil.Companion.formatTimeFromLong
import maratische.android.sharediscountapps.model.AppItem

class AppItemAdapter(private val items: ArrayList<AppItem>) :
    RecyclerView.Adapter<AppItemAdapter.AppItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_item_adapter, parent, false)
        return AppItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<AppItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    class AppItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.text1)
        private val timeView: TextView = itemView.findViewById(R.id.time)
        private val timeSuccess: TextView = itemView.findViewById(R.id.timeSuccess)
        private val button: Button = itemView.findViewById(R.id.update_button)
        private val imagebutton: ImageButton = itemView.findViewById(R.id.image_button)
        private val checkboxActive: CheckBox = itemView.findViewById(R.id.checkbox_active)
        private var checkedChangeListener: OnCheckedChangeListener? = null


        fun bind(item: AppItem) {
            textView.text = item.name
            timeView.text = formatTimeFromLong(item.timeStart)
            timeSuccess.text = if (item.timeSuccess.toInt() == -1 || item.timeSuccess == item.timeStart) {
                " "
            } else {
                formatTimeFromLong(item.timeSuccess)
            }
            button.setOnClickListener {
                var settingsStart = SettingsUtil.loadSettings("start", itemView.context)
                settingsStart.timeLast = 0
                SettingsUtil.saveSettings("start", settingsStart, itemView.context)
                var settings = SettingsUtil.loadSettings(item.key, itemView.context)
                settings.timeLast = 0
                SettingsUtil.saveSettings(item.key, settings, itemView.context)
                itemView.context.sendBroadcast(Intent(MainActivity4.UPDATE_UI))
            }
            imagebutton.setOnClickListener {
                val intent = Intent(itemView.context, ImageActivity::class.java)
                intent.putExtra("key", item.key)
                itemView.context.startActivity(intent)
            }
            var settings = SettingsUtil.loadSettings(item.key, itemView.context)
            checkedChangeListener?.setProgrammaticChange(true)
            checkboxActive.isChecked = settings.active
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

    class OnCheckedChangeListener(private var item: AppItem): CompoundButton.OnCheckedChangeListener {
        private var isProgrammaticChange = false
        fun setProgrammaticChange(isProgrammaticChange: Boolean) {
            this.isProgrammaticChange = isProgrammaticChange
        }
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            if (!isProgrammaticChange) {
                var settings = SettingsUtil.loadSettings(item.key, buttonView!!.context)
                settings.active = isChecked
                SettingsUtil.saveSettings(item.key, settings, buttonView!!.context)
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

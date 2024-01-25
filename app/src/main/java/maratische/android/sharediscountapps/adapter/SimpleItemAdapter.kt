package maratische.android.sharediscountapps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import maratische.android.sharediscountapps.model.ErrorItem
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SimpleItemAdapter(private val items: ArrayList<ErrorItem>) :
    RecyclerView.Adapter<SimpleItemAdapter.SimpleItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return SimpleItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<ErrorItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    class SimpleItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        // Определение форматтера даты и времени
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")

        fun formatTimeFromLong(timeInMillis: Long): String = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault()).format(formatter)

        fun bind(item: ErrorItem) {
            textView.text = parseError(item)
        }

        private fun parseError(item: ErrorItem) : String {
            try {
                    val date = formatTimeFromLong(item.date)
                    val text = item.message
                    return "$date $text"
            } catch (e: Exception) {
                ;
            }
            return ""
        }
    }
}

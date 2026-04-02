package com.platoon.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.platoon.app.databinding.ItemReminderBinding
import com.platoon.app.model.Reminder

class RemindersAdapter(
    private val onDelete: (Reminder) -> Unit,
    private val onToggle: (Reminder) -> Unit
) : ListAdapter<Reminder, RemindersAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: Reminder) {
            binding.tvTitle.text = reminder.title
            binding.tvMessage.text = reminder.message
            binding.tvTime.text = String.format("%02d:%02d", reminder.hour, reminder.minute)
            binding.tvType.text = when (reminder.type) {
                Reminder.TYPE_DAILY -> "יומי"
                Reminder.TYPE_WEEKLY -> "שבועי"
                Reminder.TYPE_MONTHLY -> "חודשי"
                else -> reminder.type
            }
            binding.switchEnabled.isChecked = reminder.enabled
            binding.switchEnabled.setOnClickListener { onToggle(reminder) }
            binding.btnDelete.setOnClickListener { onDelete(reminder) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Reminder>() {
            override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder) = oldItem == newItem
        }
    }
}

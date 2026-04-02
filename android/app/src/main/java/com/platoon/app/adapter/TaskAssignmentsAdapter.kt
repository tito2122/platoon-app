package com.platoon.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.platoon.app.databinding.ItemTaskAssignmentBinding
import com.platoon.app.model.TaskAssignment

class TaskAssignmentsAdapter : ListAdapter<TaskAssignment, TaskAssignmentsAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemTaskAssignmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskAssignment) {
            binding.tvName.text = item.name
            binding.tvRole.text = item.role
            binding.tvTime.text = item.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskAssignmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TaskAssignment>() {
            override fun areItemsTheSame(oldItem: TaskAssignment, newItem: TaskAssignment) =
                oldItem.name == newItem.name && oldItem.time == newItem.time
            override fun areContentsTheSame(oldItem: TaskAssignment, newItem: TaskAssignment) = oldItem == newItem
        }
    }
}

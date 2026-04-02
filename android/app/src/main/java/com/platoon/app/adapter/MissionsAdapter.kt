package com.platoon.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.platoon.app.databinding.ItemMissionBinding
import com.platoon.app.model.Mission

class MissionsAdapter(
    private val getSoldierName: (Long) -> String,
    private val onDelete: (Mission) -> Unit,
    private val onAssignSoldiers: (Mission) -> Unit
) : ListAdapter<Mission, MissionsAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemMissionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mission: Mission) {
            binding.tvName.text = mission.name
            val soldierNames = mission.soldiers.joinToString(", ") { getSoldierName(it) }
            binding.tvSoldiers.text = if (soldierNames.isEmpty()) "אין חיילים משובצים" else soldierNames
            binding.tvCount.text = "${mission.soldiers.size} חיילים"
            binding.btnDelete.setOnClickListener { onDelete(mission) }
            binding.btnAssign.setOnClickListener { onAssignSoldiers(mission) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Mission>() {
            override fun areItemsTheSame(oldItem: Mission, newItem: Mission) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Mission, newItem: Mission) = oldItem == newItem
        }
    }
}

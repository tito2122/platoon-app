package com.platoon.app.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.platoon.app.R
import com.platoon.app.databinding.ItemSoldierBinding
import com.platoon.app.model.Soldier
import com.platoon.app.util.DateUtils

class SoldiersAdapter(
    private val onItemClick: (Soldier) -> Unit,
    private val onStatusToggle: (Soldier) -> Unit,
    private val onDelete: (Soldier) -> Unit
) : ListAdapter<Soldier, SoldiersAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemSoldierBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(soldier: Soldier) {
            binding.tvName.text = soldier.fullName
            binding.tvPersonalId.text = soldier.personalId
            binding.tvRoles.text = soldier.roles.joinToString(", ")

            val onLeave = DateUtils.isSoldierOnLeave(soldier)
            when {
                onLeave -> {
                    binding.tvStatus.text = "בחופשה"
                    binding.tvStatus.setTextColor(binding.root.resources.getColor(R.color.status_leave, null))
                    binding.viewStatusBar.setBackgroundColor(binding.root.resources.getColor(R.color.status_leave, null))
                }
                soldier.status == Soldier.STATUS_ABSENT -> {
                    binding.tvStatus.text = "נעדר"
                    binding.tvStatus.setTextColor(binding.root.resources.getColor(R.color.status_absent, null))
                    binding.viewStatusBar.setBackgroundColor(binding.root.resources.getColor(R.color.status_absent, null))
                }
                else -> {
                    binding.tvStatus.text = "נוכח"
                    binding.tvStatus.setTextColor(binding.root.resources.getColor(R.color.status_present, null))
                    binding.viewStatusBar.setBackgroundColor(binding.root.resources.getColor(R.color.status_present, null))
                }
            }

            // Photo
            if (soldier.photoData.isNotEmpty()) {
                try {
                    val bytes = Base64.decode(soldier.photoData, Base64.DEFAULT)
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.ivPhoto.setImageBitmap(bmp)
                } catch (e: Exception) {
                    binding.ivPhoto.setImageResource(R.drawable.ic_person)
                }
            } else {
                binding.ivPhoto.setImageResource(R.drawable.ic_person)
            }

            // Hofpa badge
            binding.tvHofpa.visibility = if (soldier.hofpa) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { onItemClick(soldier) }
            binding.tvStatus.setOnClickListener { onStatusToggle(soldier) }
            binding.btnDelete.setOnClickListener { onDelete(soldier) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSoldierBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Soldier>() {
            override fun areItemsTheSame(oldItem: Soldier, newItem: Soldier) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Soldier, newItem: Soldier) = oldItem == newItem
        }
    }
}

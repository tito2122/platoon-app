package com.platoon.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.platoon.app.databinding.ItemListBinding
import com.platoon.app.model.PlatoonList

class ListsAdapter(
    private val onDelete: (PlatoonList) -> Unit,
    private val onAddItem: (PlatoonList) -> Unit,
    private val onRemoveItem: (PlatoonList, Int) -> Unit
) : ListAdapter<PlatoonList, ListsAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(list: PlatoonList) {
            binding.tvName.text = list.name
            binding.tvCount.text = "${list.items.size} פריטים"
            binding.tvItems.text = list.items.joinToString("\n• ", "• ")
            binding.btnDelete.setOnClickListener { onDelete(list) }
            binding.btnAddItem.setOnClickListener { onAddItem(list) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PlatoonList>() {
            override fun areItemsTheSame(oldItem: PlatoonList, newItem: PlatoonList) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: PlatoonList, newItem: PlatoonList) = oldItem == newItem
        }
    }
}

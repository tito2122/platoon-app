package com.platoon.app.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.platoon.app.MainActivity
import com.platoon.app.adapter.ListsAdapter
import com.platoon.app.databinding.FragmentListsBinding
import com.platoon.app.model.PlatoonList
import com.platoon.app.viewmodel.MainViewModel
import java.util.UUID

class ListsFragment : Fragment() {

    private var _binding: FragmentListsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ListsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as MainActivity).viewModel

        setupRecyclerView()
        setupFab()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = ListsAdapter(
            onDelete = { list ->
                AlertDialog.Builder(requireContext())
                    .setTitle("מחיקת רשימה")
                    .setMessage("האם למחוק את ${list.name}?")
                    .setPositiveButton("מחק") { _, _ -> viewModel.deleteList(list.id) }
                    .setNegativeButton("ביטול", null)
                    .show()
            },
            onAddItem = { list -> showAddItemDialog(list) },
            onRemoveItem = { list, index ->
                val items = list.items.toMutableList()
                items.removeAt(index)
                viewModel.saveList(list.copy(items = items))
            }
        )
        binding.rvLists.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener { showCreateListDialog() }
    }

    private fun observeData() {
        viewModel.lists.observe(viewLifecycleOwner) { lists ->
            adapter.submitList(lists)
            binding.tvEmpty.visibility = if (lists.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showCreateListDialog() {
        val editText = android.widget.EditText(requireContext()).apply {
            hint = "שם הרשימה"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("רשימה חדשה")
            .setView(editText)
            .setPositiveButton("צור") { _, _ ->
                val name = editText.text?.toString()?.trim() ?: ""
                if (name.isNotEmpty()) {
                    val list = PlatoonList(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        createdAt = System.currentTimeMillis()
                    )
                    viewModel.saveList(list)
                } else {
                    Toast.makeText(requireContext(), "יש להזין שם", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun showAddItemDialog(list: PlatoonList) {
        val editText = android.widget.EditText(requireContext()).apply {
            hint = "פריט חדש"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("הוסף פריט")
            .setView(editText)
            .setPositiveButton("הוסף") { _, _ ->
                val item = editText.text?.toString()?.trim() ?: ""
                if (item.isNotEmpty()) {
                    val items = list.items.toMutableList()
                    items.add(item)
                    viewModel.saveList(list.copy(items = items))
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

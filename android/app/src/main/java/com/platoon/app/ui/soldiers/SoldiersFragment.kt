package com.platoon.app.ui.soldiers

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.platoon.app.MainActivity
import com.platoon.app.R
import com.platoon.app.adapter.SoldiersAdapter
import com.platoon.app.databinding.FragmentSoldiersBinding
import com.platoon.app.model.Soldier
import com.platoon.app.util.DateUtils
import com.platoon.app.viewmodel.MainViewModel

class SoldiersFragment : Fragment() {

    private var _binding: FragmentSoldiersBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: SoldiersAdapter
    private var filterStatus = "all"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSoldiersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as MainActivity).viewModel

        setupRecyclerView()
        setupSearch()
        setupFilterChips()
        setupFab()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = SoldiersAdapter(
            onItemClick = { soldier ->
                findNavController().navigate(
                    R.id.action_soldiers_to_detail,
                    bundleOf("soldierId" to soldier.id)
                )
            },
            onStatusToggle = { soldier ->
                val newStatus = if (soldier.status == Soldier.STATUS_PRESENT)
                    Soldier.STATUS_ABSENT else Soldier.STATUS_PRESENT
                viewModel.saveSoldier(soldier.copy(status = newStatus))
            },
            onDelete = { soldier ->
                AlertDialog.Builder(requireContext())
                    .setTitle("מחיקת חייל")
                    .setMessage("האם למחוק את ${soldier.fullName}?")
                    .setPositiveButton("מחק") { _, _ -> viewModel.deleteSoldier(soldier.id) }
                    .setNegativeButton("ביטול", null)
                    .show()
            }
        )
        binding.rvSoldiers.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filterSoldiers()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener { filterStatus = "all"; filterSoldiers() }
        binding.chipPresent.setOnClickListener { filterStatus = Soldier.STATUS_PRESENT; filterSoldiers() }
        binding.chipAbsent.setOnClickListener { filterStatus = Soldier.STATUS_ABSENT; filterSoldiers() }
        binding.chipLeave.setOnClickListener { filterStatus = "leave"; filterSoldiers() }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                R.id.action_soldiers_to_detail,
                bundleOf("soldierId" to -1L)
            )
        }
    }

    private fun observeData() {
        viewModel.soldiers.observe(viewLifecycleOwner) { filterSoldiers() }
        viewModel.missing.observe(viewLifecycleOwner) { updateMissingBadge() }
    }

    private fun filterSoldiers() {
        val query = binding.etSearch.text?.toString()?.lowercase() ?: ""
        val soldiers = viewModel.soldiers.value ?: emptyList()

        val filtered = soldiers.filter { soldier ->
            val matchesSearch = query.isEmpty() ||
                    soldier.fullName.lowercase().contains(query) ||
                    soldier.personalId.contains(query)

            val onLeave = DateUtils.isSoldierOnLeave(soldier)
            val matchesFilter = when (filterStatus) {
                Soldier.STATUS_PRESENT -> soldier.status == Soldier.STATUS_PRESENT && !onLeave
                Soldier.STATUS_ABSENT -> soldier.status == Soldier.STATUS_ABSENT
                "leave" -> onLeave
                else -> true
            }
            matchesSearch && matchesFilter
        }

        adapter.submitList(filtered)
        updateStats(soldiers)
    }

    private fun updateStats(soldiers: List<Soldier>) {
        val total = soldiers.size
        val onLeave = soldiers.count { DateUtils.isSoldierOnLeave(it) }
        val absent = soldiers.count { it.status == Soldier.STATUS_ABSENT }
        val present = total - onLeave - absent

        binding.tvPresent.text = "נוכחים: $present"
        binding.tvAbsent.text = "נעדרים: $absent"
        binding.tvLeave.text = "בחופשה: $onLeave"
        binding.tvTotal.text = "סה\"כ: $total"
    }

    private fun updateMissingBadge() {
        val count = viewModel.missing.value?.size ?: 0
        if (count > 0) {
            binding.tvMissingBadge.text = "חסרים: $count"
            binding.tvMissingBadge.visibility = View.VISIBLE
        } else {
            binding.tvMissingBadge.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

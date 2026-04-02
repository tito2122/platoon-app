package com.platoon.app.ui.missions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.platoon.app.MainActivity
import com.platoon.app.adapter.MissionsAdapter
import com.platoon.app.databinding.FragmentMissionsBinding
import com.platoon.app.model.Mission
import com.platoon.app.viewmodel.MainViewModel

class MissionsFragment : Fragment() {

    private var _binding: FragmentMissionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: MissionsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMissionsBinding.inflate(inflater, container, false)
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
        adapter = MissionsAdapter(
            getSoldierName = { id -> viewModel.getSoldierById(id)?.fullName ?: "חייל $id" },
            onDelete = { mission ->
                AlertDialog.Builder(requireContext())
                    .setTitle("מחיקת משימה")
                    .setMessage("האם למחוק את ${mission.name}?")
                    .setPositiveButton("מחק") { _, _ -> viewModel.deleteMission(mission.id) }
                    .setNegativeButton("ביטול", null)
                    .show()
            },
            onAssignSoldiers = { mission -> showAssignDialog(mission) }
        )
        binding.rvMissions.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener { showAddMissionDialog() }
    }

    private fun observeData() {
        viewModel.missions.observe(viewLifecycleOwner) { missions ->
            adapter.submitList(missions)
            binding.tvEmpty.visibility = if (missions.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAddMissionDialog() {
        val editText = android.widget.EditText(requireContext()).apply {
            hint = "שם המשימה"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("משימה חדשה")
            .setView(editText)
            .setPositiveButton("הוסף") { _, _ ->
                val name = editText.text?.toString()?.trim() ?: ""
                if (name.isNotEmpty()) {
                    val mission = Mission(
                        id = viewModel.getNextMissionId(),
                        name = name,
                        createdAt = System.currentTimeMillis()
                    )
                    viewModel.saveMission(mission)
                } else {
                    Toast.makeText(requireContext(), "יש להזין שם", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun showAssignDialog(mission: Mission) {
        val soldiers = viewModel.soldiers.value ?: return
        val names = soldiers.map { it.fullName }.toTypedArray()
        val checked = soldiers.map { mission.soldiers.contains(it.id) }.toBooleanArray()

        AlertDialog.Builder(requireContext())
            .setTitle("שיבוץ חיילים")
            .setMultiChoiceItems(names, checked) { _, index, isChecked ->
                checked[index] = isChecked
            }
            .setPositiveButton("שמור") { _, _ ->
                val assignedIds = soldiers.indices
                    .filter { checked[it] }
                    .map { soldiers[it].id }
                viewModel.saveMission(mission.copy(soldiers = assignedIds))
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

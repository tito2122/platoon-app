package com.platoon.app.ui.detail

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.platoon.app.MainActivity
import com.platoon.app.R
import com.platoon.app.databinding.FragmentSoldierDetailBinding
import com.platoon.app.model.Leave
import com.platoon.app.model.Soldier
import com.platoon.app.util.DateUtils
import com.platoon.app.viewmodel.MainViewModel
import java.io.ByteArrayOutputStream

class SoldierDetailFragment : Fragment() {

    private var _binding: FragmentSoldierDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private var soldierId: Long = -1L
    private var photoBase64: String = ""
    private var currentSoldier: Soldier? = null

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { loadPhoto(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSoldierDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as MainActivity).viewModel
        soldierId = arguments?.getLong("soldierId") ?: -1L

        setupToolbar()
        setupPhotoButton()
        setupRolesChips()
        setupSaveButton()
        setupLeaveButton()

        if (soldierId != -1L) {
            loadSoldierData()
        } else {
            binding.toolbar.title = "הוסף חייל"
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    private fun setupPhotoButton() {
        binding.ivPhoto.setOnClickListener { pickPhoto.launch("image/*") }
        binding.btnPickPhoto.setOnClickListener { pickPhoto.launch("image/*") }
    }

    private fun setupRolesChips() {
        val roles = resources.getStringArray(R.array.soldier_roles)
        roles.forEach { role ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = role
                isCheckable = true
                setChipBackgroundColorResource(R.color.chip_bg_selector)
            }
            binding.chipGroupRoles.addView(chip)
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener { saveSoldier() }
    }

    private fun setupLeaveButton() {
        binding.btnAddLeave.setOnClickListener { showAddLeaveDialog() }
    }

    private fun loadSoldierData() {
        val soldier = viewModel.getSoldierById(soldierId) ?: return
        currentSoldier = soldier
        binding.toolbar.title = soldier.fullName

        binding.etFirstName.setText(soldier.firstName)
        binding.etLastName.setText(soldier.lastName)
        binding.etPersonalId.setText(soldier.personalId)
        binding.etPhone.setText(soldier.phone)
        binding.etResidence.setText(soldier.residence)
        binding.etNotes.setText(soldier.notes)
        binding.switchHofpa.isChecked = soldier.hofpa
        binding.switchExtra1.isChecked = soldier.extra1
        binding.switchHasWife.isChecked = soldier.hasWife
        binding.etWifeName.setText(soldier.wifeName)
        binding.etWifePhone.setText(soldier.wifePhone)

        // Status
        if (soldier.status == Soldier.STATUS_ABSENT) {
            binding.radioAbsent.isChecked = true
        } else {
            binding.radioPresent.isChecked = true
        }

        // Photo
        if (soldier.photoData.isNotEmpty()) {
            photoBase64 = soldier.photoData
            try {
                val bytes = Base64.decode(soldier.photoData, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.ivPhoto.setImageBitmap(bmp)
            } catch (e: Exception) { /* skip */ }
        }

        // Roles chips
        val roleChips = binding.chipGroupRoles
        for (i in 0 until roleChips.childCount) {
            val chip = roleChips.getChildAt(i) as? com.google.android.material.chip.Chip
            chip?.isChecked = soldier.roles.contains(chip?.text?.toString())
        }

        // Leaves
        updateLeavesList(soldier.leaves)
    }

    private fun updateLeavesList(leaves: List<Leave>) {
        binding.llLeaves.removeAllViews()
        leaves.forEachIndexed { index, leave ->
            val tv = android.widget.TextView(requireContext()).apply {
                text = "${DateUtils.formatDate(leave.from)} - ${DateUtils.formatDate(leave.to)}"
                textSize = 14f
                setPadding(0, 8, 0, 8)
            }
            val btnDelete = android.widget.Button(requireContext()).apply {
                text = "הסר"
                setOnClickListener {
                    val updated = currentSoldier?.leaves?.toMutableList() ?: mutableListOf()
                    updated.removeAt(index)
                    currentSoldier = currentSoldier?.copy(leaves = updated)
                    updateLeavesList(updated)
                }
            }
            val row = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                addView(tv, android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                addView(btnDelete)
            }
            binding.llLeaves.addView(row)
        }
    }

    private fun showAddLeaveDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_leave, null)
        val etFrom = dialogView.findViewById<android.widget.EditText>(R.id.etFromDate)
        val etTo = dialogView.findViewById<android.widget.EditText>(R.id.etToDate)

        AlertDialog.Builder(requireContext())
            .setTitle("הוסף חופשה")
            .setView(dialogView)
            .setPositiveButton("הוסף") { _, _ ->
                val from = etFrom.text?.toString()?.trim() ?: ""
                val to = etTo.text?.toString()?.trim() ?: ""
                if (from.isNotEmpty() && to.isNotEmpty()) {
                    val updated = currentSoldier?.leaves?.toMutableList() ?: mutableListOf()
                    updated.add(Leave(from, to))
                    currentSoldier = currentSoldier?.copy(leaves = updated)
                        ?: Soldier(leaves = updated)
                    updateLeavesList(updated)
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun saveSoldier() {
        val firstName = binding.etFirstName.text?.toString()?.trim() ?: ""
        val lastName = binding.etLastName.text?.toString()?.trim() ?: ""

        if (firstName.isEmpty()) {
            Toast.makeText(requireContext(), "יש להזין שם פרטי", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRoles = mutableListOf<String>()
        for (i in 0 until binding.chipGroupRoles.childCount) {
            val chip = binding.chipGroupRoles.getChildAt(i) as? com.google.android.material.chip.Chip
            if (chip?.isChecked == true) selectedRoles.add(chip.text.toString())
        }

        val id = if (soldierId == -1L) viewModel.getNextSoldierId() else soldierId
        val status = if (binding.radioAbsent.isChecked) Soldier.STATUS_ABSENT else Soldier.STATUS_PRESENT

        val soldier = (currentSoldier ?: Soldier(id = id)).copy(
            id = id,
            firstName = firstName,
            lastName = lastName,
            personalId = binding.etPersonalId.text?.toString()?.trim() ?: "",
            phone = binding.etPhone.text?.toString()?.trim() ?: "",
            residence = binding.etResidence.text?.toString()?.trim() ?: "",
            notes = binding.etNotes.text?.toString()?.trim() ?: "",
            hofpa = binding.switchHofpa.isChecked,
            extra1 = binding.switchExtra1.isChecked,
            hasWife = binding.switchHasWife.isChecked,
            wifeName = binding.etWifeName.text?.toString()?.trim() ?: "",
            wifePhone = binding.etWifePhone.text?.toString()?.trim() ?: "",
            roles = selectedRoles,
            status = status,
            photoData = photoBase64
        )

        viewModel.saveSoldier(soldier)
        Toast.makeText(requireContext(), "נשמר בהצלחה", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun loadPhoto(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, 256, 256, true)
            val bos = ByteArrayOutputStream()
            scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, bos)
            photoBase64 = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT)
            binding.ivPhoto.setImageBitmap(scaled)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "שגיאה בטעינת תמונה", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

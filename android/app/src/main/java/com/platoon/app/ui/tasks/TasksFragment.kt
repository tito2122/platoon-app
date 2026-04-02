package com.platoon.app.ui.tasks

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.platoon.app.adapter.TaskAssignmentsAdapter
import com.platoon.app.databinding.FragmentTasksBinding
import com.platoon.app.network.ClaudeApiService
import com.platoon.app.network.AnalyzeTaskRequest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TaskAssignmentsAdapter
    private var selectedImageBase64: String = ""
    private var selectedMimeType: String = "image/jpeg"

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { loadImage(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TaskAssignmentsAdapter()
        binding.rvAssignments.adapter = adapter

        binding.btnPickImage.setOnClickListener { pickImage.launch("image/*") }
        binding.btnAnalyze.setOnClickListener { analyzeImage() }
    }

    private fun loadImage(uri: Uri) {
        try {
            val contentType = requireContext().contentResolver.getType(uri) ?: "image/jpeg"
            selectedMimeType = contentType

            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            binding.ivPreview.setImageBitmap(bitmap)
            binding.ivPreview.visibility = View.VISIBLE

            val bos = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, bos)
            selectedImageBase64 = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT)
            binding.btnAnalyze.isEnabled = true
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "שגיאה בטעינת תמונה", Toast.LENGTH_SHORT).show()
        }
    }

    private fun analyzeImage() {
        if (selectedImageBase64.isEmpty()) {
            Toast.makeText(requireContext(), "יש לבחור תמונה", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val service = ClaudeApiService.create()
                val request = AnalyzeTaskRequest(
                    base64 = selectedImageBase64,
                    mimeType = selectedMimeType
                )
                val response = service.analyzeTask(request)
                adapter.submitList(response.assignments)
                binding.tvResultCount.text = "נמצאו ${response.assignments.size} שיבוצים"
                binding.tvResultCount.visibility = View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "שגיאה בניתוח: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnAnalyze.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

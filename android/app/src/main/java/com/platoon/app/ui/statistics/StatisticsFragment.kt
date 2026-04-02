package com.platoon.app.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.platoon.app.MainActivity
import com.platoon.app.databinding.FragmentStatisticsBinding
import com.platoon.app.model.Soldier
import com.platoon.app.util.DateUtils
import com.platoon.app.viewmodel.MainViewModel

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as MainActivity).viewModel

        viewModel.soldiers.observe(viewLifecycleOwner) { updateStats(it) }
        viewModel.missing.observe(viewLifecycleOwner) { soldiers ->
            viewModel.soldiers.value?.let { updateStats(it) }
        }
    }

    private fun updateStats(soldiers: List<Soldier>) {
        val total = soldiers.size
        val onLeave = soldiers.count { DateUtils.isSoldierOnLeave(it) }
        val absent = soldiers.count { it.status == Soldier.STATUS_ABSENT }
        val present = total - onLeave - absent
        val missing = viewModel.missing.value?.size ?: 0
        val hofpa = soldiers.count { it.hofpa }

        binding.tvTotal.text = total.toString()
        binding.tvPresent.text = present.toString()
        binding.tvAbsent.text = absent.toString()
        binding.tvLeave.text = onLeave.toString()
        binding.tvMissing.text = missing.toString()
        binding.tvHofpa.text = hofpa.toString()

        // Role breakdown
        val roleCount = mutableMapOf<String, Int>()
        soldiers.forEach { soldier ->
            soldier.roles.forEach { role ->
                roleCount[role] = (roleCount[role] ?: 0) + 1
            }
        }

        binding.llRoles.removeAllViews()
        roleCount.entries.sortedByDescending { it.value }.forEach { (role, count) ->
            val tv = android.widget.TextView(requireContext()).apply {
                text = "$role: $count"
                textSize = 14f
                setPadding(0, 8, 0, 8)
            }
            binding.llRoles.addView(tv)
        }

        // Present percentage bar
        val presentPercent = if (total > 0) (present.toFloat() / total * 100).toInt() else 0
        binding.progressPresent.progress = presentPercent
        binding.tvPresentPercent.text = "$presentPercent%"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

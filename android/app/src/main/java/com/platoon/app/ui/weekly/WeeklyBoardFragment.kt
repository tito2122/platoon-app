package com.platoon.app.ui.weekly

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.platoon.app.MainActivity
import com.platoon.app.R
import com.platoon.app.databinding.FragmentWeeklyBoardBinding
import com.platoon.app.model.Soldier
import com.platoon.app.util.DateUtils
import com.platoon.app.viewmodel.MainViewModel
import java.util.Calendar

class WeeklyBoardFragment : Fragment() {

    private var _binding: FragmentWeeklyBoardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private var currentYear = DateUtils.currentYear()
    private var currentMonth = DateUtils.currentMonth()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWeeklyBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as MainActivity).viewModel

        binding.btnPrevMonth.setOnClickListener {
            if (currentMonth == 1) { currentMonth = 12; currentYear-- }
            else currentMonth--
            renderBoard()
        }

        binding.btnNextMonth.setOnClickListener {
            if (currentMonth == 12) { currentMonth = 1; currentYear++ }
            else currentMonth++
            renderBoard()
        }

        viewModel.soldiers.observe(viewLifecycleOwner) { renderBoard() }
        renderBoard()
    }

    private fun renderBoard() {
        val monthName = DateUtils.hebrewMonths[currentMonth - 1]
        binding.tvMonthYear.text = "$monthName $currentYear"

        val soldiers = viewModel.soldiers.value ?: emptyList()
        val dates = DateUtils.getDatesInMonth(currentYear, currentMonth)

        binding.tableContainer.removeAllViews()

        // Header row
        val headerRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        headerRow.addView(makeCell("שם", true, 160))
        dates.forEach { date ->
            val cal = Calendar.getInstance()
            cal.set(currentYear, currentMonth - 1, date.split("-")[2].toInt())
            val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 6 else it - 1 }
            val dayLabel = "${date.split("-")[2]}\n${DateUtils.hebrewDays[dayOfWeek]}"
            val cell = makeCell(dayLabel, true, 44)
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            if (dow == Calendar.SATURDAY) cell.setBackgroundColor(Color.parseColor("#FFE0E0"))
            headerRow.addView(cell)
        }
        binding.tableContainer.addView(headerRow)

        // Soldier rows - only soldiers with any leave in this month
        val soldiersWithLeave = soldiers.filter { soldier ->
            dates.any { date -> isOnLeaveDate(soldier, date) }
        }

        soldiersWithLeave.forEach { soldier ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutDirection = View.LAYOUT_DIRECTION_RTL
            }
            row.addView(makeCell(soldier.fullName, false, 160))
            dates.forEach { date ->
                val onLeave = isOnLeaveDate(soldier, date)
                val cell = makeCell(if (onLeave) "✓" else "", false, 44)
                if (onLeave) cell.setBackgroundColor(Color.parseColor("#A8E6CF"))
                row.addView(cell)
            }
            binding.tableContainer.addView(row)
        }

        if (soldiersWithLeave.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "אין חופשות בחודש זה"
                gravity = Gravity.CENTER
                setPadding(16, 32, 16, 32)
            }
            binding.tableContainer.addView(tv)
        }
    }

    private fun isOnLeaveDate(soldier: Soldier, date: String): Boolean {
        val allLeaves = soldier.leaves.toMutableList()
        if (soldier.nextApproved && soldier.nextFrom.isNotEmpty() && soldier.nextTo.isNotEmpty()) {
            allLeaves.add(com.platoon.app.model.Leave(soldier.nextFrom, soldier.nextTo))
        }
        return allLeaves.any { leave -> DateUtils.isDateInRange(date, leave.from, leave.to) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun makeCell(text: String, isHeader: Boolean, widthDp: Int): TextView {
        val density = resources.displayMetrics.density
        return TextView(requireContext()).apply {
            this.text = text
            gravity = Gravity.CENTER
            textSize = if (isHeader) 11f else 12f
            if (isHeader) setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(4, 8, 4, 8)
            layoutParams = LinearLayout.LayoutParams(
                (widthDp * density).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundResource(R.drawable.cell_border)
        }
    }
}

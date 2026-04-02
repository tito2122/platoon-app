package com.platoon.app.ui.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.platoon.app.MainActivity
import com.platoon.app.R
import com.platoon.app.adapter.RemindersAdapter
import com.platoon.app.databinding.FragmentRemindersBinding
import com.platoon.app.model.Reminder
import com.platoon.app.util.ReminderReceiver
import com.platoon.app.viewmodel.MainViewModel
import java.util.Calendar

class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: RemindersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
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
        adapter = RemindersAdapter(
            onDelete = { reminder ->
                viewModel.deleteReminder(reminder.id)
                cancelAlarm(reminder)
            },
            onToggle = { reminder ->
                val updated = reminder.copy(enabled = !reminder.enabled)
                viewModel.saveReminder(updated)
                if (updated.enabled) scheduleAlarm(updated) else cancelAlarm(updated)
            }
        )
        binding.rvReminders.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener { showAddReminderDialog() }
    }

    private fun observeData() {
        viewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            adapter.submitList(reminders)
            binding.tvEmpty.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etMessage = dialogView.findViewById<EditText>(R.id.etMessage)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerType)
        timePicker.setIs24HourView(true)

        val types = arrayOf("יומי", "שבועי", "חודשי")
        spinnerType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)

        AlertDialog.Builder(requireContext())
            .setTitle("תזכורת חדשה")
            .setView(dialogView)
            .setPositiveButton("הוסף") { _, _ ->
                val title = etTitle.text?.toString()?.trim() ?: ""
                val message = etMessage.text?.toString()?.trim() ?: ""
                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "יש להזין כותרת", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val type = when (spinnerType.selectedItemPosition) {
                    1 -> Reminder.TYPE_WEEKLY
                    2 -> Reminder.TYPE_MONTHLY
                    else -> Reminder.TYPE_DAILY
                }
                val hour = timePicker.hour
                val minute = timePicker.minute
                val reminder = Reminder(
                    id = viewModel.getNextReminderId(),
                    type = type,
                    hour = hour,
                    minute = minute,
                    title = title,
                    message = message,
                    enabled = true
                )
                viewModel.saveReminder(reminder)
                scheduleAlarm(reminder)
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun scheduleAlarm(reminder: Reminder) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("title", reminder.title)
            putExtra("message", reminder.message)
        }
        val pi = PendingIntent.getBroadcast(
            requireContext(), reminder.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        when (reminder.type) {
            Reminder.TYPE_DAILY -> alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pi
            )
            else -> alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }

    private fun cancelAlarm(reminder: Reminder) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            requireContext(), reminder.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

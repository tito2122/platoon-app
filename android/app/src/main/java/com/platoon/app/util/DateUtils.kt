package com.platoon.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun today(): String = dateFormat.format(Date())

    fun formatDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance().apply { time = date }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)
            "$day/$month/$year"
        } catch (e: Exception) {
            dateStr
        }
    }

    fun isDateInRange(dateStr: String, fromStr: String, toStr: String): Boolean {
        return try {
            val date = dateFormat.parse(dateStr) ?: return false
            val from = dateFormat.parse(fromStr) ?: return false
            val to = dateFormat.parse(toStr) ?: return false
            !date.before(from) && !date.after(to)
        } catch (e: Exception) {
            false
        }
    }

    fun isSoldierOnLeave(soldier: com.platoon.app.model.Soldier): Boolean {
        val today = today()
        return soldier.leaves.any { leave ->
            isDateInRange(today, leave.from, leave.to)
        } || (soldier.nextApproved && soldier.nextFrom.isNotEmpty() && soldier.nextTo.isNotEmpty() &&
                isDateInRange(today, soldier.nextFrom, soldier.nextTo))
    }

    fun getDatesInMonth(year: Int, month: Int): List<String> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..daysInMonth).map { day ->
            String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day)
        }
    }

    fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    val hebrewMonths = listOf(
        "ינואר", "פברואר", "מרץ", "אפריל", "מאי", "יוני",
        "יולי", "אוגוסט", "ספטמבר", "אוקטובר", "נובמבר", "דצמבר"
    )

    val hebrewDays = listOf("א", "ב", "ג", "ד", "ה", "ו", "ש")
}

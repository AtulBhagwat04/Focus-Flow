package com.focusflow.core.domain.blocking.model

/**
 * Time schedule defining recurring intervals when target apps are restricted.
 *
 * @property id Unique identifier.
 * @property name User-friendly label (e.g. "Work Hours", "Bedtime").
 * @property daysOfWeek Set of active DayOfWeek integers (1 = Monday, 7 = Sunday per java.time).
 * @property startMinuteOfDay Minute of day (0 to 1439).
 * @property endMinuteOfDay Minute of day (0 to 1439). Supports crossing midnight (e.g. 22:00 to 07:00).
 * @property packageNames Set of apps subject to this schedule.
 * @property isEnabled Whether this schedule is active.
 */
data class BlockSchedule(
    val id: String,
    val name: String,
    val daysOfWeek: Set<Int>,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val packageNames: Set<String>,
    val isEnabled: Boolean = true,
) {
    /**
     * Checks if this schedule is currently active for the given day of week and minute.
     */
    fun isActiveAt(dayOfWeek: Int, minuteOfDay: Int): Boolean {
        if (!isEnabled || !daysOfWeek.contains(dayOfWeek)) return false

        return if (startMinuteOfDay <= endMinuteOfDay) {
            // Same day interval (e.g. 09:00 to 17:00)
            minuteOfDay in startMinuteOfDay until endMinuteOfDay
        } else {
            // Crosses midnight (e.g. 22:00 to 07:00)
            minuteOfDay >= startMinuteOfDay || minuteOfDay < endMinuteOfDay
        }
    }
}

package com.beautyplanner.client.domain.model

/**
 * A point-in-time snapshot of a master's public schedule, used by the client
 * app to compute free/busy slots locally without a round-trip per time slot.
 */
data class ScheduleSnapshot(
    val masterId: String,
    /** Working day start time in HH:mm format. Defaults to "09:00". */
    val workDayStart: String = "09:00",
    /** Working day end time in HH:mm format. Defaults to "18:00". */
    val workDayEnd: String = "18:00",
    /**
     * When false the busy slots are not published and the client app should
     * treat the whole working day as potentially free (server decides on submit).
     */
    val autoPublishBusySlots: Boolean = false,
    val busySlots: List<BusySlotModel> = emptyList(),
    val weeklyBlockedIntervals: List<WeeklyBlockedInterval> = emptyList(),
    val dateOverrides: List<DateOverride> = emptyList()
)

/** A single booked/busy interval on a specific date. */
data class BusySlotModel(
    /** "YYYY-MM-DD" */
    val date: String,
    /** "HH:mm" */
    val startTime: String,
    /** "HH:mm" */
    val endTime: String
)

/**
 * A recurring weekly blocked interval (e.g. lunch break every Monday).
 * [dayOfWeek] follows ISO convention: 1 = Monday … 7 = Sunday.
 */
data class WeeklyBlockedInterval(
    val dayOfWeek: Int,
    /** "HH:mm" */
    val startTime: String,
    /** "HH:mm" */
    val endTime: String
)

/**
 * Override for a specific date — marks it as a working or non-working day
 * regardless of the weekly schedule.
 */
data class DateOverride(
    /** "YYYY-MM-DD" */
    val date: String,
    val isWorkingDay: Boolean
)

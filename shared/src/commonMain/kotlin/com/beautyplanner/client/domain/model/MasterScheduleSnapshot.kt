package com.beautyplanner.client.domain.model

data class BusySlot(
    val date: String,
    val startTime: String,
    val endTime: String
)

data class WeeklyBlockedInterval(
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val isActive: Boolean = true
)

data class ScheduleDateOverride(
    val date: String,
    val unblockAll: Boolean = false
)

data class MasterScheduleSnapshot(
    val workStartTime: String = "08:00",
    val workEndTime: String = "20:00",
    val busySlots: List<BusySlot> = emptyList(),
    val weeklyBlockedIntervals: List<WeeklyBlockedInterval> = emptyList(),
    val dateOverrides: List<ScheduleDateOverride> = emptyList()
)
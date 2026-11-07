package com.focusflow.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.permission.AppPermission
import com.focusflow.core.domain.permission.PermissionHealthRepository
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.repository.UserPreferencesRepository
import com.focusflow.core.domain.usage.model.DailyUsageStats
import com.focusflow.core.domain.usage.repository.UsageStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val MILLIS_PER_HOUR = 3600_000L
private const val TOP_APPS_LIMIT = 5

private data class PeriodBounds(
    val startDay: Long,
    val endDay: Long,
    val startMs: Long,
    val endMs: Long,
    val prevStartMs: Long,
    val prevEndMs: Long,
    val label: String,
    val subtitle: String,
    val heroLabel: String,
    val chartTitle: String,
    val goalMs: Long,
    val goalLabel: String,
    val dailyGoalHours: Float,
    val dailyGoalFormatted: String,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    private val permissionHealthRepository: PermissionHealthRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock,
) : ViewModel() {

    private val selectedRange = MutableStateFlow(StatsTimeRange.WEEK)
    private val periodOffset = MutableStateFlow(0)
    private val isRefreshing = MutableStateFlow(false)
    private var syncJob: Job? = null

    val uiState: StateFlow<StatsUiState> = combine(
        selectedRange,
        periodOffset,
        userPreferencesRepository.getWeeklyGoalHours(),
    ) { range, offset, weeklyGoalHours ->
        Triple(range, offset, weeklyGoalHours)
    }.flatMapLatest { (range, offset, weeklyGoalHours) ->
        val bounds = calculatePeriodBounds(range, offset, weeklyGoalHours)

        // Asynchronously query and sync live device usage stats for this period range
        launchRefreshForRange(bounds.startDay, bounds.endDay)

        val sessionsFlow = combine(
            focusSessionRepository.observeCompletedSessionsBetween(bounds.startMs, bounds.endMs),
            focusSessionRepository.observeFocusDurationBetween(bounds.prevStartMs, bounds.prevEndMs),
            focusSessionRepository.observeAllCompletedSessions(),
        ) { sessions, prevFocusMs, allSessions ->
            Triple(sessions, prevFocusMs, allSessions)
        }

        val usageFlow = combine(
            usageStatsRepository.observeRangeUsage(bounds.startDay, bounds.endDay),
            usageStatsRepository.observeDateRangeUsage(bounds.startDay, bounds.endDay),
        ) { rangeStats, dateRangeStatsList ->
            rangeStats to dateRangeStatsList
        }

        combine(
            usageFlow,
            sessionsFlow,
            permissionHealthRepository.permissionHealth,
            isRefreshing,
        ) { (stats, dateRangeStatsList), (sessions, prevFocusMs, allSessions), health, refreshing ->
            val hasUsage = health.stateOf(AppPermission.USAGE_ACCESS).isGranted
            buildUiState(
                range = range,
                offset = offset,
                bounds = bounds,
                stats = stats,
                dailyStatsList = dateRangeStatsList,
                sessions = sessions,
                prevPeriodFocusMs = prevFocusMs,
                allSessions = allSessions,
                hasPermission = hasUsage,
                isLoading = refreshing,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = StatsUiState(isLoading = true),
    )

    init {
        refresh()
    }

    fun onSelectRange(range: StatsTimeRange) {
        selectedRange.value = range
        periodOffset.value = 0
        refresh()
    }

    fun onPreviousPeriod() {
        periodOffset.value = periodOffset.value - 1
        refresh()
    }

    fun onNextPeriod() {
        if (periodOffset.value < 0) {
            periodOffset.value = periodOffset.value + 1
            refresh()
        }
    }

    fun onResetToCurrentPeriod() {
        periodOffset.value = 0
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            isRefreshing.value = true
            try {
                permissionHealthRepository.refreshHealth()
                val weeklyGoal = try {
                    userPreferencesRepository.getWeeklyGoalHours().first()
                } catch (_: Exception) {
                    16
                }
                val bounds = calculatePeriodBounds(selectedRange.value, periodOffset.value, weeklyGoal)
                val zoneId = ZoneId.systemDefault()
                val todayEpochDay = LocalDate.ofInstant(Instant.ofEpochMilli(clock.now()), zoneId).toEpochDay()
                // Refresh today first so current stats are immediately available
                if (todayEpochDay in bounds.startDay..bounds.endDay) {
                    usageStatsRepository.refreshUsageForDay(todayEpochDay)
                }
                for (day in bounds.startDay..bounds.endDay) {
                    if (day != todayEpochDay) {
                        usageStatsRepository.refreshUsageForDay(day)
                    }
                }
            } catch (_: Exception) {
                // Ignore background sync errors
            } finally {
                isRefreshing.value = false
            }
        }
    }

    private fun launchRefreshForRange(startDay: Long, endDay: Long) {
        syncJob?.cancel()
        syncJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val zoneId = ZoneId.systemDefault()
                val todayEpochDay = LocalDate.ofInstant(Instant.ofEpochMilli(clock.now()), zoneId).toEpochDay()
                if (todayEpochDay in startDay..endDay) {
                    usageStatsRepository.refreshUsageForDay(todayEpochDay)
                }
                for (day in startDay..endDay) {
                    if (day != todayEpochDay) {
                        usageStatsRepository.refreshUsageForDay(day)
                    }
                }
            } catch (_: Exception) {
                // Degrades gracefully on query or permission errors
            }
        }
    }

    private fun calculatePeriodBounds(
        range: StatsTimeRange,
        offset: Int,
        weeklyGoalHours: Int,
    ): PeriodBounds {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.ofInstant(Instant.ofEpochMilli(clock.now()), zoneId)

        return when (range) {
            StatsTimeRange.DAY -> {
                val targetDate = today.plusDays(offset.toLong())
                val startDay = targetDate.toEpochDay()
                val endDay = startDay
                val startMs = targetDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val endMs = targetDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val prevStartMs = targetDate.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val prevEndMs = startMs

                val label = if (offset == 0) {
                    "Today, ${targetDate.format(DateTimeFormatter.ofPattern("MMM d"))}"
                } else {
                    targetDate.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                }

                val dailyGoal = weeklyGoalHours / 7f
                val goalMs = (dailyGoal * MILLIS_PER_HOUR).toLong()
                val goalFormatted = formatHoursDisplay(dailyGoal)

                PeriodBounds(
                    startDay = startDay,
                    endDay = endDay,
                    startMs = startMs,
                    endMs = endMs,
                    prevStartMs = prevStartMs,
                    prevEndMs = prevEndMs,
                    label = label,
                    subtitle = "How you're doing today",
                    heroLabel = "Focused today",
                    chartTitle = "Focus by hour",
                    goalMs = goalMs,
                    goalLabel = "Daily goal: $goalFormatted",
                    dailyGoalHours = dailyGoal,
                    dailyGoalFormatted = goalFormatted,
                )
            }

            StatsTimeRange.WEEK -> {
                val currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val targetMonday = currentMonday.plusWeeks(offset.toLong())
                val targetSunday = targetMonday.plusDays(6)

                val startDay = targetMonday.toEpochDay()
                val endDay = targetSunday.toEpochDay()
                val startMs = targetMonday.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val endMs = targetSunday.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val prevStartMs = targetMonday.minusWeeks(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val prevEndMs = startMs

                val label = formatWeekRange(targetMonday, targetSunday)
                val dailyGoal = weeklyGoalHours / 7f
                val goalMs = weeklyGoalHours * MILLIS_PER_HOUR
                val dailyGoalFormatted = formatHoursDisplay(dailyGoal)

                PeriodBounds(
                    startDay = startDay,
                    endDay = endDay,
                    startMs = startMs,
                    endMs = endMs,
                    prevStartMs = prevStartMs,
                    prevEndMs = prevEndMs,
                    label = label,
                    subtitle = "How you're doing this week",
                    heroLabel = "Focused this week",
                    chartTitle = "Focus each day",
                    goalMs = goalMs,
                    goalLabel = "Weekly goal: ${weeklyGoalHours}h",
                    dailyGoalHours = dailyGoal,
                    dailyGoalFormatted = dailyGoalFormatted,
                )
            }

            StatsTimeRange.MONTH -> {
                val currentFirstOfMonth = today.withDayOfMonth(1)
                val targetFirst = currentFirstOfMonth.plusMonths(offset.toLong())
                val targetEnd = targetFirst.plusMonths(1).minusDays(1)

                val startDay = targetFirst.toEpochDay()
                val endDay = targetEnd.toEpochDay()
                val startMs = targetFirst.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val endMs = targetEnd.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val prevStartMs = targetFirst.minusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val prevEndMs = startMs

                val label = targetFirst.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                val monthlyGoalHours = (weeklyGoalHours * 4.33f).toInt().coerceAtLeast(1)
                val goalMs = monthlyGoalHours * MILLIS_PER_HOUR

                PeriodBounds(
                    startDay = startDay,
                    endDay = endDay,
                    startMs = startMs,
                    endMs = endMs,
                    prevStartMs = prevStartMs,
                    prevEndMs = prevEndMs,
                    label = label,
                    subtitle = "How you're doing this month",
                    heroLabel = "Focused this month",
                    chartTitle = "Focus each week",
                    goalMs = goalMs,
                    goalLabel = "Monthly goal: ${monthlyGoalHours}h",
                    dailyGoalHours = weeklyGoalHours.toFloat(),
                    dailyGoalFormatted = "${weeklyGoalHours}h",
                )
            }
        }
    }

    private fun formatWeekRange(monday: LocalDate, sunday: LocalDate): String {
        return if (monday.month == sunday.month) {
            val monthStr = monday.format(DateTimeFormatter.ofPattern("MMM"))
            "$monthStr ${monday.dayOfMonth} – ${sunday.dayOfMonth}"
        } else if (monday.year == sunday.year) {
            val monStr = monday.format(DateTimeFormatter.ofPattern("MMM d"))
            val sunStr = sunday.format(DateTimeFormatter.ofPattern("MMM d"))
            "$monStr – $sunStr"
        } else {
            val monStr = monday.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            val sunStr = sunday.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            "$monStr – $sunStr"
        }
    }

    private fun buildUiState(
        range: StatsTimeRange,
        offset: Int,
        bounds: PeriodBounds,
        stats: DailyUsageStats,
        dailyStatsList: List<DailyUsageStats>,
        sessions: List<FocusSession>,
        prevPeriodFocusMs: Long,
        allSessions: List<FocusSession>,
        hasPermission: Boolean,
        isLoading: Boolean,
    ): StatsUiState {
        val dailyStatsByEpochDay = dailyStatsList.associateBy { it.epochDay }
        val totalMs = stats.totalScreenTimeMs
        val topList = stats.topApps.take(TOP_APPS_LIMIT).map { summary ->
            val fraction = if (totalMs > 0L) {
                (summary.totalTimeForegroundMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
            AppUsageItem(
                packageName = summary.packageName,
                appName = summary.appName,
                formattedDuration = formatDuration(summary.totalTimeForegroundMs),
                launchCount = summary.launchCount,
                fractionOfTotal = fraction,
                isSafeListed = summary.isSafeListed,
            )
        }

        // Compute session-based focus time or fallback to stats.totalFocusTimeMs
        val sessionFocusMs = sessions.sumOf {
            it.elapsedDurationMs(it.completedAtEpochMs ?: it.startedAtEpochMs)
        }
        val effectiveFocusMs = maxOf(stats.totalFocusTimeMs, sessionFocusMs)

        // Goal calculations
        val goalMs = bounds.goalMs
        val goalProgress = if (goalMs > 0L) {
            (effectiveFocusMs.toFloat() / goalMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        val remainingMs = (goalMs - effectiveFocusMs).coerceAtLeast(0L)
        val goalRemainingText = if (remainingMs > 0L) {
            "${formatDuration(remainingMs)} to go"
        } else {
            "Goal reached! 🎉"
        }

        // Trend calculation vs previous period
        val periodName = when (range) {
            StatsTimeRange.DAY -> "day"
            StatsTimeRange.WEEK -> "week"
            StatsTimeRange.MONTH -> "month"
        }
        val (trendText, isTrendPositive) = calculateTrend(effectiveFocusMs, prevPeriodFocusMs, periodName)

        // Generate chart bars from real daily usage + sessions
        val chartBars = generateChartBars(
            range = range,
            bounds = bounds,
            sessions = sessions,
            dailyStatsByEpochDay = dailyStatsByEpochDay,
            fallbackFocusMs = effectiveFocusMs,
        )
        val dailyHoursList = chartBars.map { it.hours }

        // Highlights calculation
        val highlights = calculateHighlights(
            range = range,
            sessions = sessions,
            dailyStatsByEpochDay = dailyStatsByEpochDay,
            allSessions = allSessions,
            dailyGoalHours = bounds.dailyGoalHours,
        )

        return StatsUiState(
            selectedRange = range,
            periodOffset = offset,
            dateRangeLabel = bounds.label,
            canNavigateNext = offset < 0,
            focusHeroLabel = bounds.heroLabel,
            totalFocusTimeFormatted = formatDuration(effectiveFocusMs),
            subtitleText = bounds.subtitle,
            trendText = trendText,
            isTrendPositive = isTrendPositive,
            goalLabel = bounds.goalLabel,
            goalRemainingText = goalRemainingText,
            goalProgress = goalProgress,
            chartTitle = bounds.chartTitle,
            chartBars = chartBars,
            dailyGoalHours = bounds.dailyGoalHours,
            dailyGoalFormatted = bounds.dailyGoalFormatted,
            highlights = highlights,
            totalScreenTimeFormatted = formatDuration(stats.totalScreenTimeMs),
            focusRatio = stats.focusRatio,
            unlockCount = stats.unlockCount,
            topApps = topList,
            dailyHours = dailyHoursList,
            hasUsagePermission = hasPermission,
            isLoading = isLoading,
        )
    }

    private fun calculateTrend(
        currentMs: Long,
        prevMs: Long,
        periodName: String,
    ): Pair<String, Boolean> {
        return when {
            prevMs > 0L && currentMs > 0L -> {
                val diff = currentMs - prevMs
                val pct = ((diff.toDouble() / prevMs.toDouble()) * 100).toInt()
                if (pct >= 0) {
                    "$pct% more than last $periodName. Great progress!" to true
                } else {
                    "${-pct}% less than last $periodName." to false
                }
            }
            currentMs > 0L -> {
                "Great start! Keep the momentum going." to true
            }
            else -> {
                "Start a session to build your focus momentum." to true
            }
        }
    }

    private fun generateChartBars(
        range: StatsTimeRange,
        bounds: PeriodBounds,
        sessions: List<FocusSession>,
        dailyStatsByEpochDay: Map<Long, DailyUsageStats>,
        fallbackFocusMs: Long,
    ): List<ChartBarItem> {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.ofInstant(Instant.ofEpochMilli(clock.now()), zoneId)

        return when (range) {
            StatsTimeRange.WEEK -> {
                val monday = LocalDate.ofInstant(Instant.ofEpochMilli(bounds.startMs), zoneId)
                (0..6).map { dayIndex ->
                    val date = monday.plusDays(dayIndex.toLong())
                    val epochDay = date.toEpochDay()
                    val dayStartMs = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val dayEndMs = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

                    val daySessions = sessions.filter {
                        it.startedAtEpochMs in dayStartMs until dayEndMs
                    }
                    var dayMs = daySessions.sumOf {
                        it.elapsedDurationMs(it.completedAtEpochMs ?: it.startedAtEpochMs)
                    }

                    val dayStats = dailyStatsByEpochDay[epochDay]
                    if (dayStats != null && dayStats.totalFocusTimeMs > dayMs) {
                        dayMs = dayStats.totalFocusTimeMs
                    }

                    if (dayMs == 0L && sessions.isEmpty() && date == today && fallbackFocusMs > 0L) {
                        dayMs = fallbackFocusMs
                    }

                    val hours = (dayMs.toFloat() / MILLIS_PER_HOUR.toFloat())
                    val isGoalReached = hours >= bounds.dailyGoalHours
                    val formatted = formatHoursDisplay(hours)

                    ChartBarItem(
                        id = "week_day_$dayIndex",
                        label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US),
                        fullLabel = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                        hours = hours,
                        formattedDuration = formatted,
                        isGoalReached = isGoalReached,
                        isCurrent = (date == today),
                    )
                }
            }

            StatsTimeRange.DAY -> {
                val targetDate = LocalDate.ofInstant(Instant.ofEpochMilli(bounds.startMs), zoneId)
                val hourBlocks = listOf(
                    0 to "12 AM", 4 to "4 AM", 8 to "8 AM",
                    12 to "12 PM", 16 to "4 PM", 20 to "8 PM"
                )
                val targetEpochDay = targetDate.toEpochDay()
                val dayStats = dailyStatsByEpochDay[targetEpochDay]

                hourBlocks.mapIndexed { index, (hour, label) ->
                    val blockStart = targetDate.atTime(hour, 0).atZone(zoneId).toInstant().toEpochMilli()
                    val blockEnd = targetDate.atTime(hour + 3, 59, 59).atZone(zoneId).toInstant().toEpochMilli()

                    val blockSessions = sessions.filter {
                        it.startedAtEpochMs in blockStart..blockEnd
                    }
                    var blockMs = blockSessions.sumOf {
                        it.elapsedDurationMs(it.completedAtEpochMs ?: it.startedAtEpochMs)
                    }

                    if (blockMs == 0L && sessions.isEmpty() && targetDate == today && fallbackFocusMs > 0L && index == 3) {
                        blockMs = fallbackFocusMs
                    }

                    val hours = (blockMs.toFloat() / MILLIS_PER_HOUR.toFloat())

                    ChartBarItem(
                        id = "day_block_$index",
                        label = label,
                        fullLabel = label,
                        hours = hours,
                        formattedDuration = formatHoursDisplay(hours),
                        isGoalReached = hours >= (bounds.dailyGoalHours / 4f),
                        isCurrent = (targetDate == today && LocalTime.ofInstant(Instant.ofEpochMilli(clock.now()), zoneId).hour in hour until (hour + 4)),
                    )
                }
            }

            StatsTimeRange.MONTH -> {
                val firstOfMonth = LocalDate.ofInstant(Instant.ofEpochMilli(bounds.startMs), zoneId)
                val daysInMonth = firstOfMonth.lengthOfMonth()
                val numWeeks = ((daysInMonth + 6) / 7).coerceAtMost(5)

                (0 until numWeeks).map { weekIndex ->
                    val weekStart = firstOfMonth.plusDays((weekIndex * 7).toLong())
                    val weekEnd = minOf(weekStart.plusDays(6), firstOfMonth.withDayOfMonth(daysInMonth))
                    val startMs = weekStart.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val endMs = weekEnd.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

                    val weekSessions = sessions.filter { it.startedAtEpochMs in startMs until endMs }
                    val weekSessionMs = weekSessions.sumOf {
                        it.elapsedDurationMs(it.completedAtEpochMs ?: it.startedAtEpochMs)
                    }
                    val weekUsageMs = (weekStart.toEpochDay()..weekEnd.toEpochDay()).sumOf { epochDay ->
                        dailyStatsByEpochDay[epochDay]?.totalFocusTimeMs ?: 0L
                    }
                    val weekMs = maxOf(weekSessionMs, weekUsageMs)
                    val hours = (weekMs.toFloat() / MILLIS_PER_HOUR.toFloat())

                    ChartBarItem(
                        id = "month_week_$weekIndex",
                        label = "W${weekIndex + 1}",
                        fullLabel = "Week ${weekIndex + 1} (${weekStart.format(DateTimeFormatter.ofPattern("MMM d"))}–${weekEnd.format(DateTimeFormatter.ofPattern("d"))})",
                        hours = hours,
                        formattedDuration = formatHoursDisplay(hours),
                        isGoalReached = hours >= bounds.dailyGoalHours,
                        isCurrent = (today in weekStart..weekEnd),
                    )
                }
            }
        }
    }

    private fun calculateHighlights(
        range: StatsTimeRange,
        sessions: List<FocusSession>,
        dailyStatsByEpochDay: Map<Long, DailyUsageStats>,
        allSessions: List<FocusSession>,
        dailyGoalHours: Float,
    ): StatsHighlights {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.ofInstant(Instant.ofEpochMilli(clock.now()), zoneId)

        // 1. Best Day (Day of week with maximum focus duration)
        val durationByDayOfWeek = mutableMapOf<DayOfWeek, Long>()
        for (session in sessions) {
            val dow = LocalDate.ofInstant(Instant.ofEpochMilli(session.startedAtEpochMs), zoneId).dayOfWeek
            val duration = session.elapsedDurationMs(session.completedAtEpochMs ?: session.startedAtEpochMs)
            durationByDayOfWeek[dow] = (durationByDayOfWeek[dow] ?: 0L) + duration
        }
        for ((epochDay, stats) in dailyStatsByEpochDay) {
            val dow = LocalDate.ofEpochDay(epochDay).dayOfWeek
            val current = durationByDayOfWeek[dow] ?: 0L
            if (stats.totalFocusTimeMs > current) {
                durationByDayOfWeek[dow] = stats.totalFocusTimeMs
            }
        }

        val bestDayEntry = durationByDayOfWeek.filter { it.value > 0L }.maxByOrNull { it.value }
        val bestDayName = bestDayEntry?.key?.getDisplayName(TextStyle.FULL, Locale.US) ?: "—"
        val bestDayCaption = if (bestDayEntry != null && bestDayEntry.value > 0L) {
            "Best day · ${formatDuration(bestDayEntry.value)}"
        } else {
            "Complete sessions to see"
        }

        // 2. Best Time to Focus (2-hour bucket with highest focus volume)
        val hourBucketDurations = LongArray(12) // 12 buckets of 2 hours
        for (session in sessions) {
            val localTime = Instant.ofEpochMilli(session.startedAtEpochMs).atZone(zoneId).toLocalTime()
            val bucket = (localTime.hour / 2).coerceIn(0, 11)
            hourBucketDurations[bucket] += session.elapsedDurationMs(session.completedAtEpochMs ?: session.startedAtEpochMs)
        }
        val hasSessionDuration = hourBucketDurations.any { it > 0L }
        val bestTimeWindow = if (hasSessionDuration) {
            val bestBucket = hourBucketDurations.indices.maxByOrNull { hourBucketDurations[it] } ?: 4
            val startHour = bestBucket * 2
            val endHour = startHour + 2
            formatHourWindow(startHour, endHour)
        } else {
            "—"
        }
        val bestTimeCaption = if (hasSessionDuration) "Best time to focus" else "Start sessions to see"

        // 3. Avg Session Length
        val totalSessionMs = sessions.sumOf { it.elapsedDurationMs(it.completedAtEpochMs ?: it.startedAtEpochMs) }
        val avgMs = if (sessions.isNotEmpty()) totalSessionMs / sessions.size else 0L
        val avgSessionLength = if (avgMs > 0L) formatDuration(avgMs) else "—"
        val avgSessionCaption = if (sessions.isNotEmpty()) {
            "${sessions.size} session${if (sessions.size == 1) "" else "s"} completed"
        } else {
            "Avg session length"
        }

        // 4. Goal days met in current period & active streak
        val dailyTotals = mutableMapOf<LocalDate, Long>()
        for (session in sessions) {
            val d = LocalDate.ofInstant(Instant.ofEpochMilli(session.startedAtEpochMs), zoneId)
            dailyTotals[d] = (dailyTotals[d] ?: 0L) + session.elapsedDurationMs(session.completedAtEpochMs ?: session.startedAtEpochMs)
        }
        for ((epochDay, stats) in dailyStatsByEpochDay) {
            val d = LocalDate.ofEpochDay(epochDay)
            dailyTotals[d] = maxOf(dailyTotals[d] ?: 0L, stats.totalFocusTimeMs)
        }

        val dailyGoalMs = (dailyGoalHours * MILLIS_PER_HOUR).toLong()
        val goalDaysCount = dailyTotals.count { it.value >= dailyGoalMs }

        // Compute active streak from all historical sessions
        val allSessionDates = allSessions.map {
            LocalDate.ofInstant(Instant.ofEpochMilli(it.startedAtEpochMs), zoneId)
        }.toSet()
        var streak = 0
        var checkDate = if (allSessionDates.contains(today)) {
            today
        } else if (allSessionDates.contains(today.minusDays(1))) {
            today.minusDays(1)
        } else {
            null
        }
        while (checkDate != null && allSessionDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        val streakCount = if (goalDaysCount > 0) {
            "$goalDaysCount days"
        } else if (streak > 0) {
            "$streak days"
        } else {
            "0 days"
        }
        val streakCaption = if (goalDaysCount > 0) "Goal days met" else if (streak > 0) "Active streak" else "Goal days met"

        return StatsHighlights(
            bestDayName = bestDayName,
            bestDayCaption = bestDayCaption,
            bestTimeWindow = bestTimeWindow,
            bestTimeCaption = bestTimeCaption,
            avgSessionLength = avgSessionLength,
            avgSessionCaption = avgSessionCaption,
            streakCount = streakCount,
            streakCaption = streakCaption,
        )
    }

    private fun formatHourWindow(start: Int, end: Int): String {
        fun formatHour(h: Int): String {
            val mod = h % 24
            val displayH = if (mod == 0) 12 else if (mod > 12) mod - 12 else mod
            val amPm = if (mod < 12) "AM" else "PM"
            return "$displayH $amPm"
        }
        val startH = if (start % 12 == 0) 12 else start % 12
        val endFormatted = formatHour(end)
        return "$startH–$endFormatted"
    }

    private fun formatHoursDisplay(hours: Float): String {
        return when {
            hours <= 0f -> "0h"
            hours < 1f -> {
                val mins = (hours * 60).toInt().coerceAtLeast(1)
                "${mins}m"
            }
            else -> {
                val rounded = String.format(Locale.US, "%.1f", hours).replace(".0", "")
                "${rounded}h"
            }
        }
    }

    private fun formatDuration(durationMs: Long): String {
        val totalMinutes = durationMs / MILLIS_PER_MINUTE
        val hours = totalMinutes / MINUTES_PER_HOUR
        val minutes = totalMinutes % MINUTES_PER_HOUR
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }
}

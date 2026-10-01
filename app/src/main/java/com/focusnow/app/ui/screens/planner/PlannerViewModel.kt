package com.focusnow.app.ui.screens.planner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.model.CollegeClass
import com.focusnow.app.data.model.DailyScheduleRoutine
import com.focusnow.app.data.model.RoutineCategory
import com.focusnow.app.data.repository.ScheduleConflict
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

enum class PlannerTab(val title: String) {
    TIMETABLE("College Timetable"),
    DAILY_ROUTINE("Daily Routine")
}

data class PlannerUiState(
    val selectedTab: PlannerTab = PlannerTab.TIMETABLE,
    val selectedDayOfWeek: DayOfWeek = LocalDate.now().dayOfWeek,
    val classesForSelectedDay: List<CollegeClass> = emptyList(),
    val allClasses: List<CollegeClass> = emptyList(),
    val dailyRoutines: List<DailyScheduleRoutine> = emptyList(),
    val conflicts: List<ScheduleConflict> = emptyList(),
    val isAddClassDialogOpen: Boolean = false,
    val isAddRoutineDialogOpen: Boolean = false,
    val editingClass: CollegeClass? = null,
    val editingRoutine: DailyScheduleRoutine? = null
)

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                app.collegeScheduleRepository.allClasses.collect { classes ->
                    _uiState.value = _uiState.value.copy(
                        allClasses = classes,
                        classesForSelectedDay = classes.filter { it.dayOfWeek == _uiState.value.selectedDayOfWeek }
                            .sortedBy { it.startTimeStr }
                    )
                }
            }

            launch {
                app.dailyRoutineRepository.allRoutines.collect { routines ->
                    val conflicts = app.dailyRoutineRepository.detectConflicts(routines)
                    _uiState.value = _uiState.value.copy(
                        dailyRoutines = routines,
                        conflicts = conflicts
                    )
                }
            }
        }
    }

    fun selectTab(tab: PlannerTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectDayOfWeek(dayOfWeek: DayOfWeek) {
        _uiState.value = _uiState.value.copy(
            selectedDayOfWeek = dayOfWeek,
            classesForSelectedDay = _uiState.value.allClasses.filter { it.dayOfWeek == dayOfWeek }
                .sortedBy { it.startTimeStr }
        )
    }

    fun openAddClassDialog() {
        _uiState.value = _uiState.value.copy(isAddClassDialogOpen = true, editingClass = null)
    }

    fun openEditClassDialog(cls: CollegeClass) {
        _uiState.value = _uiState.value.copy(isAddClassDialogOpen = true, editingClass = cls)
    }

    fun closeAddClassDialog() {
        _uiState.value = _uiState.value.copy(isAddClassDialogOpen = false, editingClass = null)
    }

    fun saveClass(
        subject: String,
        faculty: String,
        room: String,
        dayOfWeek: DayOfWeek,
        startTimeStr: String,
        endTimeStr: String,
        notes: String,
        isRecurring: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val editing = _uiState.value.editingClass
            if (editing != null) {
                val updated = editing.copy(
                    subject = subject,
                    faculty = faculty,
                    room = room,
                    dayOfWeek = dayOfWeek,
                    startTimeStr = startTimeStr,
                    endTimeStr = endTimeStr,
                    notes = notes,
                    isRecurring = isRecurring
                )
                app.collegeScheduleRepository.updateClass(updated)
            } else {
                val newClass = CollegeClass(
                    subject = subject,
                    faculty = faculty,
                    room = room,
                    dayOfWeek = dayOfWeek,
                    startTimeStr = startTimeStr,
                    endTimeStr = endTimeStr,
                    notes = notes,
                    isRecurring = isRecurring
                )
                app.collegeScheduleRepository.insertClass(newClass)
            }
            closeAddClassDialog()
        }
    }

    fun deleteClass(cls: CollegeClass) {
        viewModelScope.launch(Dispatchers.IO) {
            app.collegeScheduleRepository.deleteClass(cls)
        }
    }

    fun openAddRoutineDialog() {
        _uiState.value = _uiState.value.copy(isAddRoutineDialogOpen = true, editingRoutine = null)
    }

    fun closeAddRoutineDialog() {
        _uiState.value = _uiState.value.copy(isAddRoutineDialogOpen = false, editingRoutine = null)
    }

    fun saveRoutine(
        title: String,
        startTimeStr: String,
        endTimeStr: String,
        category: RoutineCategory
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newRoutine = DailyScheduleRoutine(
                title = title,
                startTimeStr = startTimeStr,
                endTimeStr = endTimeStr,
                category = category,
                isEnabled = true
            )
            app.dailyRoutineRepository.insertRoutine(newRoutine)
            closeAddRoutineDialog()
        }
    }

    fun deleteRoutine(routine: DailyScheduleRoutine) {
        viewModelScope.launch(Dispatchers.IO) {
            app.dailyRoutineRepository.deleteRoutine(routine)
        }
    }

    fun initializeSampleRoutine() {
        viewModelScope.launch(Dispatchers.IO) {
            val sampleRoutines = listOf(
                DailyScheduleRoutine(title = "Wake up", startTimeStr = "06:00", endTimeStr = "06:30", category = RoutineCategory.WAKE_UP),
                DailyScheduleRoutine(title = "Exercise", startTimeStr = "06:30", endTimeStr = "07:00", category = RoutineCategory.EXERCISE),
                DailyScheduleRoutine(title = "Morning Deep Study", startTimeStr = "07:00", endTimeStr = "08:30", category = RoutineCategory.STUDY),
                DailyScheduleRoutine(title = "College Hours", startTimeStr = "08:30", endTimeStr = "16:30", category = RoutineCategory.COLLEGE),
                DailyScheduleRoutine(title = "Return Home & Break", startTimeStr = "16:30", endTimeStr = "17:30", category = RoutineCategory.REST),
                DailyScheduleRoutine(title = "Coding Sprint", startTimeStr = "17:30", endTimeStr = "19:00", category = RoutineCategory.CODING),
                DailyScheduleRoutine(title = "Dinner", startTimeStr = "19:00", endTimeStr = "20:00", category = RoutineCategory.DINNER),
                DailyScheduleRoutine(title = "GATE Prep / Problem Solving", startTimeStr = "20:00", endTimeStr = "21:30", category = RoutineCategory.GATE_PREP),
                DailyScheduleRoutine(title = "Daily Revision", startTimeStr = "21:30", endTimeStr = "22:30", category = RoutineCategory.REVISION),
                DailyScheduleRoutine(title = "Sleep", startTimeStr = "22:30", endTimeStr = "06:00", category = RoutineCategory.SLEEP)
            )
            app.database.dailyRoutineDao().insertAllRoutines(sampleRoutines)
        }
    }
}

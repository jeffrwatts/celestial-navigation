package com.jeffrwatts.celestialnavigation.sights

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeffrwatts.celestialnavigation.ADD_EDIT_RESULT_OK
import com.jeffrwatts.celestialnavigation.DELETE_RESULT_OK
import com.jeffrwatts.celestialnavigation.EDIT_RESULT_OK
import com.jeffrwatts.celestialnavigation.data.Sight
import com.jeffrwatts.celestialnavigation.data.source.SightsRepository
import com.jeffrwatts.celestialnavigation.utils.Async
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.utils.WhileUiSubscribed

/**
 * UiState for the task list screen.
 */
data class SightsUiState(
    val items: List<Sight> = emptyList(),
    val isLoading: Boolean = false,
    val filteringUiInfo: FilteringUiInfo = FilteringUiInfo(),
    val userMessage: Int? = null
)

/**
 * ViewModel for the task list screen.
 */
@HiltViewModel
class SightsViewModel @Inject constructor(
    private val sightsRepository: SightsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _savedFilterType =
        savedStateHandle.getStateFlow(SIGHTS_FILTER_SAVED_STATE_KEY, SightsFilterType.ALL_SIGHTS)

    private val _filterUiInfo = _savedFilterType.map { getFilterUiInfo(it) }.distinctUntilChanged()
    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)
    private val _filteredTasksAsync =
        combine(sightsRepository.getSightsStream(), _savedFilterType) { sights, type ->
            filterSights(sights, type)
        }
            .map { Async.Success(it) }
            .onStart<Async<List<Sight>>> { emit(Async.Loading) }

    val uiState: StateFlow<SightsUiState> = combine(
        _filterUiInfo, _isLoading, _userMessage, _filteredTasksAsync
    ) { filterUiInfo, isLoading, userMessage, sightsAsync ->
        when (sightsAsync) {
            Async.Loading -> {
                SightsUiState(isLoading = true)
            }
            is Async.Success -> {
                SightsUiState(
                    items = sightsAsync.data,
                    filteringUiInfo = filterUiInfo,
                    isLoading = isLoading,
                    userMessage = userMessage
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = SightsUiState(isLoading = true)
        )

    fun setFiltering(requestType: SightsFilterType) {
        savedStateHandle[SIGHTS_FILTER_SAVED_STATE_KEY] = requestType
    }

    //fun clearCompletedTasks() {
    //    viewModelScope.launch {
    //        tasksRepository.clearCompletedTasks()
    //        showSnackbarMessage(R.string.completed_tasks_cleared)
    //        refresh()
    //    }
    //}

    //fun completeTask(task: Task, completed: Boolean) = viewModelScope.launch {
    //    if (completed) {
    //        tasksRepository.completeTask(task)
    //        showSnackbarMessage(R.string.task_marked_complete)
    //    } else {
    //        tasksRepository.activateTask(task)
    //        showSnackbarMessage(R.string.task_marked_active)
    //    }
    //}

    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_sight_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_sight_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_sight_message)
        }
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

    //fun refresh() {
    //    _isLoading.value = true
    //    viewModelScope.launch {
    //        tasksRepository.refreshTasks()
    //        _isLoading.value = false
    //    }
    //}

    private fun filterSights(
        sightsResult: Result<List<Sight>>,
        filteringType: SightsFilterType
    ): List<Sight> = if (sightsResult is Result.Success) {
        filterItems(sightsResult.data, filteringType)
    } else {
        showSnackbarMessage(R.string.loading_sights_error)
        emptyList()
    }

    private fun filterItems(tasks: List<Sight>, filteringType: SightsFilterType): List<Sight> {
        val tasksToShow = ArrayList<Sight>()
        // We filter the tasks based on the requestType
        for (task in tasks) {
            when (filteringType) {
                SightsFilterType.ALL_SIGHTS -> tasksToShow.add(task)
                //ACTIVE_TASKS -> if (task.isActive) {
                //    tasksToShow.add(task)
                //}
                //COMPLETED_TASKS -> if (task.isCompleted) {
                //    tasksToShow.add(task)
                //}
            }
        }
        return tasksToShow
    }

    private fun getFilterUiInfo(requestType: SightsFilterType): FilteringUiInfo =
        when (requestType) {
            SightsFilterType.ALL_SIGHTS -> {
                FilteringUiInfo(
                    R.string.label_all, R.string.no_sights_all,
                    //R.drawable.logo_no_fill
                )
            }
            //ACTIVE_TASKS -> {
            //    FilteringUiInfo(
            //        R.string.label_active, R.string.no_tasks_active,
            //        R.drawable.ic_check_circle_96dp
            //    )
            //}
            //COMPLETED_TASKS -> {
            //    FilteringUiInfo(
            //        R.string.label_completed, R.string.no_tasks_completed,
            //        R.drawable.ic_verified_user_96dp
            //    )
            //}
        }
}

// Used to save the current filtering in SavedStateHandle.
const val SIGHTS_FILTER_SAVED_STATE_KEY = "SIGHTS_FILTER_SAVED_STATE_KEY"

data class FilteringUiInfo(
    val currentFilteringLabel: Int = R.string.label_all,
    val noTasksLabel: Int = R.string.no_sights_all,
    //val noTaskIconRes: Int = R.drawable.logo_no_fill,
)

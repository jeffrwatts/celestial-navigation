package com.jeffrwatts.celestialnavigation.sights

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeffrwatts.celestialnavigation.ADD_EDIT_RESULT_OK
import com.jeffrwatts.celestialnavigation.DELETE_RESULT_OK
import com.jeffrwatts.celestialnavigation.EDIT_RESULT_OK
import com.jeffrwatts.celestialnavigation.data.Sight
import com.jeffrwatts.celestialnavigation.data.source.SightsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.utils.Async
import com.jeffrwatts.celestialnavigation.utils.WhileUiSubscribed
import kotlinx.coroutines.flow.*

/**
 * UiState for the sight list screen.
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
    private val _filteredSightsAsync =
        combine(sightsRepository.getSightsStream(), _savedFilterType) { sights, type ->
            filterSights(sights, type)
        }
            .map { Async.Success(it) }
            .onStart<Async<List<Sight>>> { emit(Async.Loading) }

    val uiState: StateFlow<SightsUiState> = combine(
        _filterUiInfo, _isLoading, _userMessage, _filteredSightsAsync
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

    fun activateSight(sight: Sight, activated: Boolean) = viewModelScope.launch {
        sightsRepository.activateSight(sight, activated)
        if (activated) {
            showSnackbarMessage(R.string.sight_marked_active)
        } else {
            showSnackbarMessage(R.string.sight_marked_inactive)
        }
    }

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

    private fun filterSights(
        sightsResult: Result<List<Sight>>,
        filteringType: SightsFilterType
    ): List<Sight> = if (sightsResult is Result.Success) {
        filterItems(sightsResult.data, filteringType)
    } else {
        showSnackbarMessage(R.string.loading_sights_error)
        emptyList()
    }

    private fun filterItems(sights: List<Sight>, filteringType: SightsFilterType): List<Sight> {
        val sightsToShow = ArrayList<Sight>()
        // We filter the tasks based on the requestType
        for (sight in sights) {
            when (filteringType) {
                SightsFilterType.ALL_SIGHTS -> sightsToShow.add(sight)
                SightsFilterType.ACTIVE_SIGHTS -> if (sight.isActive) {
                    sightsToShow.add(sight)
                }
            }
        }
        return sightsToShow
    }

    private fun getFilterUiInfo(requestType: SightsFilterType): FilteringUiInfo =
        when (requestType) {
            SightsFilterType.ALL_SIGHTS -> {
                FilteringUiInfo(
                    R.string.label_all, R.string.no_sights_all,
                    R.drawable.logo_no_fill
                )
            }
            SightsFilterType.ACTIVE_SIGHTS -> {
                FilteringUiInfo(
                    R.string.label_active, R.string.no_sights_active,
                    R.drawable.ic_check_circle_96dp
                )
            }
        }
}

// Used to save the current filtering in SavedStateHandle.
const val SIGHTS_FILTER_SAVED_STATE_KEY = "SIGHTS_FILTER_SAVED_STATE_KEY"

data class FilteringUiInfo(
    val currentFilteringLabel: Int = R.string.label_all,
    val noTasksLabel: Int = R.string.no_sights_all,
    val noTaskIconRes: Int = R.drawable.logo_no_fill,
)
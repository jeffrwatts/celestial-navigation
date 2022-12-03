package com.jeffrwatts.celestialnavigation.sights

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
    val userMessage: Int? = null
)

/**
 * ViewModel for the task list screen.
 */
@HiltViewModel
class SightsViewModel @Inject constructor(
    private val sightsRepository: SightsRepository) : ViewModel() {

    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)
    private val _sightsAsync = sightsRepository.getSightsStream()
            .map { Async.Success(if (it is Result.Success ) it.data else emptyList()) }
            .onStart<Async<List<Sight>>> { emit(Async.Loading) }

    val uiState: StateFlow<SightsUiState> = combine(
        _isLoading, _userMessage, _sightsAsync
    ) { isLoading, userMessage, sightsAsync ->
        when (sightsAsync) {
            Async.Loading -> {
                SightsUiState(isLoading = true)
            }
            is Async.Success -> {
                SightsUiState(
                    items = sightsAsync.data,
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
}

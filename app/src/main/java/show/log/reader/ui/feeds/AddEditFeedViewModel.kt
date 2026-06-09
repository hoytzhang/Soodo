package show.log.reader.ui.feeds

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import show.log.reader.data.repository.FeedRepository
import javax.inject.Inject

data class AddEditUiState(
    val name: String = "",
    val url: String = "",
    val category: String = "",
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class AddEditFeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val feedRepository: FeedRepository,
) : ViewModel() {

    private val feedId: Long? = savedStateHandle.get<Long>("feedId")

    private val _state = MutableStateFlow(AddEditUiState())
    val state: StateFlow<AddEditUiState> = _state.asStateFlow()

    init {
        if (feedId != null && feedId > 0) {
            loadFeed(feedId)
        }
    }

    private fun loadFeed(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val feed = feedRepository.getById(id)
            if (feed != null) {
                _state.value = _state.value.copy(
                    name = feed.title,
                    url = feed.url,
                    category = feed.category ?: "",
                    isEditMode = true,
                    isLoading = false,
                )
            } else {
                _state.value = _state.value.copy(
                    error = "Feed not found",
                    isLoading = false,
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name, error = null)
    }

    fun onUrlChange(url: String) {
        _state.value = _state.value.copy(url = url, error = null)
    }

    fun onCategoryChange(category: String) {
        _state.value = _state.value.copy(category = category, error = null)
    }

    fun save() {
        val current = _state.value
        if (current.isSaving) return

        val url = current.url.trim()
        if (url.isBlank()) {
            _state.value = current.copy(error = "RSS URL is required")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            try {
                if (current.isEditMode) {
                    val feed = feedRepository.getById(feedId!!)
                    if (feed != null) {
                        val updated = feed.copy(
                            title = current.name.trim().ifBlank { feed.title },
                            category = current.category.trim().ifBlank { null },
                        )
                        val updateResult = feedRepository.updateFeed(updated)
                        if (updateResult.isFailure) {
                            _state.value = _state.value.copy(
                                isSaving = false,
                                error = updateResult.exceptionOrNull()?.message ?: "Failed to update feed",
                            )
                            return@launch
                        }
                    }
                } else {
                    val result = feedRepository.addFeed(url)
                    if (result.isFailure) {
                        _state.value = _state.value.copy(
                            isSaving = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to add feed",
                        )
                        return@launch
                    }
                    val feed = result.getOrThrow()
                    val name = current.name.trim()
                    val category = current.category.trim()
                    if (name.isNotBlank() || category.isNotBlank()) {
                        val updateResult = feedRepository.updateFeed(
                            feed.copy(
                                title = name.ifBlank { feed.title },
                                category = category.ifBlank { null },
                            )
                        )
                        if (updateResult.isFailure) {
                            _state.value = _state.value.copy(
                                isSaving = false,
                                error = updateResult.exceptionOrNull()?.message ?: "Failed to update feed",
                            )
                            return@launch
                        }
                    }
                }
                _state.value = _state.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = e.message ?: "Unexpected error",
                )
            }
        }
    }
}

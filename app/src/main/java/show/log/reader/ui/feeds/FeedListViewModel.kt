package show.log.reader.ui.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import show.log.reader.data.repository.FeedRepository
import show.log.reader.domain.model.Feed
import javax.inject.Inject

@HiltViewModel
class FeedListViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
) : ViewModel() {

    val feeds: StateFlow<List<Feed>> = feedRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshingFeedIds = MutableStateFlow<Set<Long>>(emptySet())
    val refreshingFeedIds: StateFlow<Set<Long>> = _refreshingFeedIds.asStateFlow()

    fun deleteFeed(feed: Feed) {
        viewModelScope.launch {
            feedRepository.deleteFeed(feed.id)
        }
    }

    fun refreshFeed(feedId: Long) {
        viewModelScope.launch {
            _refreshingFeedIds.value = _refreshingFeedIds.value + feedId
            try {
                feedRepository.refreshFeed(feedId)
            } catch (_: Exception) {
            } finally {
                _refreshingFeedIds.value = _refreshingFeedIds.value - feedId
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                feedRepository.refreshAll()
            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

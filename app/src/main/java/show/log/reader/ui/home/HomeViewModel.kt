package show.log.reader.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import show.log.reader.data.db.ArticleWithFeed
import show.log.reader.data.repository.ArticleRepository
import show.log.reader.data.repository.FeedRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val feedRepository: FeedRepository,
) : ViewModel() {

    private val _articles = MutableStateFlow<List<ArticleWithFeed>>(emptyList())
    val articles: StateFlow<List<ArticleWithFeed>> = _articles.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadArticles()
    }

    private fun loadArticles() {
        viewModelScope.launch {
            articleRepository.observeAllWithFeedTitle().collect { list ->
                Log.d("RSS_QUERY", "[HomeViewModel] observeAllWithFeedTitle emitted count=${list.size}")
                _articles.value = list
            }
        }
    }

    fun refresh() {
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

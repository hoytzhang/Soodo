package show.log.reader.ui.articledetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import show.log.reader.data.db.ArticleWithFeed
import show.log.reader.data.parser.ReadabilityParser
import show.log.reader.data.parser.ReadabilityResult
import show.log.reader.data.repository.ArticleRepository
import show.log.reader.data.repository.FeedRepository
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val feedRepository: FeedRepository,
    private val readabilityParser: ReadabilityParser,
) : ViewModel() {

    private val articleId: Long = savedStateHandle["articleId"] ?: -1L

    private val _article = MutableStateFlow<ArticleWithFeed?>(null)
    val article: StateFlow<ArticleWithFeed?> = _article.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isReadingMode = MutableStateFlow(false)
    val isReadingMode: StateFlow<Boolean> = _isReadingMode.asStateFlow()

    private val _readingContent = MutableStateFlow<ReadabilityResult?>(null)
    val readingContent: StateFlow<ReadabilityResult?> = _readingContent.asStateFlow()

    private val _isLoadingReadingMode = MutableStateFlow(false)
    val isLoadingReadingMode: StateFlow<Boolean> = _isLoadingReadingMode.asStateFlow()

    init {
        loadArticle()
        observeBookmark()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            articleRepository.observeWithFeedById(articleId).collect { article ->
                _article.value = article
                if (article != null && !article.is_read) {
                    articleRepository.markAsRead(article.id)
                }
            }
        }
    }

    private fun observeBookmark() {
        viewModelScope.launch {
            articleRepository.observeIsFavorite(articleId).collect { bookmarked ->
                _isBookmarked.value = bookmarked
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            val newValue = !_isBookmarked.value
            articleRepository.toggleBookmark(articleId, newValue)
        }
    }

    fun refresh() {
        val feedId = _article.value?.feed_id ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                feedRepository.refreshFeed(feedId)
            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleReadingMode() {
        if (_isReadingMode.value) {
            _isReadingMode.value = false
            _readingContent.value = null
            return
        }

        val link = _article.value?.link ?: return
        _isLoadingReadingMode.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = readabilityParser.parse(link)
                if (result != null) {
                    _readingContent.value = result
                    _isReadingMode.value = true
                }
            } catch (_: Exception) {
            } finally {
                _isLoadingReadingMode.value = false
            }
        }
    }
}

package show.log.reader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import show.log.reader.data.db.ArticleWithFeed
import show.log.reader.data.repository.ArticleRepository
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private val _articles = MutableStateFlow<List<ArticleWithFeed>>(emptyList())
    val articles: StateFlow<List<ArticleWithFeed>> = _articles.asStateFlow()

    init {
        viewModelScope.launch {
            articleRepository.observeBookmarkedWithFeedTitle().collect { list ->
                _articles.value = list
            }
        }
    }

    fun markAsRead(articleId: Long) {
        viewModelScope.launch {
            articleRepository.markAsRead(articleId)
        }
    }
}

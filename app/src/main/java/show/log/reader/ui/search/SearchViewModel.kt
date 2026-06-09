package show.log.reader.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import show.log.reader.data.db.ArticleWithFeed
import show.log.reader.data.repository.ArticleRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<ArticleWithFeed>>(emptyList())
    val results: StateFlow<List<ArticleWithFeed>> = _results.asStateFlow()

    init {
        viewModelScope.launch {
            _query.flatMapLatest { q ->
                if (q.isBlank()) {
                    kotlinx.coroutines.flow.flowOf(emptyList())
                } else {
                    val ftsQuery = q.trim().split("\\s+".toRegex()).joinToString(" AND ")
                    articleRepository.searchWithFeedTitle(ftsQuery)
                }
            }.collect { list ->
                _results.value = list
            }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun markAsRead(articleId: Long) {
        viewModelScope.launch {
            articleRepository.markAsRead(articleId)
        }
    }
}

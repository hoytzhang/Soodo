package show.log.reader.data.repository

import kotlinx.coroutines.flow.Flow
import show.log.reader.domain.model.Feed

interface FeedRepository {

    suspend fun addFeed(url: String): Result<Feed>

    suspend fun deleteFeed(id: Long): Result<Unit>

    suspend fun updateFeed(feed: Feed): Result<Unit>

    fun observeAll(): Flow<List<Feed>>

    suspend fun getById(id: Long): Feed?

    suspend fun getByUrl(url: String): Feed?

    suspend fun refreshFeed(id: Long): Result<Unit>

    suspend fun refreshAll(): Result<Unit>
}

package show.log.reader.data.mapper

import show.log.reader.data.db.entity.ArticleEntity
import show.log.reader.data.db.entity.FeedEntity
import show.log.reader.data.parser.ParsedArticle
import show.log.reader.data.parser.ParsedFeed
import show.log.reader.domain.model.Article
import show.log.reader.domain.model.Feed

fun FeedEntity.toDomain() = Feed(
    id = id,
    title = title,
    description = description,
    url = url,
    siteUrl = siteUrl,
    iconUrl = iconUrl,
    lastSyncAt = lastSyncAt,
    etag = etag,
    lastModified = lastModified,
    createdAt = createdAt,
    updatedAt = updatedAt,
    category = category,
)

fun Feed.toEntity() = FeedEntity(
    id = id,
    title = title,
    description = description,
    url = url,
    siteUrl = siteUrl,
    iconUrl = iconUrl,
    lastSyncAt = lastSyncAt,
    etag = etag,
    lastModified = lastModified,
    createdAt = createdAt,
    updatedAt = updatedAt,
    category = category,
)

fun ArticleEntity.toDomain() = Article(
    id = id,
    feedId = feedId,
    title = title,
    link = link,
    author = author,
    description = description,
    content = content,
    imageUrl = imageUrl,
    publishedAt = publishedAt,
    isRead = isRead,
    isBookmarked = isBookmarked,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ParsedFeed.toFeedEntity(url: String) = FeedEntity(
    title = title,
    description = description,
    url = url,
    siteUrl = siteUrl,
    iconUrl = iconUrl,
)

fun ParsedArticle.toArticleEntity(feedId: Long) = ArticleEntity(
    feedId = feedId,
    title = title,
    link = link,
    author = author,
    description = description,
    content = content,
    imageUrl = imageUrl,
    publishedAt = publishedAt,
)

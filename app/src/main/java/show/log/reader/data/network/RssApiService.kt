package show.log.reader.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface RssApiService {

    @GET
    suspend fun fetchFeed(
        @Url url: String,
        @Header("If-None-Match") etag: String? = null,
        @Header("If-Modified-Since") lastModified: String? = null,
    ): Response<String>
}

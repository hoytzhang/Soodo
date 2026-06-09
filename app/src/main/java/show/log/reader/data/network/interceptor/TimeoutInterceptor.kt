package show.log.reader.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

class TimeoutInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: SocketTimeoutException) {
            throw TimeoutException("Request timed out: ${chain.request().url}", e)
        }
    }

    class TimeoutException(message: String, cause: Throwable) : IOException(message, cause)
}

package ddklient

import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking

private val gson = Gson()

private const val ipifyService = "https://api.ipify.org?format=json"
data class Ipify(val ip: String)

fun getIp() = runBlocking {
    HttpClient(CIO).use { c ->
        val response = c.get<String>(ipifyService)
        gson.fromJson(response, Ipify::class.java).ip
    }
}

fun submit(cache: Cache, config: Configuration): List<HttpResponse> = runBlocking {
    HttpClient(CIO).use { c ->
        getTasks(cache, config).map { task ->
            c.post {
                url(config.api())
                body = gson.toJson(task)
            }
        }
    }
}

fun getTasks(cache: Cache, config: Configuration): List<Task> = config.hosts().map { host ->
    when (config.kind()) {
        ClientKind.NAMECHEAP -> Namecheap(host = host, ip = cache.getCachedIp(), domain = config.domain(), password = config.password())
    }
}

enum class ClientKind { NAMECHEAP; }

sealed class Task
data class Namecheap(val host: String, val ip: String, val domain: String, val password: String) : Task()

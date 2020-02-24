package ddklient

import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.formUrlEncode
import io.ktor.http.parametersOf
import kotlinx.coroutines.runBlocking

private val gson = Gson()

const val IPIFY = "https://api.ipify.org?format=json"

data class Ipify(val ip: String)

fun getIp() = runBlocking {
    HttpClient(CIO).use { c ->
        val response = c.get<String>(IPIFY)
        gson.fromJson(response, Ipify::class.java).ip
    }
}

fun makeRequests(cache: Cache, config: Configuration): List<HttpResponse> = runBlocking {
    HttpClient(CIO).use { client ->
        getTasks(cache, config).map { task ->
            val request = task.toRequest(config)
            client.get<HttpResponse>(request)
        }
    }
}

fun Task.toRequest(config: Configuration): HttpRequestBuilder = when (this) {
    is Namecheap -> {
        val params = parametersOf(
                "host" to listOf(host), "ip" to listOf(ip), "domain" to listOf(domain), "password" to listOf(password)
        ).formUrlEncode()
        HttpRequestBuilder().apply { url("${config.api()}?$params") }
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

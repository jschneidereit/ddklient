package ddklient

import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.formUrlEncode
import io.ktor.http.parametersOf
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
            when (task) {
                is Namecheap -> { // TODO map tasks to requests instead of firing them in the switch
                    val params = parametersOf(
                            "host" to listOf(task.host),
                            "ip" to listOf(task.ip),
                            "domain" to listOf(task.domain),
                            "password" to listOf(task.password)
                    ).formUrlEncode()

                    c.get<HttpResponse> { url("${config.api()}?$params") }
                }
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

package ddklient

import io.ktor.http.HttpStatusCode

fun main() {
    var running = true
    println("Starting up ddklient")

    val config = Configuration()
    val cache = Cache()

    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down ddklient")
        running = false
    })

    while (running) {
        update(cache, config)
        Thread.sleep(config.timeout())
    }
}

fun update(cache: Cache, configuration: Configuration) {
    val ip = getIp()
    val cached = cache.getCachedIp()
    val stale = !cached.equals(ip, ignoreCase = true)

    if (stale) {
        println("Submitting updated cache with new IP address: $ip")

        cache.setCachedIp(ip)

        if (makeRequests(cache, configuration).any { it.status != HttpStatusCode.OK }) {
            System.err.println("A response failed, clearing cache so next run will proceed")
            cache.clearCachedIp()
        } else {
            println("All API requests responded with OK")
        }
    }
}

package ddklient

import io.ktor.http.HttpStatusCode



fun main() {
    var running: Boolean
    log("starting up")

    val config = Configuration()
    val cache = Cache()

    Runtime.getRuntime().addShutdownHook(Thread {
        log("shutting down")
        running = false
    })

    log("executing initial check and update")
    running = update(cache, config)

    log("starting to monitor with a timeout of ${config.timeout() / 1000} seconds")
    while (running) {
        update(cache, config)
        Thread.sleep(config.timeout())
    }
}

fun update(cache: Cache, configuration: Configuration): Boolean {
    return try {
        val ip = getIp()
        val cached = cache.getCachedIp()

        val stale = cached.isBlank() || !ip.equals(cached, ignoreCase = true)
        if (stale) {
            log("submitting updated cache with new IP address: $ip")
            cache.setCachedIp(ip)

            if (makeRequests(cache, configuration).any { it.status != HttpStatusCode.OK }) {
                err("a response failed, clearing cache so next run will proceed")
                cache.clearCachedIp()
            } else {
                err("all API requests responded with OK")
            }
        } else {
            log("cache is still valid: current ip '$ip' current cache '$cached'")
        }

        true
    } catch (ex: Exception) {
        err("shutting down due to caught unexpected exception when trying to update: $ex")
        false
    }
}

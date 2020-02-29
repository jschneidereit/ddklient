package ddklient

import org.apache.commons.validator.routines.InetAddressValidator
import java.io.File

private val validator = InetAddressValidator.getInstance()

private const val cacheFileName = ".ddklientcache"

private val errorFile = File("")

internal fun tryBuildCacheFile(directory: File): File {
    if (!directory.exists() || directory.isFile) {
        return errorFile
    }

    return try {
        directory.resolve(cacheFileName).apply { createNewFile() }
    } catch (ex: Exception) {
        err("""
        Unable to create cache file at the supplied path '$directory'.
        WIll fallback to in-memory cache only.
        Caught exception: $ex
    """.trimIndent())
        errorFile
    }
}

class Cache {
    private var warm = ""
    private val cold = lazy {
        tryBuildCacheFile(File(System.getenv(DDK_CACHE_DIR_KEY) ?: "")).also {
            if (it.exists()) {
                log("cold cache is writing to ${it.path}")
            } else {
                err("the specified directory '${System.getenv(DDK_CACHE_DIR_KEY) ?: ""}' does not exist or is a file")
            }
        }
    }

    private fun getCacheFileContents(): String {
        val result = if (cold.value.exists() && cold.value.canRead())
            cold.value.useLines { it.firstOrNull()?.trim() ?: "" }
        else
            ""

        return if (validator.isValid(result)) {
            result
        } else {
            clearCachedIp()
            ""
        }
    }

    private fun setCacheFileContents(ip: String) {
        if (cold.value.exists() && cold.value.canWrite()) {
            cold.value.writeText(ip)
        }
    }

    fun clearCachedIp() {
        warm = ""
        setCacheFileContents("")
    }

    fun getCachedIp(): String =
            if (warm.isNotBlank()) warm else getCacheFileContents().also { warm = it }

    fun setCachedIp(ip: String): Boolean = ip.trim().let {
        return if (!validator.isValid(it)) {
            err("tried to set cache with invalid ip address: '$ip'.")
            false
        } else {
            warm = it
            setCacheFileContents(it)
            true
        }
    }
}








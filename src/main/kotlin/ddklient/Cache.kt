package ddklient

import org.apache.commons.validator.routines.InetAddressValidator
import java.io.File

const val CACHE_ENV_KEY = "DDNS_CACHE_DIR"
private val validator = InetAddressValidator.getInstance()

class Cache {
    private var warm = ""
    private val cold = lazy {
        File(System.getenv(CACHE_ENV_KEY) ?: "").also {
            if (!it.exists() && it.path.isNotBlank()) {
                try {
                    it.createNewFile()
                } catch (ex: Exception) {
                    println("Unable to create file at path '${it.path}. Caught exception: $ex")
                }
            }
        }
    }

    private fun getCacheFileContents(): String =
            if (cold.value.exists()) cold.value.useLines { it.firstOrNull()?.trim() ?: "" } else ""

    private fun setCacheFileContents(ip: String) {
        if (cold.value.exists()) {
            cold.value.writeText(ip)
        }
    }

    fun clearCachedIp() {
        warm = ""
        setCachedIp("")
    }

    fun getCachedIp(): String =
            if (warm.isNotBlank()) warm else getCacheFileContents().also { warm = it }

    fun setCachedIp(ip: String): Boolean = ip.trim().let {
        return if (!validator.isValid(it)) {
            System.err.println("Error: tried to set cache with invalid ip address: '$ip'.")
            false
        } else {
            warm = it
            setCacheFileContents(it)
            true
        }
    }
}








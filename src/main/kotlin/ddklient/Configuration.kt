package ddklient

import org.apache.commons.validator.routines.DomainValidator

const val DDNS_KIND_KEY = "DDNS_KIND"
const val DDNS_DOMAIN_KEY = "DDNS_DOMAIN"
const val DDNS_PASSWORD_KEY = "DDNS_PASSWORD"
const val DDNS_HOSTS_KEY = "DDNS_HOSTS"

private val validator = DomainValidator.getInstance()

class Configuration {
    fun kind() = _kind.value
    private val _kind = lazy {
        val kind = System.getenv(DDNS_KIND_KEY)?.trim() ?: ""

        if (kind.isBlank()) {
            throw IllegalArgumentException("Env var $DDNS_KIND_KEY must be set with a valid kind, valid values: ${ClientKind.values()}.")
        }

        ClientKind.valueOf(kind.toUpperCase())
    }

    fun api() = _api.value
    private val _api = lazy {
        kind().getApiUrlForClient()
    }

    fun password() = _password.value
    private val _password = lazy {
        val password = System.getenv(DDNS_PASSWORD_KEY)

        if (password.isNullOrBlank()) {
            throw IllegalArgumentException("Unable to find password associated with env var key '$DDNS_PASSWORD_KEY'.")
        }

        password
    }

    fun domain() = _domain.value
    private val _domain = lazy {
        val domain = System.getenv(DDNS_DOMAIN_KEY)

        if (domain.isNullOrBlank()) {
            throw IllegalArgumentException("Unable to find domain url associated with env var key '$DDNS_DOMAIN_KEY'.")
        }

        if (!validator.isValid(domain)) {
            throw IllegalArgumentException("Apache commons validator considers the domain '$domain' invalid.")
        }

        domain
    }

    fun hosts() = _hosts.value
    private val _hosts = lazy {
        val hosts = (System.getenv(DDNS_HOSTS_KEY) ?: "").split(',').filter { it.isNotBlank() }

        if (hosts.isEmpty()) {
            throw IllegalArgumentException("Env var $DDNS_HOSTS_KEY must be set with one or more host for namecheap. E.g.: 'www' or '@,www'.")
        }

        hosts
    }
}

private const val NAMECHEAP_API = "https://dynamicdns.park-your-domain.com/update"

private fun ClientKind.getApiUrlForClient(): String = when (this) {
    ClientKind.NAMECHEAP -> NAMECHEAP_API
}

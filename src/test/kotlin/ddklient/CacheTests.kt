package ddklient

import io.kotlintest.data.forall
import io.kotlintest.extensions.system.captureStandardErr
import io.kotlintest.extensions.system.withEnvironment
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import java.util.*

private const val validIpv4 = "127.0.0.1"
private const val validIpv6 = "::1"

private val validIpRows = arrayOf(row(validIpv4), row(validIpv6))

class CacheTests : StringSpec({
    "cached ip should work without a valid cache file path" {
        withEnvironment(DDK_CACHE_DIR_KEY to "") {
            val sut = Cache()
            sut.getCachedIp() shouldBe ""

            forall(*validIpRows) { ip ->
                sut.setCachedIp(ip) shouldBe true
                sut.getCachedIp() shouldBe ip
            }
        }
    }

    "cached ip should be set in cache file" {
        val temp = createTempFile().also { it.deleteOnExit() }
        withEnvironment(DDK_CACHE_DIR_KEY to temp.path) {
            val sut = Cache()
            sut.getCachedIp() shouldBe ""

            forall(*validIpRows) { ip ->
                sut.setCachedIp(ip) shouldBe true
                sut.getCachedIp() shouldBe ip
            }
        }
    }

    "cached ip in file should be seeded on access of cache" {
        forall(*validIpRows) { ip ->
            val temp = createTempFile().also { it.deleteOnExit() }
            temp.writeText(ip)

            withEnvironment(DDK_CACHE_DIR_KEY to temp.path) {
                val sut = Cache()
                sut.getCachedIp() shouldBe ip
            }
        }
    }

    "cache will reject an invalid ip address" {
        withEnvironment(DDK_CACHE_DIR_KEY to "") {
            val sut = Cache()
            sut.setCachedIp(validIpv4)
            forall(row(""), row(UUID.randomUUID().toString()), row("\t"), row("\n"), row("")) { ip ->
                captureStandardErr {
                    sut.setCachedIp(ip) shouldBe false
                }

                sut.getCachedIp() shouldBe validIpv4
            }
        }
    }

    "cache will write to stderr when rejecting an invalid ip address" {
        val nonsense = UUID.randomUUID().toString()

        val error = captureStandardErr {
            val cache = Cache()
            cache.setCachedIp(nonsense) shouldBe false
        }

        error shouldStartWith "Error"
        error shouldContain nonsense
    }

    "cache should prioritize warm over cold storage" {
        val temp = createTempFile().also { it.deleteOnExit() }
        withEnvironment(DDK_CACHE_DIR_KEY to temp.path) {
            val first = Cache()
            first.setCachedIp(validIpv4)

            val nonsense = UUID.randomUUID().toString()
            temp.writeText(nonsense)

            first.getCachedIp() shouldBe validIpv4

            val second = Cache()
            second.getCachedIp() shouldBe nonsense
        }
    }
})

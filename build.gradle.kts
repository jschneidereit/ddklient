plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
    id("com.google.cloud.tools.jib") version "2.0.0"
    application
    java
}

version = "0.1"
val buildNumber by extra("0")

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("commons-validator:commons-validator:1.6")
    implementation("io.ktor:ktor-client-core:1.3.1")
    implementation("io.ktor:ktor-client-cio:1.3.1")
    implementation("com.google.code.gson:gson:2.8.6")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClassName = "ddklient.AppKt"
}

jib {
    from {
//        image = "arm64v8/openjdk:11-jre"
        image = "openjdk@sha256:e0a3a408dfab7978f5a5186822ebeb3c2afaa47af0928c19c783b23461adbd89"
    }
    to {
        image = "jschneidereit/ddklient" // TODO: fix this
        tags = setOf("$version", "$version.${extra["buildNumber"]}")
        auth {
            username = System.getenv("USERNAME")
            password = System.getenv("PASSWORD")
        }
    }
    container {
        mainClass = "ddklient.AppKt"
        labels = mapOf(
                "maintainer" to "Jim Schneidereit https://github.com/jschneidereit",
                "org.opencontainers.image.title" to "ddklient: A dynamic dns client in kotlin jib container",
                "org.opencontainers.image.description" to "ddklient: A dynamic dns client in a kotlin jib container",
                "org.opencontainers.image.version" to "$version",
                "org.opencontainers.image.authors" to "Jim Schneidereit https://github.com/jschneidereit",
                "org.opencontainers.image.url" to "https://github.com/jschneidereit/ddklient",
                "org.opencontainers.image.vendor" to "https://github.com/jschneidereit",
                "org.opencontainers.image.licenses" to "MIT"
        )
        jvmFlags = listOf(
                "-server",
                "-Djava.awt.headless=true",
                "-XX:InitialRAMFraction=2",
                "-XX:MinRAMFraction=2",
                "-XX:MaxRAMFraction=2",
                "-XX:+UseG1GC",
                "-XX:MaxGCPauseMillis=100",
                "-XX:+UseStringDeduplication"
        )
        workingDirectory = "/"
    }
}

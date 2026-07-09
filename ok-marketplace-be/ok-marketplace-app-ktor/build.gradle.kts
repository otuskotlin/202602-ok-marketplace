import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import ru.otus.otuskotlin.marketplace.plugin.DockerBuildTask
import java.time.Duration

// ============================================================
// Testcontainers в buildscript — это Gradle-уровень.
// Контейнер стартует не из тестового класса, а из задач Gradle.
// Тесты получают порт через system property / env.
// ============================================================
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.testcontainers.core)
    }
}

plugins {
    alias(libs.plugins.kotlinx.serialization)
    id("build-kmp")
    alias(libs.plugins.shadowJar)
    id("build-docker")
}

docker {
    images.register("Jvm") {
        buildContext = project.layout.buildDirectory.dir("docker-jvm").get().toString()
        dockerFile = "Dockerfile"
        dependsOnTask = "jvmJar"
        imageName = "${project.name}-jvm"
        imageTag = "${project.version}"
    }

    images.register("LinuxX64") {
        buildContext = project.layout.buildDirectory.dir("docker-linuxx64").get().toString()
        dockerFile = "Dockerfile"
        dependsOnTask = "linkReleaseExecutableLinuxX64"
        imageName = "${project.name}-x64"
        imageTag = "${project.version}"
    }
}

kotlin {
    targets.removeIf { it.name == "macosX64" }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries {
            executable {
                entryPoint = "ru.otus.otuskotlin.marketplace.app.ktor.main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
                implementation(libs.ktor.server.cors)
                implementation(libs.ktor.server.yaml)
                implementation(libs.ktor.server.negotiation)
                implementation(libs.ktor.server.headers.response)
                implementation(libs.ktor.server.headers.caching)
                implementation(libs.ktor.server.websocket)

//                // Для того, чтоб получать содержимое запроса более одного раза
//                В Application.main добавить `install(DoubleReceive)`
//                implementation("io.ktor:ktor-server-double-receive:${libs.versions.ktor.get()}")

                implementation(project(":ok-marketplace-common"))
                implementation(project(":ok-marketplace-app-common"))
                implementation(project(":ok-marketplace-biz"))

                // v2 api
                implementation(project(":ok-marketplace-api-v2-kmp"))

                // Stubs
                implementation(project(":ok-marketplace-stubs"))
                // RabbitMQ
//                implementation(project(":ok-marketplace-app-rabbit"))

                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.serialization.json)

                // DB
                implementation(libs.uuid)
                implementation(projects.okMarketplaceRepoCommon)
                implementation(projects.okMarketplaceRepoStubs)
                implementation(projects.okMarketplaceRepoInmemory)

                /**
                 * Репозиторий PostgreSQL — мультиплатформенный.
                 * Доступен для JVM, linuxX64 и macosArm64.
                 */
                implementation(projects.okMarketplaceRepoPgsqlx4k)

                // logging
                implementation(project(":ok-marketplace-api-log1"))
                implementation(libs.mkpl.logs.common)
                implementation(libs.mkpl.logs.kermit)
                implementation(libs.mkpl.logs.socket)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

                // DB
                implementation(projects.okMarketplaceRepoCommon)

                implementation(libs.ktor.server.test)
                implementation(libs.ktor.client.negotiation)

                // Чтение переменных окружения (JVM + Native)
                implementation(libs.mkpl.sysenv)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))

                // jackson
                implementation(libs.ktor.serialization.jackson)
                implementation(libs.ktor.server.calllogging)
                implementation(libs.ktor.server.headers.default)

                implementation(libs.ktor.server.tomcat.jakarta)

                implementation(libs.logback)

                // transport models
                implementation(projects.okMarketplaceApiV1Jackson)
                implementation(projects.okMarketplaceApiV1Mappers)
                implementation(projects.okMarketplaceApiV2Kmp)

                implementation("ru.otus.otuskotlin.marketplace.libs:ok-marketplace-lib-logging-logback")
                implementation(projects.okMarketplaceRepoCassandra)
                implementation(libs.testcontainers.postgres)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        /**
         * linuxX64Test и macosArm64Test — PG-тесты для native.
         * V2 API мультиплатформенный, поэтому V2 PG тесты можно запускать на native.
         * Порт PostgreSQL читается через actual fun psqlPort() из env (getenv).
         */
        val linuxX64Test by getting {
            dependencies {
                implementation(projects.okMarketplaceRepoTests)
                implementation(projects.okMarketplaceRepoPgsqlx4k)
            }
        }
        val macosArm64Test by getting {
            dependencies {
                implementation(projects.okMarketplaceRepoTests)
                implementation(projects.okMarketplaceRepoPgsqlx4k)
            }
        }
    }
}

// ============================================================
//  PostgreSQL через Testcontainers (Gradle-level)
//  Используем ComposeContainer — он поднимает psql + liquibase
//  из docker-compose-pg.yml, который лежит в тестовых ресурсах.
// ============================================================
val PG_SERVICE = "psql"
val MG_SERVICE = "liquibase"

val pgContainer: ComposeContainer by lazy {
    val res = objects.fileCollection()
        .from("src/jvmTest/resources/docker-compose-pg.yml")
        .singleFile
    ComposeContainer(res)
        .withExposedService(PG_SERVICE, 5432)
        .withStartupTimeout(Duration.ofSeconds(300))
        .waitingFor(
            MG_SERVICE,
            Wait.forLogMessage(".*Liquibase command 'update' was executed successfully.*", 1)
        )
}

val pgUp by tasks.registering {
    doFirst {
        println("Starting PostgreSQL container...")
        pgContainer.start()
        println("PostgreSQL started at port: ${pgContainer.getServicePort(PG_SERVICE, 5432)}")
    }
    finalizedBy(pgDn)
}

val pgDn by tasks.registering {
    doFirst {
        println("Stopping PostgreSQL container...")
        pgContainer.stop()
    }
}

// ============================================================
//  Настройка JVM-тестов
// ============================================================
/**
 * jvmTest — обычные тесты, НЕ требующие PostgreSQL.
 * PG-тесты исключены через фильтр.
 */
tasks.named<Test>("jvmTest") {
    filter.excludeTestsMatching("*AdRepoPGTest*")
}

/**
 * jvmTestPg — только PG-тесты, перед ними стартует Docker-контейнер.
 * Используем тот же testClassesDirs и classpath, что и jvmTest,
 * но с противоположным фильтром.
 */
val jvmTestPg by tasks.registering(Test::class) {
    group = "verification"
    description = "Запускает только PG-тесты с поднятием Docker-контейнера (psql + liquibase)"

    dependsOn(pgUp)
    finalizedBy(pgDn)

    filter.includeTestsMatching("*AdRepoPGTest*")

    testClassesDirs = sourceSets["jvmTest"].output.classesDirs
    classpath = sourceSets["jvmTest"].runtimeClasspath

    // Передаём параметры PostgreSQL в тесты через environment (единый API для JVM и native)
    doFirst {
        val pgPort = pgContainer.getServicePort(PG_SERVICE, 5432)
        environment("postgresHost", "localhost")
        environment("postgresPort", pgPort.toString())
        environment("postgresUser", "postgres")
        environment("postgresPass", "marketplace-pass")
    }

    useJUnit()
}

// ============================================================
//  Настройка Native-тестов (linuxX64, macosArm64)
//
//  Без флага -PwithPg: PG-тесты исключены, контейнер не стартует.
//  С флагом -PwithPg:   PG-тесты запускаются, перед ними стартует Docker.
//  Порт PostgreSQL передаётся в тест через env (getenv на native).
// ============================================================
tasks.withType<KotlinNativeTest>().configureEach {
    if (!project.hasProperty("withPg")) {
        filter.excludeTestsMatching("*AdRepoPGTest*")
    } else {
        dependsOn(pgUp)
        finalizedBy(pgDn)
        doFirst {
            val pgPort = pgContainer.getServicePort(PG_SERVICE, 5432)
            environment("postgresHost", "localhost")
            environment("postgresPort", pgPort.toString())
            environment("postgresUser", "postgres")
            environment("postgresPass", "marketplace-pass")
        }
    }
}

// ============================================================
//  check — запускает все тесты: обычные + PG
// ============================================================
tasks.named("check") {
    dependsOn(jvmTestPg)
}

tasks {
    withType(ProcessResources::class) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        isZip64 = true
        manifest {
            attributes["Main-Class"] = "io.ktor.server.cio.EngineMain"
        }
        dependencies {
            exclude(dependency("org.graalvm.js:js:.*"))
            exclude(dependency("org.graalvm.polyglot:js:.*"))
        }
        exclude("**/*.pom")
        exclude("**/*.module")
    }
}

afterEvaluate {
    tasks {
        named("dockerBuildJvm", DockerBuildTask::class) {
            dependsOn(shadowJar)
            group = "docker"
            doFirst {
                copy {
                    from("Dockerfile.jvm") { rename { "Dockerfile" } }
                    from(shadowJar.get().archiveFile.get())
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    println("BUILD CONTEXT: ${buildContext.get()}")
                    into(buildContext)
                }
            }
        }

        named("dockerBuildLinuxX64", DockerBuildTask::class) {
            dependsOn("linkReleaseExecutableLinuxX64")
            dependsOn("linuxX64ProcessResources")
            group = "docker"
            doFirst {
                copy {
                    from("Dockerfile")
                    from(getByName("linkReleaseExecutableLinuxX64").outputs)
                    from(linuxX64ProcessResources.get().outputs)
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    println("BUILD CONTEXT: ${buildContext.get()}")
                    into(buildContext)
                }
            }
        }
    }
}

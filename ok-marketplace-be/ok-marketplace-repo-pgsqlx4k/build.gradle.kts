import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration

// ============================================================
//  Testcontainers в buildscript — контейнер управляется Gradle.
// ============================================================
buildscript {
    repositories { mavenCentral() }
    dependencies { classpath(libs.testcontainers.core) }
}

plugins {
    id("build-kmp")
}

kotlin {
    targets.removeIf { it.name == "macosX64" }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.okMarketplaceCommon)
                api(projects.okMarketplaceRepoCommon)

                implementation(libs.coroutines.core)
                implementation(libs.uuid)
                implementation(libs.sqlx4k.postgres)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(projects.okMarketplaceRepoTests)

                // Чтение postgresPort из system property / env
                implementation(libs.mkpl.sysenv)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val linuxX64Test by getting {
            dependencies {
                implementation(projects.okMarketplaceRepoTests)
                implementation(libs.mkpl.sysenv)
            }
        }
        val macosArm64Test by getting {
            dependencies {
                implementation(projects.okMarketplaceRepoTests)
                implementation(libs.mkpl.sysenv)
            }
        }
    }
}

// ============================================================
//  PostgreSQL через Testcontainers (Gradle-level)
//  Используем docker-compose из ok-marketplace-migration-pg
// ============================================================
val PG_SERVICE = "psql"
val MG_SERVICE = "liquibase"

val pgContainer: ComposeContainer by lazy {
    val composeFile = file("../../ok-marketplace-other/ok-marketplace-migration-pg/src/test/compose/docker-compose-pg.yml")
    ComposeContainer(composeFile)
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
}

val pgDn by tasks.registering {
    doFirst {
        println("Stopping PostgreSQL container...")
        pgContainer.stop()
    }
}

// ============================================================
//  JVM + Native тесты — PG порт через environment (только env)
// ============================================================
tasks.named<Test>("jvmTest") {
    dependsOn(pgUp)
    finalizedBy(pgDn)
    doFirst { environment("postgresPort", pgContainer.getServicePort(PG_SERVICE, 5432).toString()) }
}

tasks.withType<KotlinNativeTest>().configureEach {
    dependsOn(pgUp)
    finalizedBy(pgDn)
    doFirst { environment("postgresPort", pgContainer.getServicePort(PG_SERVICE, 5432).toString()) }
}

import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import ru.otus.otuskotlin.marketplace.plugin.DockerBuildTask

plugins {
    alias(libs.plugins.kotlinx.serialization)
    id("build-kmp")
    alias(libs.plugins.shadowJar)
    id("build-docker")
}

docker {
    // JVM образ
    images.register("Jvm") {
        buildContext = project.layout.buildDirectory.dir("docker-jvm").get().toString()
        dockerFile = "Dockerfile"
        dependsOnTask = "jvmJar"
        imageName = "${project.name}-jvm"
        imageTag = "${project.version}"
    }

    // Native образ для Linux x64
    images.register("LinuxX64") {
        buildContext = project.layout.buildDirectory.dir("docker-linuxx64").get().toString()
        dockerFile = "Dockerfile"
        dependsOnTask = "linkReleaseExecutableLinuxX64"
        imageName = "${project.name}-x64"
        imageTag = "${project.version}"
    }
}

kotlin {
    // !!! Обязательно. Иначе не проходит сборка толстых джанриков в shadowJar
    jvm {  }
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

                implementation(project(":ok-marketplace-common"))
                implementation(project(":ok-marketplace-app-common"))
                implementation(project(":ok-marketplace-biz"))

                // v2 api
                implementation(project(":ok-marketplace-api-v2-kmp"))

                // Stubs
                implementation(project(":ok-marketplace-stubs"))

                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.serialization.json)

                // DB
                implementation(libs.uuid)
                implementation(projects.okMarketplaceRepoCommon)
                implementation(projects.okMarketplaceRepoStubs)
                implementation(projects.okMarketplaceRepoInmemory)

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
                implementation(project(":ok-marketplace-api-v1-jackson"))
                implementation(project(":ok-marketplace-api-v1-mappers"))

                implementation("ru.otus.otuskotlin.marketplace.libs:ok-marketplace-lib-logging-logback")

                implementation(projects.okMarketplaceRepoPgjvm)
                implementation(libs.testcontainers.postgres)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}

tasks {
    // Если ошибка: "Entry application.yaml is a duplicate but no duplicate handling strategy has been set."
    // Возникает из-за наличия файлов как в common, так и в jvm платформе
    withType(ProcessResources::class) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }


    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        isZip64 = true
        manifest {
            // Optionally, set the main class for the shadowed JAR.
            attributes["Main-Class"] = "io.ktor.server.cio.EngineMain"
        }
        dependencies {
            exclude(dependency("org.graalvm.js:js:.*"))
            exclude(dependency("org.graalvm.polyglot:js:.*"))
        }
        // Исключаем проблемные файлы из упаковки
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
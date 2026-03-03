import org.gradle.kotlin.dsl.named
import ru.otus.otuskotlin.marketplace.plugin.DockerBuildTask

plugins {
    id("build-jvm")
    application
    alias(libs.plugins.shadowJar)
    id("build-docker")
}

docker {
    // JVM образ
    images.register("Jvm") {
        buildContext = project.layout.buildDirectory.dir("docker-jvm").get().toString()
        dockerFile = "Dockerfile"
        dependsOnTask = "jvmJar"
        imageName = project.name
        imageTag = "${project.version}"
    }
}

dependencies {

    implementation(kotlin("stdlib"))

    implementation(libs.rabbitmq.client)
    implementation(libs.jackson.databind)
    implementation(libs.logback)
    implementation(libs.coroutines.core)

    implementation(project(":ok-marketplace-common"))
    implementation(project(":ok-marketplace-app-common"))
    implementation("ru.otus.otuskotlin.marketplace.libs:ok-marketplace-lib-logging-logback")

    // v1 api
    implementation(project(":ok-marketplace-api-v1-jackson"))
    implementation(project(":ok-marketplace-api-v1-mappers"))

    // v2 api
    implementation(project(":ok-marketplace-api-v2-kmp"))

    implementation(project(":ok-marketplace-biz"))
    implementation(project(":ok-marketplace-stubs"))

    testImplementation(libs.testcontainers.rabbitmq)
    testImplementation(kotlin("test"))
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        isZip64 = true
        manifest {
            // Optionally, set the main class for the shadowed JAR.
            attributes["Main-Class"] = "ru.otus.otuskotlin.marketplace.app.rabbit.MainKt"
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
                    from("Dockerfile")
                    from(shadowJar.get().archiveFile.get())
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    println("BUILD CONTEXT: ${buildContext.get()}")
                    into(buildContext)
                }
            }
        }
    }
}
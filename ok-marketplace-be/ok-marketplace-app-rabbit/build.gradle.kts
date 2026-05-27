plugins {
    id("build-jvm")
    alias(libs.plugins.crowdproj.docker)
}

docker {
    imageJvm("Jvm") {
        imageName = project.name
        dockerFile = "Dockerfile"
        imageTag = project.version.toString()
        mainClass = "ru.otus.otuskotlin.marketplace.app.rabbit.ApplicationKt"
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

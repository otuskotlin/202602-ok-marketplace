plugins {
    id("build-jvm")
}

// 1. Настраиваем конфигурацию для получения файла из другого проекта
val resourcesFromLib by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    implementation(kotlin("stdlib"))

    resourcesFromLib("${libs.ok.mkpl.dcompose.get()}:resources@zip")

    implementation("ru.otus.otuskotlin.marketplace:ok-marketplace-api-v1-jackson")
    implementation("ru.otus.otuskotlin.marketplace:ok-marketplace-api-v1-mappers")
    implementation("ru.otus.otuskotlin.marketplace:ok-marketplace-api-v2-kmp")
    implementation("ru.otus.otuskotlin.marketplace:ok-marketplace-stubs")

    testImplementation(kotlin("test-junit5"))

    testImplementation(libs.logback)
    testImplementation(libs.kermit)

    testImplementation(libs.bundles.kotest)

    testImplementation(libs.testcontainers.core)
    testImplementation(libs.coroutines.core)

    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.okhttp)

    testImplementation(libs.rabbitmq.client)
    testImplementation(libs.kafka.client)
}

var severity: String = "MINOR"

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
        dependsOn("extractLibResources")
    }
    register<Copy>("extractLibResources") {
        from(resourcesFromLib.elements.map { it.map { file -> zipTree(file) } })
        into(layout.buildDirectory.dir("dcompose"))
    }
}

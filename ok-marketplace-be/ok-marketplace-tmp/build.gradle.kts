plugins {
    id("build-jvm")
}

// 1. Настраиваем конфигурацию для получения файла из другого проекта
val resourcesFromLib by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    implementation(libs.kotlinx.datetime)
    resourcesFromLib("ru.otus.otuskotlin.marketplace:ok-marketplace-dcompose:0.1.0:resources@zip")
}

tasks {
    val extractLibResources by registering(Copy::class) {
        description = "Извлекаем ресурсы из zip"
        from(resourcesFromLib.incoming.files.elements.map {
            it.map { file -> zipTree(file) }
        })
        into(layout.buildDirectory.dir("dcompose"))
    }

    val build by getting { dependsOn(extractLibResources) }
}


plugins {
    id("build-jvm")
    alias(libs.plugins.openapi.generator)
}

// 1. Настраиваем конфигурацию для получения файла из другого проекта
val specsFromLib by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    // Используем созданную ранее конфигурацию "specsConfiguration" проекта со спецификациями
    specsFromLib("ru.otus.otuskotlin.marketplace:ok-marketplace-specs:0.1.0:spec@zip")

    implementation(kotlin("stdlib"))
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.datatype)
    testImplementation(kotlin("test-junit"))
}

val specDir = layout.buildDirectory.dir("specs")

tasks {
    // 2. Распаковка архива
    val extractLibSpecs by registering(Copy::class) {
        description = "Извлекаем содержимое архива во временную директорию"
        dependsOn(specsFromLib)
        // Распаковываем ZIP-файл (он будет единственным в этой конфигурации)
        from(specsFromLib.elements.map { it.map { file -> zipTree(file) } })
        into(specDir)
    }

    // 3. Привязываем генерацию к распаковке
    val openApiGenerate by getting() {
        dependsOn(extractLibSpecs)
    }

    compileKotlin {
        dependsOn(openApiGenerate)
    }
}

openApiGenerate {
    val openapiGroup = "${rootProject.group}.api.v1"
    generatorName.set("kotlin")
    packageName.set(openapiGroup)
    apiPackage.set("$openapiGroup.api")
    modelPackage.set("$openapiGroup.models")
    invokerPackage.set("$openapiGroup.invoker")

    // 4. Указываем конкретный файл внутри распакованной папки (например, specs-ad-v1.yaml)
    inputSpec.set(specDir.map { it.file("specs-ad-v1.yaml").asFile.absolutePath })

    globalProperties.apply {
        put("models", "")
        put("modelDocs", "false")
    }

    configOptions.set(
        mapOf(
            "dateLibrary" to "string",
            "enumPropertyNaming" to "UPPERCASE",
            "serializationLibrary" to "jackson",
            "collectionType" to "list"
        )
    )
}

sourceSets {
    main {
        // Путь генерации по умолчанию для плагина (обычно build/generate-resources/main/...)
        java.srcDir(layout.buildDirectory.dir("generate-resources/main/src/main/kotlin"))
    }
}

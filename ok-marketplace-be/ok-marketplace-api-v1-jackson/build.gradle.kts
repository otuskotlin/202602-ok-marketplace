import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("build-jvm")
    alias(libs.plugins.openapi.generator)
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generate-resources/main/src/main/kotlin"))
    }
}

// 1. Настраиваем конфигурацию для получения файла из другого проекта
val specsFromLib by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    specsFromLib("ru.otus.otuskotlin.marketplace:ok-marketplace-specs:0.1.0:spec@zip")
}

val specDir = layout.buildDirectory.dir("specs")

/**
 * Настраиваем генерацию здесь
 */
openApiGenerate {
    val openapiGroup = "${rootProject.group}.api.v1"
    generatorName.set("kotlin") // Это и есть активный генератор
    packageName.set(openapiGroup)
    apiPackage.set("$openapiGroup.api")
    modelPackage.set("$openapiGroup.models")
    invokerPackage.set("$openapiGroup.invoker")
//    inputSpec.set("$specDir/specs-ad-v1.yaml")
    inputSpec.set(specDir.map { it.file("specs-ad-v1.yaml").asFile.absolutePath })

    /**
     * Здесь указываем, что нам нужны только модели, все остальное не нужно
     * https://openapi-generator.tech/docs/globals
     */
    globalProperties.apply {
        put("models", "")
        put("modelDocs", "false")
    }

    /**
     * Настройка дополнительных параметров из документации по генератору
     * https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/kotlin.md
     */
    configOptions.set(
        mapOf(
            "dateLibrary" to "string",
            "enumPropertyNaming" to "UPPERCASE",
            "serializationLibrary" to "jackson",
            "collectionType" to "list"
        )
    )
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.datatype)
    testImplementation(kotlin("test-junit"))
    testImplementation(projects.okMarketplaceStubs)
}

tasks {
    val extractLibSpecs by registering(Copy::class) {
        dependsOn(specsFromLib)
        // Распаковываем ZIP-файл (он будет единственным в этой конфигурации)
        from(specsFromLib.elements.map { it.map { file -> zipTree(file) } })
        into(specDir)
    }

// 3. Привязываем генерацию к распаковке
    named("openApiGenerate") {
        dependsOn(extractLibSpecs)
    }

    val openApiGenerateTask: GenerateTask = getByName("openApiGenerate", GenerateTask::class) {
        outputDir.set(layout.buildDirectory.file("generate-resources").get().toString())
    }
    filter { it.name.startsWith("compile") }.forEach {
        it.dependsOn(openApiGenerateTask)
    }
}

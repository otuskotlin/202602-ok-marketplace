import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("build-kmp")
    alias(libs.plugins.crowdproj.generator)
    alias(libs.plugins.kotlinx.serialization)
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
        finalizedBy("compileCommonMainKotlinMetadata")
    }
    filter { it.name.startsWith("compile") }.forEach {
        it.dependsOn(openApiGenerateTask)
    }
}
crowdprojGenerate {
    packageName.set("${project.group}.api.log1")
    inputSpec.set(specDir.map { it.file("specs-ad-log1.yaml").asFile.absolutePath })
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDirs(layout.buildDirectory.dir("generate-resources/src/commonMain/kotlin"))
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.coroutines.core)
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)

                implementation(project(":ok-marketplace-common"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        jvmMain {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
//
//        all {
//            languageSettings.optIn("kotlin.RequiresOptIn")
//        }
    }
}

//tasks {
//    val openApiGenerateTask: GenerateTask = getByName("openApiGenerate", GenerateTask::class) {
//        outputDir.set(layout.buildDirectory.file("generate-resources").get().toString())
//        finalizedBy("compileCommonMainKotlinMetadata")
//    }
//    filter { it.name.startsWith("compile") }.forEach {
//        it.dependsOn(openApiGenerateTask)
//    }
//}

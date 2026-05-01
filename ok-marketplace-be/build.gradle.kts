plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

group = "ru.otus.otuskotlin.marketplace"
version = "0.1.0"

val specDir = "${rootDir}/../ok-marketplace-other/ok-marketplace-specs/specs"
extra["spec-v1"] = "$specDir/specs-ad-v1.yaml"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks {
    register("build" ) {
        description = "Сборка всех подпроектов"
        group = "build"
    }
    register("clean" ) {
        description = "Очистка всех подпроектов"
        group = "build"
        subprojects.forEach { proj ->
            println("PROJ $proj")
            proj.getTasksByName("clean", false).also {
                this@register.dependsOn(it)
            }
        }
    }
    register("check" ) {
        description = "Запуск тестов всех подпроектов"
        group = "verification"
        subprojects.forEach { proj ->
            println("PROJ $proj")
            proj.getTasksByName("check", false).also {
                this@register.dependsOn(it)
            }
        }
    }
}

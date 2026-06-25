group = "ru.otus.otuskotlin.marketplace"
version = "0.1.0"

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}

tasks {
    register("buildInfra") {
        group = "build"
        dependsOn(project(":ok-marketplace-dcompose").getTasksByName("publish",false))
        dependsOn(project(":ok-marketplace-specs").getTasksByName("publish",false))
        dependsOn(project(":ok-marketplace-swagger").getTasksByName("buildImages",false))
        dependsOn(project(":ok-marketplace-migration-pg").getTasksByName("buildImages",false))
        dependsOn(project(":ok-marketplace-migration-cs").getTasksByName("buildImages",false))
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

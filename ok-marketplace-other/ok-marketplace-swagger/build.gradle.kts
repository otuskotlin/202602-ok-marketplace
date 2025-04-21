// В проекте :ok-marketplace-specs
plugins {
    id("build-jvm")
    alias(libs.plugins.shadowJar)
    id("build-docker")
}

val dockerDir = project.layout.buildDirectory.dir("docker-swagger").get().toString()

docker {
//    imageName = "${project.name}"
    images.register("Swagger") {
        buildContext = dockerDir
        dockerFile = "Dockerfile"
        dependsOnTask = "extractLibSpecs"
        imageName = project.name
        imageTag = "${project.version}"
    }

}

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
        from("Dockerfile", "generate-config.sh")
        into(dockerDir)
    }

    register("buildImages") {
        description = "Сборка докер-образов"
        group = "build"
        dependsOn("dockerBuildSwagger")
    }
}
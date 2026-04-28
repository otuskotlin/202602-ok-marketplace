// В проекте :ok-marketplace-specs
plugins {
    id("build-jvm")
    alias(libs.plugins.shadowJar)
    id("build-docker")
}

val dockerDir = project.layout.buildDirectory.dir("docker-swagger").get().toString()

docker {
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
    specsFromLib("${libs.ok.mkpl.specs.get()}:spec@zip")
}

val specDir = layout.buildDirectory.dir("specs")

tasks {
    register<Copy>("extractLibSpecs") {
        description = "Подготовка директории для Dockerfile"
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
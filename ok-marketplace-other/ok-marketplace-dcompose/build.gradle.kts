plugins {
    id("build-jvm")
    id("maven-publish")
}

val resourcesZip = tasks.register<Zip>("resourcesZip") {
    description = "Упаковка ресурсов в Zip-архив"
    archiveClassifier.set("resources")
    archiveExtension.set("zip")
    from("dcompose")
}

// Добавляем артефакт в стандартную конфигурацию runtime,
// чтобы includeBuild мог его сопоставить при поиске зависимости
configurations {
    runtimeElements {
        outgoing.artifact(resourcesZip)
    }
}

// Публикация
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            artifact(resourcesZip) {
                classifier = "resources"
                extension = "zip"
            }
        }
    }
}

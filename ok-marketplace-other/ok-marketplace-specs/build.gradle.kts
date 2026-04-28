// В проекте :ok-marketplace-specs
plugins {
    id("build-jvm")
    id("maven-publish")
}

val specsZip = tasks.register<Zip>("specsZip") {
    description = "Упаковка спецификаций в Zip-архив"
    archiveClassifier.set("spec")
    archiveExtension.set("zip")
    from("specs")
}

// Добавляем артефакт в стандартную конфигурацию runtime, 
// чтобы includeBuild мог его сопоставить при поиске зависимости
configurations {
    runtimeElements {
        outgoing.artifact(specsZip)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            // Эти данные ДОЛЖНЫ совпадать с тем, что ты запрашиваешь в другом проекте
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            artifact(specsZip) {
                classifier = "spec"
                extension = "zip"
            }
        }
    }
}

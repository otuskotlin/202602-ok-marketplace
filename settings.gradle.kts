pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "ok-marketplace-202602"

//includeBuild("lessons")
includeBuild("ok-marketplace-be")
includeBuild("ok-marketplace-other")
includeBuild("ok-marketplace-tests")
includeBuild("ok-marketplace-libs")
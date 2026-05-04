group = "com.otus.otuskotlin.marketplace"
version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks {
    register("clean") {
        group = "build"
        gradle.includedBuilds.forEach {
            dependsOn(it.task(":clean"))
        }
    }
    register("buildInfra") { ->
        dependsOn(
            gradle.includedBuild("ok-marketplace-other").task(":buildInfra")
        )
    }

//    register("buildImages") {
//        dependsOn(gradle.includedBuild("ok-marketplace-be").task(":buildImages"))
//    }

    register("e2eTests") { ->
        dependsOn(
            gradle.includedBuild("ok-marketplace-tests").task(":e2eTests")
        )
    }

    register("check") {
        group = "verification"
        dependsOn(gradle.includedBuild("ok-marketplace-be").task(":check"))
    }
}


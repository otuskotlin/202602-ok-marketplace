plugins {
    id("build-kmp")
}

kotlin {
    targets.removeIf { it.name == "macosX64" }

    sourceSets {
        val commonMain by getting
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }

        // nativetMain — общий source set для linuxX64 и macosArm64
        val nativetMain by creating {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativetMain)
        }
        val macosArm64Main by getting {
            dependsOn(nativetMain)
        }
    }
}

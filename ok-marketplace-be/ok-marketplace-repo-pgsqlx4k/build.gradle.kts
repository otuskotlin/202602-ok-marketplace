plugins {
    id("build-kmp")
}

kotlin {
    targets.removeIf { it.name == "macosX64" }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.okMarketplaceCommon)
                api(projects.okMarketplaceRepoCommon)

                implementation(libs.coroutines.core)
                implementation(libs.uuid)
                implementation(libs.sqlx4k.postgres)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(projects.okMarketplaceRepoTests)
            }
        }
    }
}

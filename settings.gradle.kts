pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion
    }
}

rootProject.name = "otuskotlin-marketplace-202602"

include("m1l1-first", "m1l2-basic")


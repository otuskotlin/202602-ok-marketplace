pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion
    }
}

rootProject.name = "otuskotlin-marketplace-202602"

include("m1l1-first")
include("m1l2-basic")
include("m1l3-func")
include("m1l4-oop")

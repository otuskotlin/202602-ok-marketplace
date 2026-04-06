plugins {
    //id("build-jvm")
    //kotlin("jvm")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.datetime)
}
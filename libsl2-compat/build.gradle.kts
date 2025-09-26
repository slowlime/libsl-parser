plugins {
    kotlin("jvm")
}

group = "org.jetbrains.research"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":libsl1"))
    implementation(project(":libsl2"))
}

kotlin {
    jvmToolchain(8)
}

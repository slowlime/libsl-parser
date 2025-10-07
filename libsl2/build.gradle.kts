import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    antlr
    kotlin("plugin.serialization") version "1.5.10"
    `maven-publish`
}

group = "org.jetbrains.research"
version = "2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    antlr("org.antlr:antlr4:4.13.2")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompile> {
    dependsOn("generateGrammarSource")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    outputDirectory =
        project.layout.buildDirectory.asFile.get().resolve("generated-src/antlr/main/org/jetbrains/research/libsl2")
    arguments = arguments + listOf("-visitor", "-no-listener", "-long-messages")
    packageName = "${project.group}.libsl2"
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

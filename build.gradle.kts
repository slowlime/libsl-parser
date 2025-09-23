import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.20"
    antlr
    kotlin("plugin.serialization") version "1.5.10"
    `maven-publish`
}

group = "org.jetbrains.research"
version = "1.0-SNAPSHOT"

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
    jvmToolchain(8)
}

tasks.withType<KotlinCompile> {
    dependsOn("generateGrammarSource")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    outputDirectory =
        project.layout.buildDirectory.asFile.get().resolve("generated-src/antlr/main/org/jetbrains/research/libsl")
    arguments = arguments + listOf("-visitor", "-no-listener", "-long-messages", "-package", "${project.group}.libsl")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.vldf"
            artifactId = "libsl"
            version = "1.1.1"

            from(components["java"])
            artifact(sourcesJar) {
                classifier = "sources"
            }
        }
    }
}

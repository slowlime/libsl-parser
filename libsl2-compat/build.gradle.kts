plugins {
    kotlin("jvm")
}

group = "org.jetbrains.research"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    create("cli") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val cliImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val cliRuntimeClasspath by configurations.getting

dependencies {
    implementation(project(":libsl1"))
    implementation(project(":libsl2"))

    cliImplementation("com.github.ajalt.clikt:clikt:5.0.3")
}

val cliUberJar = tasks.register<Jar>("cliUberJar") {
    archiveBaseName = "libsl2-compat-cli"
    archiveClassifier = "uber"

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets["cli"].output)
    from(sourceSets.main.get().output)

    dependsOn(cliRuntimeClasspath)

    from(
        cliRuntimeClasspath
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    )

    manifest {
        attributes["Main-Class"] = "${project.group}.libsl2.compat.cli.MainKt"
    }
}

tasks.named("assemble") {
    dependsOn(cliUberJar)
}

import java.nio.file.Paths

plugins {
    id("java")
}

group = "com.tonic.woodcutter"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.runelite.net")
        content {
            includeGroupByRegex("net\\.runelite.*")
        }
    }
    mavenCentral()
}

val apiVersion = "latest.release"
val sideloadedPluginsDir: File = Paths.get(System.getProperty("user.home"), ".runelite", "sideloaded-plugins").toFile()

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    compileOnly("net.runelite:client:$apiVersion")
    compileOnly("com.tonic:base-api:$apiVersion")
    compileOnly("com.tonic:api:$apiVersion")
}

tasks.register<Jar>("buildPlugin") {
    group = "build"
    description = "Builds a JAR containing the plugin classes and moves to sideloaded-plugins for easy refreshing"
    dependsOn(tasks.named("classes"))

    from(sourceSets.main.get().output)

    archiveBaseName.set(project.name)

    destinationDirectory.set(file(sideloadedPluginsDir))
    doFirst {
        sideloadedPluginsDir.mkdirs()
    }
}
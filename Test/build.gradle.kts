import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    application
}

group = parent!!.group
version = parent!!.version

repositories {
    mavenCentral()
}

dependencies {
    api("net.kyori:adventure-api:4.14.0")
    api("net.kyori:adventure-text-minimessage:4.14.0")

    api(project(":KComp"))

    testImplementation(kotlin("test"))

    PLUGIN_CLASSPATH_CONFIGURATION_NAME(project(":Processor"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}
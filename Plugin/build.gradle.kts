plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij") version "1.15.0"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":KComp"))
}

intellij {
    pluginName.set("KComp")
    version.set("2023.1.4")
    type.set("IC")

    plugins.set(listOf("org.jetbrains.kotlin"))
}

kotlin {
    jvmToolchain(17)
}


plugins {
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish`
}

group = parent!!.group
version = parent!!.version

repositories {
    mavenCentral()
}

dependencies {
    api("net.kyori:adventure-api:4.17.0")
    api("net.kyori:adventure-text-minimessage:4.17.0")

    api(project(":KComp"))

    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.0.20")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}

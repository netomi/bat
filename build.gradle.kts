plugins {
    id("base")
    id("idea")
    id("org.jetbrains.kotlin.jvm") version Versions.kotlinVersion apply false
}

allprojects {
    group = "org.github.netomi.bat"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
            jvmTarget = "11"
        }
    }
}


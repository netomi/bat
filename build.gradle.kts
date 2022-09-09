plugins {
    id("base")
    id("idea")
    id("org.jetbrains.kotlin.jvm") version Versions.kotlinVersion apply false
    id("net.researchgate.release") version "3.0.0"
}

allprojects {
    group = "org.github.netomi.bat"
    version = property("version") ?: "undefined"

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            // enable invokedynamic generation for lanbdas (-Xlambdas=indy)
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all", "-Xlambdas=indy")
            jvmTarget = "11"
        }
    }
}


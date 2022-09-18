plugins {
    id("base")
    id("idea")
    id("org.jetbrains.kotlin.jvm") version Versions.kotlinVersion apply false
    id("net.researchgate.release") version "3.0.2"
}

allprojects {
    group = "com.github.netomi.bat"
    version = property("version") ?: "undefined"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply<JavaLibraryPlugin>()
    apply<MavenPublishPlugin>()

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>(project.name) {
                from(components["java"])
                groupId    = project.group.toString()
                artifactId = project.name
                version    = project.version.toString()
            }
        }

        repositories {
            mavenLocal()
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            // enable invokedynamic generation for lanbdas (-Xlambdas=indy)
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all", "-Xlambdas=indy")
            jvmTarget = "11"
        }
    }
}


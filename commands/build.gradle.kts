plugins {
    kotlin("jvm")
    distribution
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

distributions {
    main {
        distributionBaseName.set("bat-tools")
        contents {
            into("lib") {
                from(tasks["jar"])
                from(configurations.runtimeClasspath)
            }
            from(projectDir) {
                include("bin/")
            }
        }
    }
}

dependencies {
    implementation("info.picocli:picocli:${Versions.picocliVersion}")

    implementation(project(":core"))
    implementation(project(":classfile"))
    implementation(project(":dexfile"))
    implementation(project(":smali"))
    implementation(project(":dexdump"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.jupiterVersion}")
}

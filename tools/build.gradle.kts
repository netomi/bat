plugins {
    kotlin("jvm")
    distribution
}

base {
    archivesName.set("bat-commands")
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
            into("") {
                from("${projectDir}/scripts")
                eachFile {
                    fileMode = 0b111101101
                }
            }
        }
    }
}

dependencies {
    implementation("info.picocli:picocli:${Versions.picocliVersion}")

    implementation(project(":common"))
    implementation(project(":classfile"))
    implementation(project(":dexfile"))
    implementation(project(":smali"))
    implementation(project(":dexdump"))
    implementation(project(":classdump"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.jupiterVersion}")
}

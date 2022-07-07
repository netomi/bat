plugins {
    kotlin("jvm")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("info.picocli:picocli:${Versions.picocliVersion}")

    implementation(project(":core"))
    implementation(project(":dexfile"))
    implementation(project(":smali"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.jupiterVersion}")
}

/*jar {
    manifest {
        attributes "Main-Class": "com.github.netomi.bat.dump.DexDumpCommand"
    }

    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
}*/

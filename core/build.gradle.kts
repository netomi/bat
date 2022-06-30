plugins {
    kotlin("jvm")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    api("com.google.guava:guava:${Versions.guavaVersion}")

    testImplementation(kotlin("test"))
}

plugins {
    kotlin("jvm")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(kotlin("test"))
}

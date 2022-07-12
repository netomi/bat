plugins {
    kotlin("jvm")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":core"))

    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))
}

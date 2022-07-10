plugins {
    kotlin("jvm")
    id("dev.ahmedmourad.nocopy.nocopy-gradle-plugin") version "1.4.0"
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test"))
}

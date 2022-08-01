plugins {
    kotlin("jvm")
}

base {
    archivesName.set("bat-dexfile")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":common"))

    testImplementation(kotlin("test"))
}

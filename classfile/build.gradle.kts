plugins {
    kotlin("jvm")
}

base {
    archivesName.set("bat-classfile")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test"))
}

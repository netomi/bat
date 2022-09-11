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

tasks.compileTestJava {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}

dependencies {
    implementation(project(":common"))
    testImplementation(kotlin("test"))
}

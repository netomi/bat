plugins {
    kotlin("jvm")
}

base {
    archivesName.set("bat-classdump")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":classfile"))
    testImplementation(kotlin("test"))
}

plugins {
    kotlin("jvm")
}

base {
    archivesName.set("bat-shrinker")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":classfile"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.jupiterVersion}")
}

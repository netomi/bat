plugins {
    kotlin("jvm")
}

base {
    archivesName.set("bat-core")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutinesVersion}")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.jupiterVersion}")
}

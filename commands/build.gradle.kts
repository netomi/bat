plugins {
    kotlin("jvm")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("info.picocli:picocli:${Versions.picocliVersion}")

    implementation(project(":core"))
    implementation(project(":classfile"))
    implementation(project(":dexfile"))
    implementation(project(":smali"))
    implementation(project(":dexdump"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.jupiterVersion}")
}

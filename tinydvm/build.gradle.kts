plugins {
    kotlin("jvm")
}

base {
    archivesName.set("bat-tinydvm")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":dexfile"))
    implementation(project(":smali"))

    implementation("javassist:javassist:3.12.1.GA")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.jupiterVersion}")
}

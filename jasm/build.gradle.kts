plugins {
    kotlin("jvm")
    id("antlr")
}

base {
    archivesName.set("bat-jasm")
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks {
    compileKotlin {
        dependsOn(generateGrammarSource)
    }

    generateGrammarSource {
        maxHeapSize = "64m"
        arguments = arguments + listOf("-package", "com.github.netomi.bat.jasmin.parser", "-visitor")
        outputDirectory = File("${project.buildDir}/generated-src/antlr/main/com/github/netomi/bat/jasmin/parser")
    }
}

tasks.test {
    useJUnitPlatform()
}

configurations[JavaPlugin.API_CONFIGURATION_NAME].let { apiConfiguration ->
  apiConfiguration.setExtendsFrom(apiConfiguration.extendsFrom.filter { it.name != "antlr" })
}

dependencies {
    implementation(project(":common"))
    implementation(project(":classfile"))

    // antlr
    antlr("org.antlr:antlr4:${Versions.antlrVersion}")
    implementation("org.antlr:antlr4-runtime:${Versions.antlrVersion}")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.jupiterVersion}")
}

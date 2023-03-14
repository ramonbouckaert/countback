plugins {
    kotlin("js") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("org.siouan.frontend-jdk11") version "6.0.0"
}

group = "io.bouckaert"
version = "2.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("com.github.doyaaaaaken:kotlin-csv:1.8.0")
    implementation("io.ktor:ktor-client-js:2.1.3")
    testImplementation(kotlin("test-js"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}

kotlin {
    js {
        binaries.executable()
        browser {
            testTask {
                useMocha {
                    timeout = "60000ms"
                }
            }
        }
    }
}

tasks.register<Copy>("copyWebWorker") {
    from(layout.buildDirectory.file("distributions/"))
    into(layout.projectDirectory.dir("src/main/client/public"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    finalizedBy("assembleFrontend")
}.get()

tasks.named("assemble") {
    setDependsOn(dependsOn.filter { it != "assembleFrontend" })
    finalizedBy("copyWebWorker")
}

tasks.named("assembleFrontend") {
    setOnlyIf { true }
}

tasks.named("cleanFrontend") {
    setOnlyIf { true }
}

frontend {
    nodeVersion.set("16.15.1")
    packageJsonDirectory.set(layout.projectDirectory.dir("src/main/client").asFile)
    assembleScript.set("run build")
    cleanScript.set("run clean")
}
plugins {
    kotlin("js") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("org.siouan.frontend-jdk11") version "6.0.0"
}

group = "io.bouckaert"
version = "2.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.github.doyaaaaaken:kotlin-csv:1.10.0")
    implementation("io.ktor:ktor-client-js:3.0.1")
    testImplementation(kotlin("test-js"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
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

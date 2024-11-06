plugins {
    kotlin("multiplatform") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("org.siouan.frontend-jdk11") version "6.0.0"
}

group = "io.bouckaert"
version = "2.1.0"

repositories {
    mavenCentral()
}

kotlin {
    js {
        binaries.executable()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
                implementation("co.touchlab:stately-concurrent-collections:2.0.0")
                implementation(kotlin("test"))
            }
        }
        jsMain {
            dependencies {
                implementation("io.ktor:ktor-client-js:3.0.1")
            }
        }
    }
}

tasks.register<Copy>("copyWebWorker") {
    from(layout.buildDirectory.file("dist/js/productionExecutable"))
    into(layout.projectDirectory.dir("src/commonMain/client/public"))
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
    packageJsonDirectory.set(layout.projectDirectory.dir("src/commonMain/client").asFile)
    assembleScript.set("run build")
    cleanScript.set("run clean")
}

plugins {
    java
    kotlin("jvm") version "1.4.10"
}

group = "space.iseki"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0-M1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.compileKotlin {
    kotlinOptions {
        this.freeCompilerArgs = this.freeCompilerArgs + listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}
tasks.compileTestKotlin {
    kotlinOptions {
        this.freeCompilerArgs = this.freeCompilerArgs + listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

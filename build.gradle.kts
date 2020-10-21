plugins {
    java
    kotlin("jvm") version "1.4.10"
    jacoco
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
        jvmTarget = "1.8"
        this.freeCompilerArgs = this.freeCompilerArgs + listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}
tasks.compileTestKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
        this.freeCompilerArgs = this.freeCompilerArgs + listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

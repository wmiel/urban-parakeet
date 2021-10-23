import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    application
}

group = "me.wojtek"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation(platform("software.amazon.awssdk:bom:2.15.0"))
    implementation("software.amazon.awssdk:ecs")
    implementation("software.amazon.awssdk:ecr")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
//
//tasks.withType<KotlinCompile>() {
//    kotlinOptions.jvmTarget = "11"
//}

application {
    mainClassName = "MainKt"
}
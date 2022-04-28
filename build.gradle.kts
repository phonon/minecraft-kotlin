/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

// plugin versioning
val VERSION = "1.6.10" // kotlin version
val JVM = 16         // 1.8 for 8, 11 for 11, 16 for 16

// base of output jar name, full jar will be "kotlin-runtime-jvm{VERSION}-{MINECRAFT_VERSION}.jar"
val OUTPUT_JAR_NAME = "kotlin-runtime"

// target will be set by task
var target = ""

// output jar text that indicates kotlin-reflect included
var useReflect = ""

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    // maven() // no longer needed in gradle 7

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()

    // paper
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JVM))
    }
}

// required configuration for using being able to configure 
// plugins by cli arguments
configurations {
    create("resolvableImplementation") {
        isCanBeResolved = true
        isCanBeConsumed = true
    }
}

dependencies {
    // Align versions of all Kotlin components
    compileOnly(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    configurations["resolvableImplementation"]("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // minecraft version dependent api version
    if ( project.hasProperty("1.12") === true ) {
        compileOnly("com.destroystokyo.paper:paper-api:1.12.2-R0.1-SNAPSHOT")
        target = "1.12"
    } else if ( project.hasProperty("1.16") === true ) {
        compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
        target = "1.16"
    } else if ( project.hasProperty("1.17") === true ) {
        compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
        target = "1.17"
    } else if ( project.hasProperty("1.18") === true ) { // needs jvm 17: TODO add jvm argument
        compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
        target = "1.18"
    }
    
    // optionally add kotlin-reflect api
    if ( project.hasProperty("reflect") === true ) {
        configurations["resolvableImplementation"]("org.jetbrains.kotlin:kotlin-reflect")
        useReflect = "-reflect"
    }

    // set source directories based on target directory
    sourceSets["main"].resources.srcDir("src/mc-${target}/resources")
    sourceSets["main"].java.srcDir("src/mc-${target}")
}

application {
    // Define the main class for the application.
    mainClassName = "phonon.kotlin.KotlinPluginKt"
}

tasks {
    named<ShadowJar>("shadowJar") {
        // verify valid target minecraft version
        doFirst {
            val supportedMinecraftVersions = setOf("1.12", "1.16", "1.17", "1.18")
            if ( !supportedMinecraftVersions.contains(target) ) {
                throw Exception("Invalid Minecraft version! Supported versions are: 1.12, 1.16, 1.17 1.18")
            }
        }

        classifier = ""
        configurations = mutableListOf(project.configurations.named("resolvableImplementation").get())
        
        // do NOT minimize (want entire kotlin runtime)
        //minimize()
    }
}


tasks {
    build {
        dependsOn(shadowJar)
    }
    
    test {
        testLogging.showStandardStreams = true
    }
}

gradle.taskGraph.whenReady {
    tasks {
        named<ShadowJar>("shadowJar") {
            baseName = "${OUTPUT_JAR_NAME}${useReflect}-${VERSION}-jvm${JVM}-mc${target}"
        }
    }
}
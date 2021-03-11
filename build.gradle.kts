plugins {
    kotlin("jvm") version "1.4.31"
    //    val build = "0.6.4"
    //    id("kx.kotlin.11") version build
    //    id("kx.dokka") version build
    //    id("kx.jitpack") version build
    java
}

group = "kotlin.graphics"
version = "3.3.1"

repositories {
    mavenCentral()
}

dependencies {

    implementation(kotlin("stdlib-jdk8"))

    testImplementation("io.kotest:kotest-runner-junit5:4.4.1")
    testImplementation("io.kotest:kotest-assertions-core:4.4.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    modularity.inferModulePath.set(true)
}
val java9 by sourceSets.creating {
    java {
        setSrcDirs(listOf("src/main/java9"))
    }
}
configurations.named(java9.implementationConfigurationName) {
    extendsFrom(configurations.implementation.get())
}

val moduleName = rootProject.run { "$group.$name" }
val compileJava9 = tasks.named<JavaCompile>(java9.compileJavaTaskName) {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(9))
    })
    options.compilerArgumentProviders.add(object : CommandLineArgumentProvider {
        @get:Optional
        @get:Classpath
        protected val compileJavaOutputClasses = tasks.compileJava.flatMap { it.destinationDirectory }
        override fun asArguments() = listOf(
            "--patch-module",
            "$moduleName=${tasks.compileJava.get().destinationDir.absolutePath}")
    })
}

tasks.jar {
    manifest {
        attributes("Multi-Release" to true)
    }
    from(compileJava9) {
        into("META-INF/versions/9/")
    }
}
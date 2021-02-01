import org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    java
    kotlin("jvm") version "1.4.21"
    `maven-publish` // Jitpack
    id("org.jetbrains.dokka") version "1.4.20"
}

val group = "com.github.kotlin_graphics"
val moduleName = "$group.kotlin_unsigned"
val kotestVersion = "4.3.2"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
}

tasks {

    dokkaHtml {
        dokkaSourceSets.configureEach {
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/kotlin-graphics/kotlin-unsigned/tree/master/src/main/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }

    withType<KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs += listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
        }
        sourceCompatibility = "1.8"
    }

    withType<Test> { useJUnitPlatform() }

    javadoc { dependsOn(dokkaJavadoc) }
}

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.get().outputDirectory.get())
    archiveClassifier.set("javadoc")
}

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.get().outputDirectory.get())
    archiveClassifier.set("html-doc")
}

artifacts {
    archives(dokkaJavadocJar)
    archives(dokkaHtmlJar)
}

publishing.publications.register("mavenJava", MavenPublication::class) {
    from(components["java"])
}

configurations.all { attributes.attribute(TARGET_JVM_VERSION_ATTRIBUTE, 8) }

java.withSourcesJar()
plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.0"
}

group = "bid.yuanlu.chatLog"
version = "velocity-1.1.5"
description = "yuanlu's velocity plugin: chat log"

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly(libs.com.velocitypowered.velocity.api) // https://docs.papermc.io/velocity/dev/creating-your-first-plugin
    annotationProcessor(libs.com.velocitypowered.velocity.api)
    compileOnly(libs.org.projectlombok.lombok) // https://mvnrepository.com/artifact/org.projectlombok/lombok
    annotationProcessor(libs.org.projectlombok.lombok)
    api(libs.org.bstats.bstats.velocity) // https://bstats.org/getting-started/include-metrics
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.build.configure {
    dependsOn("shadowJar")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName = "${rootProject.name}-${project.version}.${archiveExtension.get()}"
    exclude("META-INF/**")
    minimize()
    relocate("org.bstats", "bid.yuanlu.chatLog.libs.bstats")
}

publishing {
    repositories {
        maven {
            name = "yl-yuanlu-mcsp-main"
            url = uri("https://yl-yuanlu-maven.pkg.coding.net/repository/mcsp/main/")
        }
    }
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

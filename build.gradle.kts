plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.game"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jmonkeyengine:jme3-core:3.8.1-stable")
    implementation("org.jmonkeyengine:jme3-desktop:3.8.1-stable")
    implementation("org.jmonkeyengine:jme3-lwjgl3:3.8.1-stable")
    implementation("org.jmonkeyengine:jme3-effects:3.8.1-stable")
    implementation("org.jmonkeyengine:jme3-plugins:3.8.1-stable")
    implementation("org.jmonkeyengine:jme3-jogg:3.8.1-stable")
}

application {
    mainClass.set("org.game.Main")
}
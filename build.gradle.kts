plugins {
    id("java")
}

group = "org.game"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jmonkeyengine:jme3-core:3.8.1-stable")
    implementation("org.jmonkeyengine:jme3-desktop:3.8.1-stable")
    implementation("org.jmonkeyengine:jme3-lwjgl3:3.8.1-stable")
    implementation("org.jmonkeyengine:jme3-effects:3.8.1-stable")
    implementation("org.jmonkeyengine:jme3-plugins:3.8.1-stable")
    implementation("org.jmonkeyengine:jme3-jogg:3.8.1-stable")
//    implementation("org.jmonkeyengine:jme3-bullet:3.8.1-stable")
//    implementation("org.jmonkeyengine:jme3-bullet-native:3.8.1-stable")
}

tasks.test {
    useJUnitPlatform()
}
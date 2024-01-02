plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
}

group = "net.jasonly027"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    implementation("net.dv8tion:JDA:5.0.0-beta.18")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.jasonly027.steamsalebot.App"
    }
}
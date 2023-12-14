plugins {
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

    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
}

tasks.test {
    useJUnitPlatform()
}
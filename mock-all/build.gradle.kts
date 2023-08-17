plugins {
    kotlin("jvm") version "1.9.0"
    `java-library`
    jacoco
}

group = "au.kilemon"
version = "0.1.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/javax.annotation/javax.annotation-api
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    // https://mvnrepository.com/artifact/jakarta.annotation/jakarta.annotation-api
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")

    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test
    implementation("org.springframework.boot:spring-boot-starter-test:3.1.2")

    testImplementation("org.mockito:mockito-inline:5.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

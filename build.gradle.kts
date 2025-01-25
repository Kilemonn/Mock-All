plugins {
    kotlin("jvm") version "2.0.0"
    `java-library`
    `maven-publish`
    jacoco
}

group = "com.github.Kilemonn"
version = "0.1.6" // Make sure this version matches the release version in the repo
java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = rootProject.name
            version = version

            from(components["java"])

            pom {
                name.set("Mock All")
                description.set("An extension library built on-top of mockito used to quickly mock all injected beans.")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("kilemonn")
                    }
                }
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/javax.annotation/javax.annotation-api
    // Needed to support legacy Resource annotation
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    // https://mvnrepository.com/artifact/jakarta.annotation/jakarta.annotation-api
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")

    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test
    implementation("org.springframework.boot:spring-boot-starter-test:3.3.0")

    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED")
    finalizedBy(tasks.jacocoTestReport)
}

tasks.withType<Jar> {
    from(sourceSets["main"].allSource)
}

kotlin {
    jvmToolchain(17)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Generate the report after the tests
    reports {
        xml.required.set(false)
        csv.required.set(true)
    }
}

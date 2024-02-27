plugins {
    kotlin("jvm") version "1.9.0"
    `java-library`
    `maven-publish`
    jacoco
}

group = "com.github.Kilemonn"
version = "0.1.0"
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
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED")
}

tasks.withType<Jar> {
    from(sourceSets["main"].allSource)
}

kotlin {
    jvmToolchain(17)
}

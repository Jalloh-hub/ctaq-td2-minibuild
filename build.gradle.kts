plugins {
    application
    jacoco
}

group = "org.acme"
version = "0.1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // JUnit 5 (Jupiter) : exécution des tests (inclut junit-jupiter-params)
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Hamcrest 2.2 : matchers pour des assertions lisibles
    testImplementation("org.hamcrest:hamcrest:2.2")

    // Mockito 5.14.2 : doublures de test
    testImplementation("org.mockito:mockito-core:5.14.2")
}

application {
    mainClass.set("org.acme.minibuild.App")
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(false)
        csv.required.set(false)
    }
}
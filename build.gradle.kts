/*
 * build.gradle.kts
 * ----------------
 * Archivo principal de build del proyecto.
 *
 * Plugins:
 *   - java               → compila código Java
 *   - spring-boot         → empaqueta como JAR ejecutable y configura bootRun
 *   - dependency-management → alinea versiones de dependencias de Spring
 *
 * Dependencias clave:
 *   - spring-boot-starter-web      → REST controllers, embedded Tomcat
 *   - spring-boot-starter-data-jpa → JPA/Hibernate para persistencia
 *   - mysql-connector-j            → driver JDBC de MySQL (solo en runtime)
 */

plugins {
    java
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}

springBoot {
    mainClass.set("com.example.interviewback.InterviewBackApplication")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        // Mantenemos Java 26 como pide el profe
        languageVersion = JavaLanguageVersion.of(26)
    }
}

tasks.withType<JavaCompile> {
    // TRUCO: Le decimos al compilador de Java 26 que genere bytecode de Java 21.
    // Esto evita que el escáner ASM de Spring tire "Unsupported class file major version 70"
    options.release.set(21)
}

repositories {
    mavenCentral()
}

dependencies {
    // --- Spring Boot starters ---
    // Web: habilita REST controllers, serialización JSON (Jackson), Tomcat embebido
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Data JPA: integra Hibernate como ORM + Spring Data repositories
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // --- Base de datos ---
    // Driver MySQL: solo se necesita en runtime, no en compilación
    runtimeOnly("com.mysql:mysql-connector-j")

    // --- Seguridad ---
    // Spring Security: BCrypt, filtros HTTP, autenticación
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Validación de DTOs: @NotBlank, @Email, @Size, etc.
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JWT: generación y validación de tokens firmados con HS256
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // --- Testing ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

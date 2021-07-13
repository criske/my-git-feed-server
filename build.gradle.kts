import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktlint by configurations.creating

plugins {
    java
    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.spring") version "1.4.30"
}

group = "pcf.crskdev"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.ehcache:ehcache:3.9.2")
    implementation("com.giffing.bucket4j.spring.boot.starter:bucket4j-spring-boot-starter:0.3.3")
    val springSec = "5.4.5"
    implementation(group = "org.springframework.security", name = "spring-security-config", version = springSec)
    implementation(group = "org.springframework.security", name = "spring-security-web", version = springSec)
    implementation(group = "net.logstash.logback", name = "logstash-logback-encoder", version = "6.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(group = "redis.clients", name = "jedis", version = "3.5.1")
    implementation("com.github.criske:inval-id:1.0")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    ktlint("com.pinterest:ktlint:0.41.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    val kotest = "4.4.3"
    testImplementation("io.kotest:kotest-runner-junit5:${kotest}") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core:${kotest}")
    testImplementation("io.kotest:kotest-extensions-spring:${kotest}")
    testImplementation(group = "it.ozimov", name = "embedded-redis", version = "0.7.3")
    testImplementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    //  testImplementation(group = "org.slf4j", name = "slf4j-simple", version = "1.7.30")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0") {
        exclude("com.squareup.okhttp3", "okhttp")
    }
    testImplementation("com.squareup.okhttp3:okhttp:4.9.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    environment(mapOf(
        "GH_TOKEN" to "gh_fake_123",
        "GL_TOKEN" to "gl_fake_123",
        "BB_TOKEN" to "bb_fake_123",
        "REDIS_URL" to "redis://localhost:6379",
        "ADMIN_USER" to "test_user",
        "ADMIN_PASSWORD" to "fake_password"
    ))
}

tasks.register<Copy>("copyReactBuild") {
    from(file("../my-git-feed-client/build"))
    into(file("$buildDir/resources/main/static/"))
}

tasks.register<JavaExec>("ktlint") {
    group = "verification"
    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args("src/**/*.kt")
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Fix Kotlin code style deviations."
    main = "com.pinterest.ktlint.Main"
    classpath = ktlint
    args("-F", "src/**/*.kt")
}

tasks.named("check") {
    dependsOn(ktlint)
}



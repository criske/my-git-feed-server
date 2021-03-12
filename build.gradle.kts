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
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	ktlint("com.pinterest:ktlint:0.40.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	val kotest = "4.2.5"
	testImplementation("io.kotest:kotest-runner-junit5:${kotest}") // for kotest framework
	testImplementation("io.kotest:kotest-assertions-core:${kotest}")
	testImplementation("io.kotest:kotest-extensions-spring:${kotest}")
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

tasks.register<Copy>("copyReactBuild") {
	from(file("../my-git-feed-client/build"))
	into(file("$buildDir/resources/main/static/"))
}

tasks.register<JavaExec>("ktlint") {
	group = "verification"
	description = "Check Kotlin code style."
	classpath = ktlint
	main = "com.pinterest.ktlint.Main"
	args("src/main/**/*.kt")
}

tasks.register<JavaExec>("ktlintFormat"){
	group = "formatting"
	description = "Fix Kotlin code style deviations."
	main = "com.pinterest.ktlint.Main"
	classpath = ktlint
	args("-F", "src/**/*.kt")
}

tasks.named("check") {
	dependsOn(ktlint)
}



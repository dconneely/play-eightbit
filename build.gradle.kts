plugins {
    java
    `java-test-fixtures`
    application
    id("com.diffplug.spotless") version "8.10.4"
}

group = "com.davidconneely"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "com.davidconneely.eightbit.zx81.ZX81Machine"
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

dependencies {
    implementation("org.fusesource.jansi:jansi:2.4.3")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runScript") {
    description = "Runs the ZX81 emulator with keys typed from a script, and prints the screen."
    classpath = sourceSets["testFixtures"].runtimeClasspath
    mainClass = "com.davidconneely.eightbit.zx81.ScriptedZX81"
    jvmArgs("-Dstdout.encoding=UTF-8")
}

tasks.jar {
    archiveBaseName = "eightbit"
    manifest {
        attributes("Main-Class" to "com.davidconneely.eightbit.zx81.ZX81Machine")
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

spotless {
    java {
        googleJavaFormat()
    }
}

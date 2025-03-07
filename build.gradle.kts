plugins {
    kotlin("jvm") version "2.1.10"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.3.0")
    implementation ("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation(kotlin("stdlib"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.example.bmi_bot")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "com.example.bmi_bot"
        )
    }
}

// Ensure the source sets are included
tasks.withType<Jar> {
    from(sourceSets.main.get().output)
}
plugins {
    val kotlinVersion = "1.7.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    application
}

group = "com.sirekanyan"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.telegram:telegrambots:6.1.0")
    implementation("io.ktor:ktor-client-cio:2.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.40.1")
    implementation("org.jetbrains.lets-plot:lets-plot-common:2.5.0")
    implementation("org.jetbrains.lets-plot:lets-plot-image-export:2.5.0")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.1.0")
    implementation("org.postgresql:postgresql:42.5.0")
    implementation("org.slf4j:slf4j-simple:2.0.3")
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass.set("com.sirekanyan.andersrobot.Main")
    if (hasProperty("debug")) {
        applicationDefaultJvmArgs = listOf("-Ddebug")
    }
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            allWarningsAsErrors = true
        }
    }
    jar {
        manifest.attributes["Main-Class"] = "com.sirekanyan.andersrobot.Main"
        from(configurations.runtimeClasspath.get().map(::zipTree))
        doLast {
            File("bot").also { file ->
                file.createNewFile()
                file.setExecutable(true)
                file.printWriter().use { writer ->
                    writer.println("#!/usr/bin/env sh")
                    writer.println("java -jar ${archiveFileName.get()} \"$@\"")
                }
            }
        }
    }
}
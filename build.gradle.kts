plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.2"
}

group = "dev.emortal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom-snapshots:d0754f2a15")
    implementation("dev.hollowcube:polar:1.11.3")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Minestom has a minimum Java version of 21
    }
}


tasks {
    jar {
        manifest.attributes["Main-Class"] = "dev.emortal.lidar.Main"
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix on the shadowjar file.
    }
}
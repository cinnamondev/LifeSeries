import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("com.gradleup.shadow") version "9.2.2"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven { url = uri("https://repo.opencollab.dev/main/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://repo.md-5.net/content/groups/public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
}

dependencies {
    implementation("com.google.inject:guice:7.0.0")
    implementation("org.apache.commons:commons-collections4:4.6.0")
    paperweight.paperDevBundle("26.2.build.+")
    //compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

group = "com.github.cinnamondev"
version = "1.0-SNAPSHOT"
description = "life series."
java {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release = 25
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    runServer {
        minecraftVersion("26.2")
    }
}

paperPluginYaml {
    main = "com.github.cinnamondev.lifeSeries.LifeSeries"
    apiVersion = "26.2"
    authors.add("cinnamondev")
    dependencies {
        server("LibsDisguises", PaperPluginYaml.Load.BEFORE, false)
    }
}


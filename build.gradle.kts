import xyz.jpenilla.resourcefactory.bukkit.Permission
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
    paperweight.paperDevBundle("26.3.build.+")
    //compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

group = "com.github.cinnamondev"
version = "1.0-SNAPSHOT"
description = "Life Series Plugin"
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
        minecraftVersion("26.3")
        downloadPlugins {
            modrinth("luckperms", "v5.5.71-bukkit")
        }
    }
}

paperPluginYaml {
    main = "com.github.cinnamondev.lifeSeries.LifeSeries"
    apiVersion = "26.2"
    authors.add("cinnamondev")
    dependencies {
        server("LibsDisguises", PaperPluginYaml.Load.BEFORE, false)
    }

    permissions {
        register("life.admin") {
            description = "Admin commands"
            default = Permission.Default.OP
        }
        register("life.admin.game") {
            description = "Admin commands Game Specific"
            default = Permission.Default.OP
        }
        register("life.admin.revival") {
            description = "lf revival"
            default = Permission.Default.OP
        }
        register("life.admin.session") {
            description = "Admin commands for lf session"
            default = Permission.Default.OP
        }
        register("life.bypass.kick") {
            description = "end of session kick bypass"
            default = Permission.Default.OP
        }
        register("life.boogeyman") {
            description = "Am I The Boogeyman? Command"
            default = Permission.Default.TRUE
        }
        register("life.revival") {
            description = "revival base permission"
            default = Permission.Default.TRUE
        }
        register("life.revival.toggle-spawn-point") {
            description = "allow players to choose where revived players respawn"
            default = Permission.Default.FALSE
        }
        register("life.bypass.banned-enchantments") {
            description = "bypass enchant limits"
            default = Permission.Default.OP
        }
        register("life.bypass.banned-items") {
            description = "bypass item limits"
            default = Permission.Default.OP
        }
        register ("life.revival.clock-use-bypass") {
            description = "bypass clock use"
            default = Permission.Default.OP
        }

    }
}


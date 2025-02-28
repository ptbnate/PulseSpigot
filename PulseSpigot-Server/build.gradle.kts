
val minecraftVersion = "1_8_R3"

dependencies {
    implementation(project(":pulsespigot-api"))

    // Native (Minecraft) libraries start
    compileOnly("io.netty:netty-all:4.0.25.Final")
    // PulseSpigot start - updated log4j to 2.19.0
    val log4jVersion = "2.19.0"
    library("org.apache.logging.log4j:log4j-api:$log4jVersion")
    library("org.apache.logging.log4j:log4j-core:$log4jVersion")
    library("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion") // PulseSpigot - Backport Plugin#getSLF4JLogger
    // PulseSpigot end - updated log4j to 2.19.0
    library("commons-io:commons-io:2.4")
    library("commons-codec:commons-codec:1.9")
    library("net.sf.jopt-simple:jopt-simple:5.0") // PulseSpigot - updated jopt-simple to 5.0
    implementation("com.mojang:authlib:1.5.21") {
        // PulseSpigot start - use our versions of dependencies
        exclude("org.apache.commons")
        exclude("commons-io")
        exclude("commons-codec")
        exclude("org.apache.logging.log4j")
        exclude("com.google.code.gson")
        exclude("com.google.guava")
        // PulseSpigot end
    }
    library("org.xerial:sqlite-jdbc:3.7.2")
    compileOnly("mysql:mysql-connector-java:5.1.14")
    // Native libraries end

    // PulseSpigot libraries start
    // Paper start - Use TerminalConsoleAppender
    library("net.minecrell:terminalconsoleappender:1.3.0")
    val jLineVersion = "3.21.0"
    library("org.jline:jline-reader:$jLineVersion")
    library("org.jline:jline-terminal:$jLineVersion")
    library("org.jline:jline-terminal-jansi:$jLineVersion")
    library("org.fusesource.jansi:jansi:2.4.0")
    // Paper end

    val byteBuddyVersion = "1.12.12"
    library("net.bytebuddy:byte-buddy-agent:$byteBuddyVersion")
    library("net.bytebuddy:byte-buddy:$byteBuddyVersion")

    val okaeriConfigsVersion = "5.0.0-beta.6"
    implementation("eu.okaeri:okaeri-configs-core:$okaeriConfigsVersion")
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:$okaeriConfigsVersion")
    implementation("eu.okaeri:okaeri-configs-serdes-commons:$okaeriConfigsVersion")
    implementation("eu.okaeri:okaeri-configs-validator-okaeri:$okaeriConfigsVersion")

    implementation("com.velocitypowered:velocity-native:1.1.9")
    library("com.github.ben-manes.caffeine:caffeine:2.9.3")
    library("it.unimi.dsi:fastutil:8.5.11")
    library("com.eatthepath:fast-uuid:0.2.0")
    // PulseSpigot libraries end
}

tasks {
    shadowJar {
        mergeServiceFiles()
        transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer()) // Paper - Use TerminalConsoleAppender
        archiveClassifier.set("") // without "-all"

        val libsPackage = "me.nate.spigot.libs"
        relocate("eu.okaeri", "$libsPackage.eu.okaeri")

        relocate("org.bukkit.craftbukkit", "org.bukkit.craftbukkit.v${minecraftVersion}") {
            exclude("org.bukkit.craftbukkit.Main*") // don't relocate main class
        }
        relocate("net.minecraft.server", "net.minecraft.server.v${minecraftVersion}")
    }

    named("build") {
        dependsOn(named("shadowJar"))
    }

    test {
        exclude("org/bukkit/craftbukkit/inventory/ItemStack*Test.class", "org/bukkit/craftbukkit/inventory/ItemFactoryTest.class")
    }

    jar {
        archiveClassifier.set("original")
        manifest {
            val gitHash = project.parent!!.ext["gitHash"]
            attributes(
                    "Main-Class" to "org.bukkit.craftbukkit.Main",
                    "Implementation-Title" to "CraftBukkit",
                    "Implementation-Version" to "git-PulseSpigot-$gitHash",
                    "Implementation-Vendor" to "Bukkit Team",
                    "Specification-Title" to "Bukkit",
                    "Specification-Version" to project.version,
                    "Specification-Vendor" to "Bukkit Team",
                    "Multi-Release" to "true"
            )
        }
    }
}
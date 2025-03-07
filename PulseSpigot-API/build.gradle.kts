java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    // Native (Minecraft) libraries start
    api("com.google.guava:guava:17.0") // Loaded by CerberusLoader
    api("com.google.code.gson:gson:2.2.4") // Loaded by CerberusLoader
    api("org.yaml:snakeyaml:1.15") // Loaded by CerberusLoader
    api("com.googlecode.json-simple:json-simple:1.1.1")
    api("commons-lang:commons-lang:2.6")
    api("org.avaje:ebean:2.8.1")
    api("net.sf.trove4j:trove4j:3.0.3")
    api("net.md-5:bungeecord-chat:1.8-SNAPSHOT")
    // Native libraries end

    // PulseSpigot libraries start
    api("org.apache.commons:commons-lang3:3.12.0") // PulseSpigot - updated commons-lang3 to 3.12.0 // PulseSpigot - Add commons-lang3 to API
    api("org.slf4j:slf4j-api:1.7.36") // Backport Plugin#getSLF4JLogger
    api("org.jetbrains:annotations:24.0.1") // PulseSpigot - add jetbrains annotations - future is now 😎

    // Paper start - Use ASM for event executors
    api("org.ow2.asm:asm:9.4")
    api("org.ow2.asm:asm-commons:9.4")
    // Paper end
    // PulseSpigot libraries end
}

tasks {
    val generateApiVersioningFile by registering {
        inputs.property("version", project.version)
        val pomProps = layout.buildDirectory.file("pom.properties")
        outputs.file(pomProps)
        doLast {
            pomProps.get().asFile.writeText("version=${project.version}")
        }
    }

    jar {
        from(generateApiVersioningFile.map { it.outputs.files.singleFile }) {
            into("META-INF/maven/${project.group}/${project.name.lowercase()}")
        }

        manifest {
            attributes(
                "Automatic-Module-Name" to "org.bukkit"
            )
        }
    }

    withType<Javadoc> {
        (options as StandardJavadocDocletOptions).let {
            // hide warnings
            it.addBooleanOption("Xdoclint:none", true)
            it.addStringOption("Xmaxwarns", "1")

            it.links(
                "https://guava.dev/releases/17.0/api/docs/",
                "https://javadoc.io/doc/org.yaml/snakeyaml/1.15/",
                "https://javadoc.io/doc/net.md-5/bungeecord-chat/1.16-R0.4/",
            )
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.github.ptbnate"
            artifactId = "pulsespigot-api"
            version = "1.8.8-R0.1-SNAPSHOT"
        }
    }
}

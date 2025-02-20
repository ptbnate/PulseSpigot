import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = "xyz.krypton.spigot"
    version = "1.8.8-R0.1-SNAPSHOT"

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    repositories {
        mavenCentral()
        maven(url = "https://libraries.minecraft.net")
        maven(url = "https://repo.titanvale.net/releases")
        maven(url = "https://repo.titanvale.net/snapshots")
        maven(url = "https://storehouse.okaeri.eu/repository/maven-public")
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

    val cerberusLibraries = mutableSetOf<String>()
    ext["cerberusLibraries"] = cerberusLibraries

    val library by configurations.creating
    val libraryApi by configurations.creating

    afterEvaluate {
        dependencies {
            library.dependencies.forEach {
                compileOnly(it)
                cerberusLibraries.add("${it.group}:${it.name}:${it.version}")
            }
            libraryApi.dependencies.forEach {
                compileOnlyApi(it)
                cerberusLibraries.add("${it.group}:${it.name}:${it.version}")
            }
        }

        tasks.withType<ShadowJar> {
            // include cerberus libraries as file
            buildDir.mkdirs()
            val librariesFile = File(buildDir, "libraries.cerberus")
            if (!librariesFile.exists()) {
                librariesFile.createNewFile()
            }

            val resultDependencies = LinkedHashSet<String>(cerberusLibraries)
            this.project.configurations.stream()
                    .flatMap { it.allDependencies.stream() }
                    .filter { it is ProjectDependency }
                    .map { it as ProjectDependency }
                    .map { it.dependencyProject.project }
                    .distinct()
                    .map { it.ext }
                    .forEachOrdered { extra ->
                        if (!extra.has("cerberusLibraries")) {
                            return@forEachOrdered
                        }
                        resultDependencies += extra["cerberusLibraries"] as MutableSet<String>
                    }
            librariesFile.writeText(resultDependencies.joinToString("\n"))
            from(librariesFile)
        }
    }

}

ext["gitHash"] = project.getCurrentGitHash()

fun Project.getCurrentGitHash(): String {
    val result = exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }

    return result.toString().trim()
}

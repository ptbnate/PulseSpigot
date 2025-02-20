import java.util.*

rootProject.name = "PulseSpigot"
val projectName: String = rootProject.name;

this.setupSubproject("${projectName}-Server")
this.setupSubproject("${projectName}-API")

fun setupSubproject(name: String, dir: String) {
    include(":$name")
    project(":$name").projectDir = file(dir)
}

fun setupSubproject(dir: String) {
    val name = dir.lowercase(Locale.ROOT);
    this.setupSubproject(name, dir)
}
@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.concurrent.thread
import kotlin.reflect.KProperty

plugins {
    idea
    eclipse
    `maven-publish`
    java
    kotlin("jvm")
    id("net.minecraftforge.gradle")
}

val mc_version: String by gradleProperties
val forge_version: String by gradleProperties
val mc_mappings: String by gradleProperties

val branch: String = gradleProperties["branch"] ?: "git rev-parse --abbrev-ref HEAD".execute(rootDir.absolutePath).lines().last()
logger.info("On branch $branch")

val mod_version_prefix = if(mc_version.contains(branch)) "" else "${branch.replace('/', '-')}-"

val mod_version: String by gradleProperties
val mod_name: String by gradleProperties
val mod_group: String by gradleProperties

// ===================================================== Common ===================================================== //

allprojects {
    apply(plugin = "idea")
    apply(plugin = "eclipse")
    apply(plugin = "maven-publish")
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "net.minecraftforge.gradle")


    group = mod_group
    version = mod_version_prefix + mod_version
    if(project == rootProject)
        base.archivesBaseName = "$mod_name-$mod_version_prefix$mc_version"
    else
        base.archivesBaseName = "librarianlib-${project.name}"

    minecraft {
        mappings = "$mc_mappings-$mc_version"
    }

    repositories {
        maven(url = "http://maven.shadowfacts.net/")
        maven(url = "https://jitpack.io")
    }

    dependencies {
        minecraft("net.minecraftforge:forge:$mc_version-$forge_version")
        compile(kotlin("stdlib-jdk8"))
    }

    tasks.getByName<Jar>("jar") {
        manifest {
//            attributes(mapOf(
//                "Specification-Title" to mod_name,
//                "Specification-Vendor" to "Team Wizardry",
//                "Specification-Version" to version,
//                "Implementation-Title" to project.name,
//                "Implementation-Version" to version,
//                "Implementation-Vendor" to "examplemodsareus",
//                "Implementation-Timestamp" to DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(LocalDateTime.now(ZoneId.of("UTC")))
//            ))
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceSets {
            sourceSets["test"].compileClasspath += sourceSets["main"].compileClasspath
            sourceSets["test"].runtimeClasspath += sourceSets["main"].runtimeClasspath
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            javaParameters = true
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xjvm-default=enable",
                "-Xuse-experimental=kotlin.Experimental"
            )
        }
        destinationDir = File(destinationDir.absolutePath.replace("kotlin/([^/]+)$".toRegex(), "java/$1"))
    }
}

// ====================================================== Root ====================================================== //

dependencies {
    subprojects.forEach {
        compileOnly(it.java.sourceSets["main"].output)
        compileOnly(it.java.sourceSets["test"].output)
    }
}

minecraft {
    runs {
        "client" {
            workingDirectory(project.file("run"))
            isSingleInstance = true

            // Recommended logging data for a userdev environment
//            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")

            // Recommended logging level for the console
            property("forge.logging.console.level", "debug")

            mods {
                "librarianlib" {
                    source(java.sourceSets["main"])
                }
                "librarianlib-testmod" {
                    source(java.sourceSets["test"])
                }
                subprojects.forEach {
                    "librarianlib-${it.name}" {
                        source(it.java.sourceSets["main"])
                    }
                    "librarianlib-${it.name}-testmod" {
                        source(it.java.sourceSets["test"])
                    }
                }
            }
        }

        "server" {
            workingDirectory(project.file("run"))
            isSingleInstance = true

            // Recommended logging data for a userdev environment
//            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")

            // Recommended logging level for the console
            property("forge.logging.console.level", "debug")

            mods {
                "librarianlib" {
                    source(java.sourceSets["main"])
                }
                "librarianlib-testmod" {
                    source(java.sourceSets["test"])
                }
                subprojects.forEach {
                    "librarianlib-${it.name}" {
                        source(it.java.sourceSets["main"])
                    }
                    "librarianlib-${it.name}-testmod" {
                        source(it.java.sourceSets["test"])
                    }
                }
            }
        }
    }
}

// ==================================================== Utilities =================================================== //

fun String.execute(wd: String? = null, ignoreExitCode: Boolean = false): String =
    split(" ").execute(wd, ignoreExitCode)

fun List<String>.execute(wd: String? = null, ignoreExitCode: Boolean = false): String {
    val process = ProcessBuilder(this)
        .also { pb -> wd?.let { pb.directory(File(it)) } }
        .start()
    var result = ""
    val errReader = thread { process.errorStream.bufferedReader().forEachLine { logger.error(it) } }
    val outReader = thread {
        process.inputStream.bufferedReader().forEachLine { line ->
            logger.debug(line)
            result += line
        }
    }
    process.waitFor()
    outReader.join()
    errReader.join()
    if (process.exitValue() != 0 && !ignoreExitCode) error("Non-zero exit status for `$this`")
    return result
}

val Project.gradleProperties: ProjectPropertiesDelegate
    get() = ProjectPropertiesDelegate(this)

@Suppress("UNCHECKED_CAST")
class ProjectPropertiesDelegate(private val project: Project) {
    operator fun <T> getValue(receiver: Any?, property: KProperty<*>): T = this[property.name]
    operator fun <T> get(name: String): T = project.extra.properties[name] as T
    operator fun contains(name: String): Boolean = project.extra.properties[name] != null
}
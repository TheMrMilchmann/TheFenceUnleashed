/*
 * Copyright (c) 2021-2022 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import com.github.themrmilchmann.fency.build.*
import com.github.themrmilchmann.fency.build.BuildType
import io.github.themrmilchmann.gradle.publish.curseforge.*
import io.github.themrmilchmann.gradle.publish.curseforge.tasks.*

plugins {
    java
    id("net.minecraftforge.gradle") version "5.1.52"
    /*
     * TODO:
     *  The build should really not depend on a snapshot of some library or Gradle plugin but - for some reason - there
     *  was no MixinGradle release for an extremely long time. I might have to look into forking this to at least get a
     *  somewhat reproducible build.
     */
    id("org.spongepowered.mixin") version "0.7-SNAPSHOT"
    id("io.github.themrmilchmann.curseforge-publish") version "0.2.0"
}

group = "com.github.themrmilchmann.fency"
val nextVersion = "1.0.2-1.19.2-1.1"
version = when (deployment.type) {
    BuildType.SNAPSHOT -> "$nextVersion-SNAPSHOT"
    else -> nextVersion
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

minecraft {
    mappings("official", "1.19.2")

    runs {
        create("client") {
            workingDirectory(file("run"))

            property("forge.logging.console.level", "debug")
            property("forge.logging.markers", "REGISTRIES")

            arg("-mixin.config=fency.mixins.json")
            jvmArg("-Dmixin.env.disableRefMap=true")

            mods {
                create("fency") {
                    source(sourceSets["main"])
                }
            }
        }
        create("server") {
            workingDirectory(file("run"))

            property("forge.logging.console.level", "debug")
            property("forge.logging.markers", "REGISTRIES")

            arg("-mixin.config=fency.mixins.json")
            jvmArg("-Dmixin.env.disableRefMap=true")

            mods {
                create("fency") {
                    source(sourceSets["main"])
                }
            }
        }
    }
}

mixin {
    add(sourceSets["main"], "fency.refmap.json")
}

tasks {
    compileJava {
        options.release.set(17)
    }

    jar {
        manifest {
            attributes(mapOf(
                "MixinConfigs" to "fency.mixins.json"
            ))
        }
    }

    withType<Jar>().configureEach {
        archiveBaseName.set("TheFenceUnleashed")
    }

    withType<JavaExec>().configureEach {
        doFirst { // Workaround for https://github.com/MinecraftForge/ForgeGradle/pull/889
            javaLauncher.set(project.javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(17)) })
        }
    }

    withType<PublishToCurseForgeRepository>().configureEach {
        onlyIf { deployment.type === BuildType.RELEASE }
    }
}

publishing {
    repositories {
        curseForge {
            apiKey.set(deployment.cfApiKey)
        }
    }
    publications {
        create<CurseForgePublication>("curseForge") {
            projectID.set(521072) // https://www.curseforge.com/minecraft/mc-mods/the-fence-unleashed

            artifact {
                changelog = changelog()
                displayName = "The Fence Unleashed ${project.version}"
                releaseType = ReleaseType.RELEASE
            }
        }
    }
}

fun changelog(): Changelog {
    if (deployment.type == BuildType.SNAPSHOT) return Changelog("", ChangelogType.TEXT)

    val mc = project.version.toString() // E.g. 1.0.0-1.16.5-1.0
        .substringAfter('-')            //            1.16.5-1.0
        .substringBefore('-')           //            1.16.5
        .let {
            if (it.count { it == '.' } == 1)
                it
            else
                it.substringBeforeLast('.')
        }                               //            1.16

    return Changelog(
        content = File(rootDir, "docs/changelog/$mc/${project.version}.md").readText(),
        type = ChangelogType.MARKDOWN
    )
}

dependencies {
    minecraft("net.minecraftforge:forge:1.19.2-43.1.1")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}
/*
 * Copyright (c) 2021-2023 Leon Linhart
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
import io.github.themrmilchmann.gradle.publish.curseforge.*

plugins {
    alias(libs.plugins.forge)
    alias(libs.plugins.mixin)
    id("io.github.themrmilchmann.java-conventions")
    id("io.github.themrmilchmann.curseforge-publish-conventions")
}

minecraft {
    mappings(provider { "official" }, libs.versions.minecraft)

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
    jar {
        manifest {
            attributes(mapOf(
                "MixinConfigs" to "fency.mixins.json"
            ))
        }
    }
}

publishing {
    publications {
        register<CurseForgePublication>("curseForge") {
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
    val modVersionSegment = version.toString().substringBefore('-')
    val mcVersionSegment = version.toString().substring(startIndex = modVersionSegment.length + 1).substringBefore('-')
    val mcVersionGroup = if (mcVersionSegment.count { it == '.' } == 1) mcVersionSegment else mcVersionSegment.substringBeforeLast('.')
    val loaderVersionSegment = version.toString().substring(startIndex = modVersionSegment.length + mcVersionSegment.length + 2)

    return Changelog(
        content = File(rootDir, "docs/changelog/$mcVersionGroup/${modVersionSegment}-${mcVersionSegment}-${loaderVersionSegment}.md").readText(),
        type = ChangelogType.MARKDOWN
    )
}

dependencies {
    minecraft(libs.minecraftforge)

    annotationProcessor(libs.mixin) {
        artifact { classifier = "processor" }
    }
}
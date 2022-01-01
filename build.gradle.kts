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

plugins {
    java
    id("net.minecraftforge.gradle") version "5.1.0"
    /*
     * TODO:
     *  The build should really not depend on a snapshot of some library or Gradle plugin but - for some reason - there
     *  was no MixinGradle release for an extremely long time. I might have to look into forking this to at least get a
     *  somewhat reproducible build.
     */
    id("org.spongepowered.mixin") version "0.7-SNAPSHOT"
}

group = "com.github.themrmilchmann.fency"
val nextVersion = "1.16.5-1.0.0.0"
version = when (deployment.type) {
    BuildType.SNAPSHOT -> "$nextVersion-SNAPSHOT"
    else -> nextVersion
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

minecraft {
    mappings("official", "1.16.5")

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

dependencies {
    minecraft("net.minecraftforge:forge:1.16.5-36.2.2")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}
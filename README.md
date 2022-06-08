# The Fence Unleashed

**Have you ever been annoyed by that one cow that just didn't want to stay in
its pen and managed to get out just before you could close the fence gate?**

_The Fence Unleashed_ is a small QoL mod that helps in exactly this scenario by
preventing mobs that are not leashed from passing through fence gates. Mobs that
are leashed to a fence cannot pass either. The behavior of mobs that cannot be
leashed remains unchanged. If a mob is mounted, the properties of the topmost
mob will be considered. This behavior can be adjusted in the mod's configuration
file.

<p align="center">
  <img alt="Fency Banner" src="https://github.com/TheMrMilchmann/TheFenceUnleashed/blob/master/docs/banner.png">
</p>


## Versioning

The Fence Unleashed follows a custom versioning scheme. A version number matches
the pattern `WORLD.API.FEATURE-MCVERSION-PATCH`.


## Supported versions

| Minecraft Version | State              |
|-------------------|--------------------|
| 1.19              | Active Development |
| 1.18.2            | Mainline           |
| 1.17.1            | Active Development |
| 1.16.5            | Active Development |


### Support Cycle

| State                  | Description                                                   |
|------------------------|---------------------------------------------------------------|
| **Mainline**           | The primary development branch                                |
| **Active Development** | This version still receives all updates                       |
| **Fixes Only**         | This version still receives fixes but no new features         |
| **Unsupported**        | This version is unsupported and does not receive any updates  |


## Frequently Asked Questions
(Just kidding, we just released this. No one has asked for or about this.)

> Why is this mod called "The Fence Unleashed" when it actually improves fence gates?

Excellent question! Did you ever come up with a pun about fence gates? No? Yeah,
I didn't either. That's pretty much it... so I guess I'll add another random
fact here: The initial name for this mod was "Fency" (and this name is still
used internally) but it had to be changed for technical reasons.

> Is The Fence Unleashed compatible with fence gates added by other mods?

The Fence Unleashed modifies vanilla fence gate logic to be compatible with all
sorts of fence gates. Thus, it is very likely, but not guaranteed that it will
work with fence gates from other mods too.

> Can I use The Fence Unleashed in my modpack?

Yes, please respect the [license terms](./LICENSE).

> Can I use this mod with existing saves?

Yes, The Fence Unleashed does not affect savegames. It can be added or removed
at any time.

> Mob X from mod Y behaves weirdly with The Fence Unleashed. Is there anything I
> can do about this?

While The Fence Unleashed can usually correctly identify whether a mob can be
leashed and adjusts the behavior accordingly, some mods do not correctly
implement the required checks. To make sure that this gets fixed properly,
please file an [issue](https://github.com/TheMrMilchmann/TheFenceUnleashed/issues).
You do not have to wait for a fix, however. Fortunately, The Fence Unleashed
provides a configuration file in which its behavior can be adjusted.


## Building from source

### Setup

This project uses [Gradle's toolchain support](https://docs.gradle.org/7.4.2/userguide/toolchains.html)
to detect and select the JDKs required to run the build. Please refer to the
build scripts to find out which toolchains are requested.

An installed JDK 1.8 (or later) is required to use Gradle.

### Building

Once the setup is complete, invoke the respective Gradle tasks using the
following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the Java library
- `runClient`               - runs the development client
- `runServer`               - runs the development server

Additionally `tasks` may be used to print a list of all available tasks.


## License

```
Copyright (c) 2021-2022 Leon Linhart

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
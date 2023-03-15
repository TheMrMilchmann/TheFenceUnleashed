### 1.0.2-1.19.4-0.0

_Released 2023 Mar 15_

#### Requirements
- **MinecraftForge:** 1.19.4-45.0.1

#### Overview

- Updated to Minecraft 1.19.4


---

### 1.0.2-1.19.3-0.0

_Released 2022 Dec 08_

#### Requirements
- **MinecraftForge:** 1.19.3-44.0.0

#### Overview

- Updated to Minecraft 1.19.3


---

### 1.0.2-1.19.2-1.1

_Released 2022 Oct 22_

#### Requirements
- **MinecraftForge:** 1.19.2-43.1.1

#### Fixes

- Added MineColonies' visitors (`minecolonies:visitor`) to the default allowlist.
    - This will only take effect for new installations. If you wish to whitelist
      MineColonies' citizens to an existing installation, you will still have to
      add the exception manually.


---

### 1.0.2-1.19.2-1.0

_Released 2022 Oct 17_

#### Requirements
- **MinecraftForge:** 1.19.2-43.1.1

#### Improvements

- Added an advancement to inform players about the mod. [[GH-4](https://github.com/TheMrMilchmann/TheFenceUnleashed/issues/4)]
    - The advancement is granted the first time a player walks through a fence
      gate.
    - The advancement grants two leads upon unlock to make it easier to get
      started with the mod.
- Changed the artifact base name to "TheFenceUnleashed" (from "Fency") to avoid
  potential confusion.

#### Fixes

- Fixed a bug that caused the mod's resources to be displayed as incompatible
  with the current Minecraft version.
- Fixed a bug that caused carriage-return characters to be visible in the mod's
  description in some places.


---

### 1.0.1-1.19.2-1.0

_Released 2022 Aug 23_

#### Requirements
- **MinecraftForge:** 1.19.2-43.1.1

#### Improvements

- Added MineColonies' citizens (`minecolonies:citizen`) to the default allowlist.
    - This will only take effect for new installations. If you wish to whitelist
      MineColonies' citizens to an existing installation, you will still have to
      add the exception manually.
- Improved documentation for configuration options.

#### Fixes

- Updated an error message to avoid potential confusion.
- Updated the required Forge version indication to the correct requirement.


---

### 1.0.0-1.19-0

_Released 2022 Jun 08_

#### Requirements
- **MinecraftForge:** 1.19-41.0.1

#### Overview

**Have you ever been annoyed by that one cow that just didn't want to stay in its pen and managed to get out just before
you could close the fence gate?**

_The Fence Unleashed_ is a small QoL mod that helps in exactly this scenario by preventing mobs that are not leashed
from passing through fence gates. Mobs that are leashed to a fence cannot pass either. The behavior of mobs that cannot
be leashed remains unchanged. If a mob is mounted, the properties of the topmost mob will be considered.
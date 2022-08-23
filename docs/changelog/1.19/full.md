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
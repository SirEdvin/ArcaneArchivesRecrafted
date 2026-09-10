# Hive letter crafting policy

Source: pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`, `init/RecipeLibrary.java:84-115`.

The server branches use `getHiveByMember(player UUID)`. Invitation accepts absent membership or ownership; resignation accepts any membership (including owners); expulsion requires ownership. The client branches express the same rules using `inHive`/`ownsHive`. Do not invent a restriction preventing owners from crafting resignation letters.

`recipe/gct/HiveCraftingConditions.java` ports these three boolean policies without Minecraft bootstrap or loader dependencies. Supply the acting player's nonnull UUID and the current membership's owner UUID. Null owner means confirmed absence of membership ONLY. Unavailable/corrupt hive data must abort the caller's operation, not be converted into null. UUID comparison is by value. No caching, mutable singleton, assets, save/network format, or recipe cost/output change is introduced. This is a mechanical policy extraction, not a new gameplay rule or a replacement hive model.

Five tests cover unaffiliated players, ordinary members, owners (including owner resignation), successive membership changes, and invalid player input. Executed command:

`timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:test --tests '*HiveCraftingConditionsTest' assemble --no-daemon`

Exit 0 in 27s; full log `build/hive-conditions-20260907-222904.log`. XML totals: 83 tests each on Fabric 1.20.1, Fabric 1.21.1 and NeoForge 1.21.1, zero failures/errors/skips. Forge executes ONLY the five new pure-Java policy tests, all passing; its Minecraft-backed harness remains unresolved. Four artifact pairs and `git diff --check` pass.

Pending: authoritative hive persistence/lookup, player and block identity validation, runtime recipe conditions binding and preview-to-commit revalidation, actual letters and their effects, output/remainder conservation, and multiplayer tests. These helpers do not themselves authorize or perform crafting. The migration remains unfinished.

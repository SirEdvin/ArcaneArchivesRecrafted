# 0081 — Echo atlas and native source tint

Status: implemented atlas registration and source-provider tint priority; fallback cache implementation is now restored under approved [0104](0104-echo-ore-cache-compatibility.md). Connected visual acceptance remains open.

Original behavior: baseline `80944ce45c6559243d8928cc4b305bf379388652`, `proxy/ClientProxy.java:100–118`, colors only layer 1. Empty Echoes return -1. Source item tint indices 0, 1, 2 are queried in order; the first value other than -1 wins. Otherwise the upstream TintUtils cache is consulted. `util/TintUtils.java:87–164` generates that cache from configured ore-smelting results, sampling the first south-facing quad's source image at x/y 6–9. It is not a general all-item average-color algorithm.

Implementation: preserve that native provider ordering through loader-specific color registration, with source components intact. Register the three original legacy `items/echoes_layer*` paths as explicit modern block-atlas sources; merely packaging these images in 0080 did not establish atlas availability. No item mutation, new gameplay or dormant producer activation is introduced. This falls under the approved faithful migration scope on all four targets.

The former missing ore-smelting fallback/configuration is superseded by 0104. The fallback is still restricted to configured smelting results, not an arbitrary average for every Echo source. The original persistent cache is retained across resource reloads; connected rendering, animation and resource-reload acceptance remain open.

Verification: `timeout --foreground 10m ./gradlew assemble --offline --no-daemon`, exit 0, 20 seconds, `build/echo-color-final-20260909-175940.log`; all four targets assemble. Initial run `build/echo-color-20260909-175838.log`, exit 1, 12 seconds, exposed an unavailable vanilla Minecraft.getItemColors getter on Fabric. Corrected registration to use Fabric ColorProviderRegistry and Forge/NeoForge event-provided ItemColors; no access widening or reflective workaround was added. No new gameplay or visual tests were run.

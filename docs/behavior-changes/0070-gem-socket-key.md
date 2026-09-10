# Gem Socket opening key

## Original behavior

Pinned bb99accf48ed583e29b0efae56e28c963407b8df `client/Keybinds.java` registers an unbound socket key only with EnableArsenal. While gameplay has focus it sends `PacketArcaneGems.OpenSocket`; the server resolves the socket through GemSocketHandler.findSocket. This is a menu-opening key, NOT a recharge key. Original English/Portuguese key labels and categories recovered from the pinned language files.

## Implemented behavior

All four targets register the native configurable key, initially unbound, labeled Activate Gem Socket in Controls under Arcane Archives (Portuguese category: Arquivos Arcanos). Client registration remains Arsenal-gated. Focused gameplay input sends an empty, serverbound packet. Native receiver branches enqueue work on the server thread; the server finds the actual main-hand, equipped or inventory socket using existing priority and opens the persistent menu. No item identity/location supplied by the client is trusted.

Right-click and key opening share GemSocketItem.open. Requests require a live nonspectator player on the server thread with the ordinary inventory menu active; requests while another container is open are ignored, preventing repeated packets from closing/reopening menus. The client coalesces queued presses in a tick and does not send while a screen is open or the window lacks focus. These are native input/authority safety adaptations, not a new balance policy. Existing recharge-button logic and worn/open-menu precedence remain unchanged.

## Approval / scope

Restoration of the pinned socket-opening control; no new default key or recharge hotkey proposed. No additional gameplay decision required. Manifest key functionality is separate and is not implemented by this socket checkpoint. HUD, upgrades, remaining shared presentation and broader migration are still open.

## Verification

`timeout --foreground 10m ./gradlew assemble --no-daemon`: exit 0, 22s, log `build/socket-key-20260909-142942.log`; all four compileJava tasks present, Gradle success in 21s. `git diff --check` passed. All four production JARs contain GemSocketKey/OpenGemSocket and key localization. Full artifact verifier not run; expectations extended. Gameplay key-binding, input-mod compatibility and menu lifecycle acceptance remain deferred; no client/server launch, gameplay tests or optimization performed. Migration remains incomplete and not first-playtest-ready.

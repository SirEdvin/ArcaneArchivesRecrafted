# Manifest hover-item shortcut

## Original behavior

At development pin `80944ce45c6559243d8928cc4b305bf379388652`, `client/Keybinds.java:56–72,105–136` queries JEI overlays/recipes/bookmarks before the native hovered inventory slot. Pressing the Manifest key over an item in a GUI selects count-independent, data-sensitive matches in range/current dimension. Successful selection closes the GUI unless Shift is held. Unlike world opening, this GUI shortcut does not require a carried Manifest.

## Adaptation and security

Restore this path with a bounded reference-item request, not client-supplied coordinates or owners. Reuse current server collection, authorization, grants and tracking synchronization. Validate the live player and current container, rate-limit before reference decoding/scanning, reject malformed or oversized payloads, and close only after successful selection. Shift requests retain the open container. All additions are prepared before publication. No inventory mutation, withdrawal, forced chunk loading or new permission store.

A common client keyboard hook handles one key-down (not repeats); a minimal accessor supplies the native hovered slot. JEI is optional and supplies its hovered reference through a client-only callback cleared when the runtime unloads. Screens without an item under the pointer retain normal key handling.

## Approval and targets

The user authorized reasonable safety/presentation adaptations while completing the feature set. Applies to all four supported leaves. Original in-world opening/clear shortcuts remain unchanged.

## Verification

Pending native payload/selection/permissions and build checks. Connected keyboard/JEI interaction remains in the deferred user acceptance campaign.

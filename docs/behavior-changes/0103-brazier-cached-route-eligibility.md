# 0103 — Revalidate cached Brazier destinations

Status: authorized by the user's direction to introduce reasonable additional gameplay validation and document it; implementation and verification remain pending. Applies to Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Original behavior and evidence

Release pin bb99accf48ed583e29b0efae56e28c963407b8df, checked at /tmp/arcane-archives-reference/migration-source:

- util/InventoryRoutingUtils.java:68–110 constructs fresh routes from the owner's personal network or current Hive according to subnetworkOnly; filters dimension and horizontal radius, and excludes negative routing weights.
- util/InventoryRoutingUtils.java:118–145 tries a valid cached destination before that fresh route construction. A still-live weak reference is used directly without checking current network membership, range, dimension or routing eligibility.
- util/InventoryRoutingUtils.java:148–162 calls route.acceptStack and refreshes the cache after complete acceptance.
- tileentities/BrazierTileEntity.java:386–393 refreshes an existing time-valid entry; ItemCache at 451–489 checks only elapsed time (<1000 ms), with getRoute checking whether the referenced tile is invalid. Radius/subnetwork setters at 83–104 do not clear the cache.
- tileentities/RadiantChestTileEntity.java:112–114 considers native tile invalidity only; acceptStack at 132–135 inserts directly into its inventory without caller/network authorization. A departing Hive member's still-loaded Chest is therefore not excluded by this destination check.

A cache established while eligible can continue receiving inserts after Hive departure/expulsion or range/network-mode changes. Repeated successful transfers less than one second apart refresh it rather than waiting out a fixed revocation window. This is a source-backed stale-route risk, not a reproduced Minecraft storage incident.

## Proposed behavior

Before every cached-route insertion or simulation, revalidate against the same current eligibility rules as a fresh route: live destination identity, current owner/personal-or-Hive membership, network mode, dimension, horizontal radius and routing admissibility. Reject/evict an ineligible entry and use the ordinary fresh-route search; if nothing can accept, preserve the existing remainder/rejection behavior without losing items. Perform validation and mutation on the server thread.

Keep eligible cached routes preferred, preserve original routing weights and ordering, preserve the cache duration for eligible routes, and do not force-load chunks. This is not permission to remove caching, redesign network ownership, change the Brazier radius, enable cross-dimensional routing, or rewrite the normal priority algorithm.

## Reason and approval

Cache hits must not extend access after membership or device configuration changes. This changes reachable upstream behavior. The user subsequently authorized reasonable additional gameplay validation provided it is documented; this eligibility check falls within that authorization. The prior stale-invitation approval (0100) covered invitation authors, not Brazier routing. Normal priority, costs, range and ownership semantics remain unchanged.

## Verification

Executed a minimal source-logic model of the TTL/refresh path: build/brazier-stale-route-model.json. With a still-live accepting destination cached at t=0 and eligibility removed, eight attempts at 500-ms intervals remain time-valid and refresh through t=4000. The model documents its assumptions; it is not upstream Java or runtime gameplay execution.

After approval, cover Hive leave/expulsion, radius reduction, personal-network mode changes, unloaded/replaced destination, routing-mode exclusion, valid cached priority, simulation conservation and rejected remainder retention. Four-target build/package checks and connected-world tests remain required. No Brazier implementation was added or changed for this proposal.

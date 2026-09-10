#!/usr/bin/env python3
"""Linux/X11 client startup smoke check; run under a private xvfb-run display.

Requires installed Xvfb, xauth, xwininfo, libX11 and ffmpeg. Saves screenshots
outside the repository for visual inspection; no gameplay claim is implied.
"""

import argparse
import ctypes
import json
import os
from pathlib import Path
import re
import signal
import subprocess
import tempfile
import time

from smoke_servers import NODES, ROOT


class MessageData(ctypes.Union):
    _fields_ = [("bytes", ctypes.c_char * 20), ("shorts", ctypes.c_short * 10), ("longs", ctypes.c_long * 5)]


class ClientMessage(ctypes.Structure):
    _fields_ = [
        ("type", ctypes.c_int), ("serial", ctypes.c_ulong), ("send_event", ctypes.c_int),
        ("display", ctypes.c_void_p), ("window", ctypes.c_ulong),
        ("message_type", ctypes.c_ulong), ("format", ctypes.c_int), ("data", MessageData),
    ]


class Event(ctypes.Union):
    _fields_ = [("client", ClientMessage), ("padding", ctypes.c_long * 24)]


def close_window(window):
    x11 = ctypes.CDLL("libX11.so.6")
    x11.XOpenDisplay.argtypes = [ctypes.c_char_p]
    x11.XOpenDisplay.restype = ctypes.c_void_p
    x11.XInternAtom.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_int]
    x11.XInternAtom.restype = ctypes.c_ulong
    x11.XSendEvent.argtypes = [ctypes.c_void_p, ctypes.c_ulong, ctypes.c_int, ctypes.c_long, ctypes.POINTER(Event)]
    x11.XCloseDisplay.argtypes = [ctypes.c_void_p]
    display = x11.XOpenDisplay(None)
    if not display:
        raise RuntimeError("Cannot open the private test display")
    try:
        event = Event()
        event.client.type = 33  # X11 ClientMessage
        event.client.display = display
        event.client.window = window
        event.client.message_type = x11.XInternAtom(display, b"WM_PROTOCOLS", False)
        event.client.format = 32
        event.client.data.longs[0] = x11.XInternAtom(display, b"WM_DELETE_WINDOW", False)
        if not x11.XSendEvent(display, window, False, 0, ctypes.byref(event)):
            raise RuntimeError("Could not send the client window a close request")
    finally:
        x11.XCloseDisplay(display)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("node", choices=NODES)
    args = parser.parse_args()
    if not os.environ.get("DISPLAY"):
        parser.error("run inside xvfb-run with a private 1280x800 display")
    artifacts = Path(tempfile.mkdtemp(prefix=f"arcane-client-{args.node}-"))
    log = ROOT / "build" / f"smoke-client-{args.node}-{time.time_ns()}.log"
    log.parent.mkdir(exist_ok=True)
    command = ["timeout", "--foreground", "10m", "./gradlew", f":{args.node}:runClient", "--no-daemon", "--console=plain"]
    started = time.monotonic()
    close_requested = False
    resources_at = None
    process = None
    try:
        with log.open("w") as output:
            process = subprocess.Popen(command, cwd=ROOT, stdout=output, stderr=subprocess.STDOUT, start_new_session=True)
            while process.poll() is None and time.monotonic() - started < 610:
                text = log.read_text(errors="replace")
                if resources_at is None and re.search(r"Created: .*textures/atlas/particles\.png-atlas", text):
                    resources_at = time.monotonic()
                if resources_at is not None and not close_requested and time.monotonic() - resources_at >= 5:
                    windows = subprocess.check_output(["xwininfo", "-root", "-tree"], text=True, timeout=10)
                    match = re.search(r'^\s*(0x[0-9a-fA-F]+) "Minecraft', windows, re.M)
                    if match:
                        with (artifacts / "capture.log").open("w") as capture_log:
                            subprocess.run([
                                "ffmpeg", "-nostdin", "-video_size", "1280x800", "-f", "x11grab", "-i", os.environ["DISPLAY"],
                                "-frames:v", "1", "-update", "1", str(artifacts / "screen.png"),
                            ], stdout=capture_log, stderr=subprocess.STDOUT, check=True, timeout=30)
                        close_window(int(match.group(1), 16))
                        close_requested = True
                time.sleep(0.5)
    finally:
        if process is not None:
            try:
                os.killpg(process.pid, signal.SIGTERM)
            except ProcessLookupError:
                pass
            try:
                process.wait(timeout=15)
            except subprocess.TimeoutExpired:
                os.killpg(process.pid, signal.SIGKILL)
                process.wait(timeout=15)
    text = log.read_text(errors="replace")
    checks = {
        "mod_initialized": "Arcane Archives Recrafted bootstrap initialized on " in text,
        "atlases_created": resources_at is not None,
        "patchouli_resources_loaded": bool(re.search(r"BookContentResourceListenerLoader preloaded \d+ jsons", text)),
        "window_closed": close_requested and "Stopping!" in text,
        "mod_pack_present": not re.search(r"Missing (?:metadata in|data) pack mod:arcanearchives", text),
        "mixin_minimum_declared": not re.search(r'Mixin config arcanearchives[^\s]* does not specify "minVersion"', text),
        "mod_models_present": not re.search(r"(?:Missing textures in model|Unable to load model|Failed to load|Exception loading blockstate definition).*arcanearchives", text),
    }
    result = {
        "node": args.node, "command": command, "exit_code": process.returncode,
        "duration_seconds": round(time.monotonic() - started, 2), "checks": checks,
        "log": str(log.relative_to(ROOT)), "screenshot": str(artifacts / "screen.png"),
        "passed": process.returncode == 0 and all(checks.values()),
    }
    log.with_suffix(".json").write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps(result))
    return 0 if result["passed"] else 1


if __name__ == "__main__":
    raise SystemExit(main())

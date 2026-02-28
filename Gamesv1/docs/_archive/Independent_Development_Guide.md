> [!WARNING]
> This document is archived and may be outdated.
> Do not use it as source of truth.
> Canonical entrypoint: `docs/MasterContext.md` and `docs/DOCS_MAP.md`.
# 🔌 Independent Development Guide (No GS Server Required)

*This document explains how we are able to build and test the fully functional slot game while the real GS backend is currently offline and being built by another team.*

---

## 🛑 The Problem: The GS is Offline!

You mentioned that the primary GS Server is not ready yet. If a real slot game tries to turn on, the first thing it does is ask the GS Server: *"Who is playing? What is their balance? What are the math limits?"*

If the server is offline or doesn't answer, the slot game will permanently freeze on a loading screen, making it impossible for us to build and test the spinning reels, the Win animations, or the UI buttons.

## 🌉 The Solution: The "Dummy" Mock Server

To solve this, we built a very small, fake GS Server (`mock-server.js`) that runs purely on our local computer. 

This dummy server acts exactly like an understudy in a play. It knows all of the lines the real GS Server is supposed to speak. 

**How it works during our development:**
1. Our frontend game (the slot template) sends a secure connection request.
2. The dummy server answers immediately with a fake payload: *"Welcome! The player has $1000.00. The game is Ready."*
3. Because the dummy server spoke the exact right "language" (the `abs.gs.v1` protocol), our frontend game unlocks the `SPIN` button believing it is talking to the real casino.
4. When we click SPIN, the dummy server generates fake random outcomes (like winning $5.00) so we can test the visual fireworks on the screen.

## 🤝 Handoff to the "Other AI Agent"

You mentioned another AI Agent is currently building the real GS Server. Because we have strictly forced our "Dummy Server" to speak the exact GS V1 Protocol, the code we are writing right now **will not require any changes** when the real server is finished.

When the other agent finishes the backend, you simply change the WSS URL in `main.ts` from `ws://127.0.0.1:6001` to the real production server link, and the game will instantly connect and work perfectly.


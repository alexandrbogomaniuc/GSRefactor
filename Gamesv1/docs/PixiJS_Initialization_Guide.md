# 🎨 PixiJS UI & Graphics Initialization Guide

*This document explains how the visual side of our slot machine template is put together and why it works the way it does.*

---

## 🤷‍♂️ What is PixiJS?

PixiJS is a hyper-fast 2D graphics drawing tool. HTML and CSS are great for buttons and text, but they are too slow to draw 60-frames-per-second spinning slot machine reels. PixiJS takes over a rectangular "Canvas" on the screen and uses your computer's Graphics Card (GPU) to draw the symbols, animations, and sparks extremely fast.

## 🐛 The Bug We Just Fixed (Version 8 Update)

In older versions of PixiJS (Version 7), you could instantly create the application on screen like this:
`const app = new PIXI.Application({ width: 800 })`

In the brand new **Version 8**, PixiJS modernized its inner workings to interact with WebGL much more safely. It now requires measuring the screen and preparing the GPU *before* it starts drawing. This means the startup process takes a fraction of a second and demands an **Asynchronous** boot up.

We updated our code to use the modern pattern:
`await app.init({ width: 800 })`

## 🏗️ How the UI Architecture Works

We have completely separated the "Brain" of the game from the "Paintbrush".

1. **The Brain (`SlotEngine.ts`)**: This cares *only* about math and GS server rules. It knows the game is "Spinning" or "Settled". It does not know what a Slot Symbol looks like.
2. **The Paintbrush (`UIManager.ts`)**: This cares *only* about drawing pictures. It listens to the Brain. 
   - When the Brain says "State = RESERVED", the Paintbrush starts blurring the visual reel strips.
   - When the GS Server responds with the Win Amount and grid layout, the Brain passes it to the Paintbrush.
   - The Paintbrush snaps the reels to show those exact symbols. It then takes 2 seconds to play a "Win Celebration" animation. 
   - **Crucially:** The Brain waits until the Paintbrush confirms the animation is 100% finished before sending the final `SETTLE_REQUEST` to the server.

This separation means you can completely redesign how the game *looks* without ever accidentally breaking the GS casino backend integration!

# 📘 Game Server (GS) Database Registration Guide

*This document explains how to take a newly designed game and register it so the "Game Server" (GS) actually knows it exists and allows players to launch it.*

---

## 🤷‍♂️ Why is this required?

Think of the Game Server (GS) as a highly secure toll-booth. When a new custom slot game (`Template Slot V1`) tries to connect to the server and say *"Hey, the player just bet $10!"*, the GS checks its internal database. 

If the database doesn't recognize the name `Template Slot V1`, or if it sees that the maximum allowed bet for this game is only `$5`, the GS will block the action and crash the game to protect the casino.

Therefore, before any game code goes live, the game's **Identity** and its **Math Limits** must be inserted into the GS Database.

## 🛠️ How to Generate the Registration Code

You do not need to be a database expert to do this. We have built an automated tool for you.

1. **Open the Control Panel:** Inside the `slot-template` folder, double-click the `config-ui.html` file. It will open in your web browser.
2. **Fill in the Blanks:** You will see simple text boxes. Enter the name of your new game, its RTP (e.g., 96.5%), and the betting limits.
3. **Click "Generate":** Click the big blue button.
4. **Copy the Output:** On the right-hand side of the screen, a block of SQL text will be generated automatically.

## 🚀 How to Send it to the GS Team

Copy the exact SQL text that was generated in Step 4 and email it or slack it to your Backend Engineering Team.

**Use this exact message template:**

> "Hi Backend Team,
> We have built a new client-side game. Please run the attached SQL script against the `public.game_catalog` and `public.game_math_limits` tables in the Game Server database.
> 
> This registers the new GameID and hardcodes its Maximum Exposure and Bet limits so the GS Orchestrator will accept WebSockets from it without rejecting them."

## 🤓 What happens behind the scenes?

When the developers run the script, two things happen inside the GS brain:
1. **Catalog Update:** The GS learns the name and ID of the game.
2. **Math Lock-In:** The GS memorizes the absolute Maximum Payout (Max Exposure). If the game gets hacked or glitches and tries to pay a player $1,000,000, but the database says the Max Exposure is $500,000, the server will physically block the transaction. This is why the DB registration is completely mandatory for every new game.

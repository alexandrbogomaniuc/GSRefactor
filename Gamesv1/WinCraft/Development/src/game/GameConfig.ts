/**
 * WinCraft Pro - Centralized Game Configuration
 * Separates all math, weights, and timings from the rendering logic.
 */

export const GameConfig = {
    // ---------------------------------------------------------
    // 1. Grid Definitions
    // ---------------------------------------------------------
    TopSlotGrid: {
        cols: 5,
        rows: 3,
        slotSize: 84,
        paddingX: 16,
        paddingY: 12,
        gapX: 12,
        gapY: 12,
        symbolWidth: 96,
        symbolHeight: 96
    },

    MiningGrid: {
        cols: 5,
        rows: 6,
        blockSize: 100, // Slightly smaller than reel symbols
        gravitySpeed: 300, // ms duration for Bounce.Out ease
    },

    // ---------------------------------------------------------
    // 2. Pickaxe Physics & Tools (The "Weapons")
    // ---------------------------------------------------------
    Tools: {
        WOODEN_PICKAXE: { id: 101, name: "Wooden Pickaxe", texture: "PickWood.png", multiplier: 1.0, damage: 1, durability: 2 },
        STONE_PICKAXE: { id: 102, name: "Stone Pickaxe", texture: "PickStone.png", multiplier: 1.2, damage: 2, durability: 4 },
        GOLDEN_PICKAXE: { id: 103, name: "Golden Pickaxe", texture: "PickGold.png", multiplier: 1.5, damage: 3, durability: 3 },
        DIAMOND_PICKAXE: { id: 104, name: "Diamond Pickaxe", texture: "PickDiamond.png", multiplier: 2.0, damage: 5, durability: 10 },
        TNT: { id: 105, name: "TNT", texture: "redstone.png", multiplier: 0.0, damage: 10, durability: 1 }, // Using redstone as placeholder for TNT
        ENDER_EYE: { id: 106, name: "Ender Eye", texture: "ender-eye-bonus.png", multiplier: 0.0, damage: 0, durability: 0 },
        ENCHANTMENT_BOOK: { id: 107, name: "Enchantment Book", texture: "Enchantment.png", multiplier: 0.0, damage: 0, durability: 0 }
    },

    // ---------------------------------------------------------
    // 3. Block Types & Health (The "Field")
    // ---------------------------------------------------------
    Blocks: {
        Dirt: { id: 1, maxHp: 1, rewardRange: [0.1, 0.5], rarityWeight: 60, breakVfx: 'dust', texture: 'dirt.png' },
        Stone: { id: 2, maxHp: 3, rewardRange: [0.5, 1.5], rarityWeight: 25, breakVfx: 'rock_chips', texture: 'stone.png' },
        GoldOre: { id: 3, maxHp: 5, rewardRange: [2.0, 5.0], rarityWeight: 10, breakVfx: 'sparkles_gold', texture: 'gold.png' },
        Diamond: { id: 4, maxHp: 8, rewardRange: [10.0, 50.0], rarityWeight: 5, breakVfx: 'sparkles_blue', texture: 'crystal.png' }, // using crystal as diamond
        Bedrock: { id: 5, maxHp: 999, rewardRange: [0, 0], rarityWeight: 0, breakVfx: 'none', indestructible: true, texture: 'obsidian.png' },
        Grass: { id: 6, maxHp: 1, rewardRange: [0.1, 0.5], rarityWeight: 0, breakVfx: 'dust', texture: 'Grass.png' },
        Redstone: { id: 7, maxHp: 4, rewardRange: [1.0, 3.0], rarityWeight: 15, breakVfx: 'rock_chips', texture: 'redstone.png' }
    },

    // ---------------------------------------------------------
    // 4. Chest Multiplier Weights (Row Clear Rewards)
    // ---------------------------------------------------------
    Multipliers: [
        { value: 2, weight: 50 },
        { value: 3, weight: 30 },
        { value: 5, weight: 12 },
        { value: 10, weight: 5 },
        { value: 25, weight: 2 },
        { value: 100, weight: 1 }
    ],

    // ---------------------------------------------------------
    // 5. Visual "Feel" & Timings
    // ---------------------------------------------------------
    Feel: {
        PickaxeDropTimeMS: 400, // Time it takes for tool to drop from top of column to first block
        ImpactShakeIntensityPX: 2, // Shake amount on hit
        ImpactShakeDurationMS: 100, // How long to shake
        BlockCrumbleDelayMS: 150, // Slight visual pause before gravity triggers so player can register the break
        ChestRevealTimeMS: 800
    }
};

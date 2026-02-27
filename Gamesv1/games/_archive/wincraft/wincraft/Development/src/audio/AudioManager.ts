/**
 * AudioManager — Synthesized Game Audio via Web Audio API
 * 
 * All sounds are generated procedurally — no external audio files needed.
 * Minecraft-inspired 8-bit/retro sounds for block breaking, pickaxe hits,
 * chest opening, reel spinning, win celebrations, and UI interactions.
 * 
 * Usage:
 *   const audio = AudioManager.getInstance();
 *   audio.play('blockBreak', { pitch: 200 });
 */

type SoundName =
    | 'blockBreakDirt'
    | 'blockBreakStone'
    | 'blockBreakGold'
    | 'blockBreakDiamond'
    | 'blockBreakRedstone'
    | 'blockBreakGrass'
    | 'pickaxeHit'
    | 'pickaxeDrop'
    | 'reelSpin'
    | 'reelStop'
    | 'chestOpen'
    | 'winSmall'
    | 'winBig'
    | 'winMega'
    | 'buttonClick'
    | 'betChange'
    | 'coinPickup';

export class AudioManager {
    private static instance: AudioManager;
    private ctx: AudioContext | null = null;
    private masterGain: GainNode | null = null;
    private _muted: boolean = false;
    private _volume: number = 0.4; // Default volume (0-1)
    private _initialized: boolean = false;

    private constructor() {
        // Private constructor for singleton
    }

    public static getInstance(): AudioManager {
        if (!AudioManager.instance) {
            AudioManager.instance = new AudioManager();
        }
        return AudioManager.instance;
    }

    /**
     * Must be called from a user gesture to satisfy browser autoplay policies
     */
    public init(): void {
        if (this._initialized) return;

        try {
            this.ctx = new (window.AudioContext || (window as any).webkitAudioContext)();
            this.masterGain = this.ctx.createGain();
            this.masterGain.gain.value = this._volume;
            this.masterGain.connect(this.ctx.destination);
            this._initialized = true;
            console.log('[AudioManager] Initialized. Web Audio API ready.');
        } catch (e) {
            console.warn('[AudioManager] Web Audio API not supported:', e);
        }
    }

    /**
     * Resume context if suspended (browser autoplay policy)
     */
    private async ensureResumed(): Promise<boolean> {
        if (!this.ctx || !this.masterGain) return false;
        if (this.ctx.state === 'suspended') {
            await this.ctx.resume();
        }
        return true;
    }

    // ─────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────

    public async play(sound: SoundName): Promise<void> {
        if (this._muted || !this._initialized) return;
        if (!(await this.ensureResumed())) return;

        switch (sound) {
            case 'blockBreakDirt': this.synthBlockBreak(180, 0.08, 'brown'); break;
            case 'blockBreakGrass': this.synthBlockBreak(250, 0.06, 'green'); break;
            case 'blockBreakStone': this.synthBlockBreak(120, 0.12, 'grey'); break;
            case 'blockBreakRedstone': this.synthBlockBreak(300, 0.10, 'red'); break;
            case 'blockBreakGold': this.synthBlockBreak(500, 0.15, 'gold'); break;
            case 'blockBreakDiamond': this.synthBlockBreak(800, 0.18, 'blue'); break;
            case 'pickaxeHit': this.synthPickaxeHit(); break;
            case 'pickaxeDrop': this.synthPickaxeDrop(); break;
            case 'reelSpin': this.synthReelSpin(); break;
            case 'reelStop': this.synthReelStop(); break;
            case 'chestOpen': this.synthChestOpen(); break;
            case 'winSmall': this.synthWin(1); break;
            case 'winBig': this.synthWin(2); break;
            case 'winMega': this.synthWin(3); break;
            case 'buttonClick': this.synthButtonClick(); break;
            case 'betChange': this.synthBetChange(); break;
            case 'coinPickup': this.synthCoinPickup(); break;
        }
    }

    /**
     * Get the sound name for a block breakVfx type
     */
    public getSoundForBlockType(vfxType: string): SoundName {
        switch (vfxType) {
            case 'dirt_crumble': return 'blockBreakDirt';
            case 'grass_tear': return 'blockBreakGrass';
            case 'rock_chips': return 'blockBreakStone';
            case 'redstone_sparks': return 'blockBreakRedstone';
            case 'sparkles_gold': return 'blockBreakGold';
            case 'sparkles_blue': return 'blockBreakDiamond';
            case 'obsidian_shatter': return 'blockBreakStone';
            default: return 'blockBreakDirt';
        }
    }

    public toggleMute(): boolean {
        this._muted = !this._muted;
        if (this.masterGain) {
            this.masterGain.gain.value = this._muted ? 0 : this._volume;
        }
        console.log(`[AudioManager] ${this._muted ? '🔇 Muted' : '🔊 Unmuted'}`);
        return this._muted;
    }

    public get isMuted(): boolean { return this._muted; }

    public setVolume(v: number): void {
        this._volume = Math.max(0, Math.min(1, v));
        if (this.masterGain && !this._muted) {
            this.masterGain.gain.value = this._volume;
        }
    }

    // ─────────────────────────────────────────────
    // SOUND SYNTHESIZERS
    // ─────────────────────────────────────────────

    /**
     * Block break — short noise burst with pitched filter
     * Different blocks have different base frequencies and durations
     */
    private synthBlockBreak(freq: number, duration: number, _type: string): void {
        if (!this.ctx || !this.masterGain) return;
        const t = this.ctx.currentTime;

        // Noise generator
        const bufferSize = this.ctx.sampleRate * duration;
        const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
        const data = buffer.getChannelData(0);
        for (let i = 0; i < bufferSize; i++) {
            data[i] = (Math.random() * 2 - 1) * 0.8;
        }

        const noise = this.ctx.createBufferSource();
        noise.buffer = buffer;

        // Bandpass filter to color the noise
        const filter = this.ctx.createBiquadFilter();
        filter.type = 'bandpass';
        filter.frequency.value = freq;
        filter.Q.value = 2;

        // Volume envelope
        const gain = this.ctx.createGain();
        gain.gain.setValueAtTime(0.5, t);
        gain.gain.exponentialRampToValueAtTime(0.01, t + duration);

        noise.connect(filter);
        filter.connect(gain);
        gain.connect(this.masterGain);
        noise.start(t);
        noise.stop(t + duration);
    }

    /**
     * Pickaxe hit — metallic clang (sine + harmonics)
     */
    private synthPickaxeHit(): void {
        if (!this.ctx || !this.masterGain) return;
        const t = this.ctx.currentTime;

        // Fundamental + overtone
        [440, 880, 1320].forEach((freq, i) => {
            const osc = this.ctx!.createOscillator();
            osc.type = 'square';
            osc.frequency.value = freq + (Math.random() * 20 - 10);

            const gain = this.ctx!.createGain();
            const vol = 0.15 / (i + 1);
            gain.gain.setValueAtTime(vol, t);
            gain.gain.exponentialRampToValueAtTime(0.001, t + 0.08);

            osc.connect(gain);
            gain.connect(this.masterGain!);
            osc.start(t);
            osc.stop(t + 0.08);
        });
    }

    /**
     * Pickaxe dropping through the air — descending whoosh
     */
    private synthPickaxeDrop(): void {
        if (!this.ctx || !this.masterGain) return;
        const t = this.ctx.currentTime;

        const osc = this.ctx.createOscillator();
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(800, t);
        osc.frequency.exponentialRampToValueAtTime(200, t + 0.25);

        const gain = this.ctx.createGain();
        gain.gain.setValueAtTime(0.08, t);
        gain.gain.exponentialRampToValueAtTime(0.001, t + 0.25);

        osc.connect(gain);
        gain.connect(this.masterGain);
        osc.start(t);
        osc.stop(t + 0.25);
    }

    /**
     * Reel spin — rhythmic clicking (like slot machine ticker)
     */
    private synthReelSpin(): void {
        if (!this.ctx || !this.masterGain) return;
        const t = this.ctx.currentTime;

        // 6 quick ticks
        for (let i = 0; i < 6; i++) {
            const osc = this.ctx.createOscillator();
            osc.type = 'square';
            osc.frequency.value = 600 + Math.random() * 100;

            const gain = this.ctx.createGain();
            gain.gain.setValueAtTime(0.06, t + i * 0.05);
            gain.gain.exponentialRampToValueAtTime(0.001, t + i * 0.05 + 0.02);

            osc.connect(gain);
            gain.connect(this.masterGain);
            osc.start(t + i * 0.05);
            osc.stop(t + i * 0.05 + 0.02);
        }
    }

    /**
     * Reel stop — satisfying thunk
     */
    private synthReelStop(): void {
        if (!this.ctx || !this.masterGain) return;
        const t = this.ctx.currentTime;

        const osc = this.ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(200, t);
        osc.frequency.exponentialRampToValueAtTime(80, t + 0.1);

        const gain = this.ctx.createGain();
        gain.gain.setValueAtTime(0.3, t);
        gain.gain.exponentialRampToValueAtTime(0.001, t + 0.15);

        osc.connect(gain);
        gain.connect(this.masterGain);
        osc.start(t);
        osc.stop(t + 0.15);
    }

    /**
     * Chest opening — creaky wood sound (low frequency modulated noise)
     */
    private synthChestOpen(): void {
        if (!this.ctx || !this.masterGain) return;
        const t = this.ctx.currentTime;

        // Creak: modulated low frequency
        const osc = this.ctx.createOscillator();
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(100, t);
        osc.frequency.linearRampToValueAtTime(300, t + 0.2);
        osc.frequency.linearRampToValueAtTime(150, t + 0.35);

        const gain = this.ctx.createGain();
        gain.gain.setValueAtTime(0.12, t);
        gain.gain.linearRampToValueAtTime(0.2, t + 0.15);
        gain.gain.exponentialRampToValueAtTime(0.001, t + 0.4);

        const filter = this.ctx.createBiquadFilter();
        filter.type = 'lowpass';
        filter.frequency.value = 800;

        osc.connect(filter);
        filter.connect(gain);
        gain.connect(this.masterGain);
        osc.start(t);
        osc.stop(t + 0.4);

        // Add a sparkle pop after the creak
        setTimeout(() => this.synthCoinPickup(), 200);
    }

    /**
     * Win celebration — ascending chime sequence
     * magnitude: 1=small, 2=big, 3=mega
     */
    private synthWin(magnitude: number): void {
        if (!this.ctx || !this.masterGain) return;
        const t = this.ctx.currentTime;

        // Musical chord (C major arpeggiated)
        const notes = magnitude >= 3
            ? [523, 659, 784, 1047, 1319, 1568]  // C5-G6 mega
            : magnitude >= 2
                ? [523, 659, 784, 1047]            // C5-C6 big
                : [523, 659, 784];                  // C5-G5 small

        notes.forEach((freq, i) => {
            const osc = this.ctx!.createOscillator();
            osc.type = 'sine';
            osc.frequency.value = freq;

            const gain = this.ctx!.createGain();
            const delay = i * 0.08;
            const vol = 0.15 + (magnitude * 0.05);
            gain.gain.setValueAtTime(0.001, t + delay);
            gain.gain.linearRampToValueAtTime(vol, t + delay + 0.02);
            gain.gain.exponentialRampToValueAtTime(0.001, t + delay + 0.4 + (magnitude * 0.1));

            osc.connect(gain);
            gain.connect(this.masterGain!);
            osc.start(t + delay);
            osc.stop(t + delay + 0.5 + (magnitude * 0.15));
        });

        // Add shimmer overlay for big/mega
        if (magnitude >= 2) {
            for (let i = 0; i < magnitude * 4; i++) {
                const shimmer = this.ctx.createOscillator();
                shimmer.type = 'triangle';
                shimmer.frequency.value = 2000 + Math.random() * 3000;

                const gain = this.ctx.createGain();
                const delay = Math.random() * 0.5;
                gain.gain.setValueAtTime(0.001, t + delay);
                gain.gain.linearRampToValueAtTime(0.04, t + delay + 0.01);
                gain.gain.exponentialRampToValueAtTime(0.001, t + delay + 0.08);

                shimmer.connect(gain);
                gain.connect(this.masterGain);
                shimmer.start(t + delay);
                shimmer.stop(t + delay + 0.1);
            }
        }
    }

    /**
     * Button click — short, crisp tick
     */
    private synthButtonClick(): void {
        if (!this.ctx || !this.masterGain) return;
        const t = this.ctx.currentTime;

        const osc = this.ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.value = 1000;

        const gain = this.ctx.createGain();
        gain.gain.setValueAtTime(0.15, t);
        gain.gain.exponentialRampToValueAtTime(0.001, t + 0.03);

        osc.connect(gain);
        gain.connect(this.masterGain);
        osc.start(t);
        osc.stop(t + 0.03);
    }

    /**
     * Bet amount change — gentle pop
     */
    private synthBetChange(): void {
        if (!this.ctx || !this.masterGain) return;
        const t = this.ctx.currentTime;

        const osc = this.ctx.createOscillator();
        osc.type = 'triangle';
        osc.frequency.value = 700;

        const gain = this.ctx.createGain();
        gain.gain.setValueAtTime(0.1, t);
        gain.gain.exponentialRampToValueAtTime(0.001, t + 0.05);

        osc.connect(gain);
        gain.connect(this.masterGain);
        osc.start(t);
        osc.stop(t + 0.05);
    }

    /**
     * Coin/gem pickup — bright rising pling
     */
    private synthCoinPickup(): void {
        if (!this.ctx || !this.masterGain) return;
        const t = this.ctx.currentTime;

        const osc = this.ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(880, t);
        osc.frequency.exponentialRampToValueAtTime(1760, t + 0.08);

        const gain = this.ctx.createGain();
        gain.gain.setValueAtTime(0.2, t);
        gain.gain.exponentialRampToValueAtTime(0.001, t + 0.15);

        osc.connect(gain);
        gain.connect(this.masterGain);
        osc.start(t);
        osc.stop(t + 0.15);
    }
}

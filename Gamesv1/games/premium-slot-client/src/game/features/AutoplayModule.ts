/**
 * AutoplayModule — stub for automated spin sequences.
 *
 * Enable via game.settings.json → features.autoplay: true
 * The module manages a countdown of remaining auto-spins and
 * exposes hooks for the UI to start/stop the sequence.
 */
export class AutoplayModule {
    public static id = 'autoplay';

    /** How many spins remain in the current auto-play run */
    private remaining: number = 0;
    private active: boolean = false;

    /** Preset options offered to the player (e.g. 10, 25, 50, 100) */
    public readonly presets: number[] = [10, 25, 50, 100];

    constructor(private config: { enabled: boolean }) { }

    public init() {
        if (!this.config.enabled) {
            console.log('AutoplayModule: disabled by config');
            return;
        }
        console.log('AutoplayModule: initialized');
    }

    /** Start an autoplay session with the given spin count */
    public start(count: number) {
        this.remaining = count;
        this.active = true;
        console.log(`AutoplayModule: started with ${count} spins`);
    }

    /** Called after each spin resolves. Returns true if another spin should fire. */
    public onSpinComplete(): boolean {
        if (!this.active) return false;
        this.remaining--;
        if (this.remaining <= 0) {
            this.stop();
            return false;
        }
        return true;
    }

    /** Stop the autoplay session immediately */
    public stop() {
        this.active = false;
        this.remaining = 0;
        console.log('AutoplayModule: stopped');
    }

    public get isActive(): boolean {
        return this.active;
    }

    public get spinsRemaining(): number {
        return this.remaining;
    }
}

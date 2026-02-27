export class FreeSpinsModule {
    public static id = 'free-spins';

    constructor(private config: any) { }

    public init() {
        console.log('FreeSpinsModule initialized with config:', this.config);
    }
}

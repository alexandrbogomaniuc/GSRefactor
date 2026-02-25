import * as PIXI from 'pixi.js';

export class TopInventory {
    public container: PIXI.Container;
    private slots: PIXI.Container[] = [];
    private rows: number = 3;
    private cols: number = 5;
    private slotSize: number = 80;
    private padding: number = 10;

    constructor(xOffset: number, yOffset: number) {
        this.container = new PIXI.Container();
        this.container.x = xOffset;
        this.container.y = yOffset;

        this.buildGrid();
    }

    private buildGrid() {
        for (let r = 0; r < this.rows; r++) {
            for (let c = 0; c < this.cols; c++) {
                const slot = new PIXI.Graphics();
                slot.lineStyle(2, 0xffd700, 0.5); // Gold outline
                slot.beginFill(0x000000, 0.6); // Semi-transparent black
                slot.drawRoundedRect(0, 0, this.slotSize, this.slotSize, 8);
                slot.endFill();

                slot.x = c * (this.slotSize + this.padding);
                slot.y = r * (this.slotSize + this.padding);

                this.container.addChild(slot);
                this.slots.push(slot);
            }
        }
    }

    public updateInventory(items: any[]) {
        // Logic to display collected items will go here
    }
}

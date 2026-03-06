import { Container, Graphics, Text } from "pixi.js";

import {
  type LayoutViewport,
  createViewportText,
  formatViewportLabel,
} from "./LayoutContext";

export class LayoutDebugOverlay extends Container {
  private readonly safeRect = new Graphics();
  private readonly text: Text = createViewportText();

  constructor() {
    super();
    this.addChild(this.safeRect);
    this.addChild(this.text);
    this.visible = false;
  }

  public renderViewport(viewport: LayoutViewport) {
    this.safeRect.clear();
    this.safeRect.rect(0, 0, viewport.width, viewport.height);
    this.safeRect.stroke({ color: 0x55ff55, width: 2, alpha: 0.8 });

    const safeX = viewport.safeArea.left;
    const safeY = viewport.safeArea.top;
    const safeWidth = Math.max(
      0,
      viewport.width - viewport.safeArea.left - viewport.safeArea.right,
    );
    const safeHeight = Math.max(
      0,
      viewport.height - viewport.safeArea.top - viewport.safeArea.bottom,
    );

    this.safeRect.rect(safeX, safeY, safeWidth, safeHeight);
    this.safeRect.stroke({ color: 0xff9933, width: 2, alpha: 0.9 });

    this.text.text = `[layout] ${formatViewportLabel(viewport)}`;
    this.text.x = 12;
    this.text.y = 12;
  }
}

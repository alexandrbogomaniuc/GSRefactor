import { flowLayout, type Orientation, type Spacing } from "@pixi/layout";

export interface HudLayoutViewport {
  width: number;
  height: number;
  orientation: Orientation;
  safeArea: Spacing;
}

export interface HudLayoutItem {
  id: string;
  width: number;
  height: number;
  visible: boolean;
  object: {
    x: number;
    y: number;
    visible: boolean;
    width: number;
    height: number;
  };
}

const portraitConfig = {
  regionHeight: 250,
  gap: 12,
  rowGap: 10,
};

const landscapeConfig = {
  regionHeight: 150,
  gap: 14,
  rowGap: 10,
};

export const computeHudLayout = (
  items: Array<Pick<HudLayoutItem, "id" | "width" | "height" | "visible">>,
  viewport: HudLayoutViewport,
) => {
  const config = viewport.orientation === "portrait" ? portraitConfig : landscapeConfig;
  const regionTop = viewport.height - config.regionHeight;

  const laidOut = flowLayout(items, {
    width: viewport.width,
    height: config.regionHeight,
    orientation: viewport.orientation,
    safeArea: {
      top: 0,
      right: viewport.safeArea.right,
      bottom: viewport.safeArea.bottom,
      left: viewport.safeArea.left,
    },
    padding: {
      top: 8,
      right: 16,
      bottom: 10,
      left: 16,
    },
    gap: config.gap,
    rowGap: config.rowGap,
  });

  return {
    regionTop,
    layout: laidOut,
  };
};

export class ResponsiveHudLayoutController {
  private itemMap = new Map<string, HudLayoutItem>();

  constructor(items: HudLayoutItem[]) {
    for (const item of items) {
      this.itemMap.set(item.id, item);
    }
  }

  public setFeatureVisible(id: string, visible: boolean): void {
    const item = this.itemMap.get(id);
    if (!item) return;
    item.visible = visible;
  }

  public isFeatureVisible(id: string): boolean {
    return this.itemMap.get(id)?.visible ?? false;
  }

  public apply(viewport: HudLayoutViewport): void {
    const all = Array.from(this.itemMap.values());

    const { regionTop, layout } = computeHudLayout(
      all.map((item) => ({
        id: item.id,
        width: item.width,
        height: item.height,
        visible: item.visible,
      })),
      viewport,
    );

    const positioned = new Map(layout.items.map((item) => [item.id, item]));

    for (const item of all) {
      const position = positioned.get(item.id);
      if (!position) {
        item.object.visible = false;
        continue;
      }

      item.object.visible = true;
      item.object.x = position.x + item.width * 0.5;
      item.object.y = regionTop + position.y + item.height * 0.5;
    }
  }
}
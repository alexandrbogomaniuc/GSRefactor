export type Orientation = "portrait" | "landscape";

export interface Spacing {
  top: number;
  right: number;
  bottom: number;
  left: number;
}

export interface LayoutItem {
  id: string;
  width: number;
  height: number;
  visible?: boolean;
}

export interface PositionedLayoutItem extends LayoutItem {
  x: number;
  y: number;
  row: number;
  column: number;
}

export interface FlowLayoutOptions {
  width: number;
  height: number;
  gap?: number;
  rowGap?: number;
  padding?: Partial<Spacing>;
  safeArea?: Partial<Spacing>;
  orientation?: Orientation;
}

export interface FlowLayoutResult {
  orientation: Orientation;
  innerWidth: number;
  innerHeight: number;
  originX: number;
  originY: number;
  items: PositionedLayoutItem[];
  rows: number;
}

const normalizeSpacing = (input?: Partial<Spacing>): Spacing => ({
  top: input?.top ?? 0,
  right: input?.right ?? 0,
  bottom: input?.bottom ?? 0,
  left: input?.left ?? 0,
});

export const detectOrientation = (width: number, height: number): Orientation =>
  height >= width ? "portrait" : "landscape";

export const flowLayout = (
  inputItems: LayoutItem[],
  options: FlowLayoutOptions,
): FlowLayoutResult => {
  const gap = options.gap ?? 12;
  const rowGap = options.rowGap ?? gap;
  const padding = normalizeSpacing(options.padding);
  const safeArea = normalizeSpacing(options.safeArea);
  const orientation = options.orientation ?? detectOrientation(options.width, options.height);

  const originX = padding.left + safeArea.left;
  const originY = padding.top + safeArea.top;
  const innerWidth = Math.max(0, options.width - originX - padding.right - safeArea.right);
  const innerHeight = Math.max(0, options.height - originY - padding.bottom - safeArea.bottom);

  const visibleItems = inputItems.filter((item) => item.visible !== false);

  const placed: PositionedLayoutItem[] = [];

  let cursorX = 0;
  let cursorY = 0;
  let row = 0;
  let column = 0;
  let rowMaxHeight = 0;

  for (const item of visibleItems) {
    const nextWidth = column === 0 ? item.width : item.width + gap;
    const shouldWrap = column > 0 && cursorX + nextWidth > innerWidth;

    if (shouldWrap) {
      row += 1;
      cursorY += rowMaxHeight + rowGap;
      cursorX = 0;
      column = 0;
      rowMaxHeight = 0;
    }

    if (column > 0) {
      cursorX += gap;
    }

    placed.push({
      ...item,
      x: originX + cursorX,
      y: originY + cursorY,
      row,
      column,
    });

    cursorX += item.width;
    rowMaxHeight = Math.max(rowMaxHeight, item.height);
    column += 1;
  }

  return {
    orientation,
    innerWidth,
    innerHeight,
    originX,
    originY,
    items: placed,
    rows: placed.length === 0 ? 0 : Math.max(...placed.map((entry) => entry.row)) + 1,
  };
};

export const hasRowGaps = (layout: FlowLayoutResult): boolean => {
  const rows = new Map<number, number[]>();

  for (const item of layout.items) {
    const cols = rows.get(item.row) ?? [];
    cols.push(item.column);
    rows.set(item.row, cols);
  }

  for (const cols of rows.values()) {
    const sorted = [...cols].sort((a, b) => a - b);
    for (let i = 0; i < sorted.length; i += 1) {
      if (sorted[i] !== i) {
        return true;
      }
    }
  }

  return false;
};
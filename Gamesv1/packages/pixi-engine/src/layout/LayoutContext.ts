import { Text } from "pixi.js";

export interface SafeAreaInsets {
  top: number;
  right: number;
  bottom: number;
  left: number;
}

export interface LayoutViewport {
  width: number;
  height: number;
  orientation: "portrait" | "landscape";
  safeArea: SafeAreaInsets;
}

const parseEnvInset = (value: string): number => {
  const parsed = Number.parseFloat(value);
  return Number.isFinite(parsed) ? parsed : 0;
};

export const readSafeAreaInsetsFromCss = (): SafeAreaInsets => {
  if (typeof document === "undefined") {
    return { top: 0, right: 0, bottom: 0, left: 0 };
  }

  const probe = document.createElement("div");
  probe.style.position = "fixed";
  probe.style.top = "0";
  probe.style.left = "0";
  probe.style.paddingTop = "env(safe-area-inset-top)";
  probe.style.paddingRight = "env(safe-area-inset-right)";
  probe.style.paddingBottom = "env(safe-area-inset-bottom)";
  probe.style.paddingLeft = "env(safe-area-inset-left)";
  probe.style.pointerEvents = "none";
  probe.style.visibility = "hidden";
  document.body.appendChild(probe);

  const computed = getComputedStyle(probe);
  const result: SafeAreaInsets = {
    top: parseEnvInset(computed.paddingTop),
    right: parseEnvInset(computed.paddingRight),
    bottom: parseEnvInset(computed.paddingBottom),
    left: parseEnvInset(computed.paddingLeft),
  };

  probe.remove();
  return result;
};

export const inferOrientation = (width: number, height: number): "portrait" | "landscape" =>
  height >= width ? "portrait" : "landscape";

export const buildLayoutViewport = (
  width: number,
  height: number,
  safeArea: Partial<SafeAreaInsets> = {},
): LayoutViewport => ({
  width,
  height,
  orientation: inferOrientation(width, height),
  safeArea: {
    top: safeArea.top ?? 0,
    right: safeArea.right ?? 0,
    bottom: safeArea.bottom ?? 0,
    left: safeArea.left ?? 0,
  },
});

export const formatViewportLabel = (viewport: LayoutViewport): string =>
  `${viewport.width}x${viewport.height} ${viewport.orientation} | safe(top:${viewport.safeArea.top}, right:${viewport.safeArea.right}, bottom:${viewport.safeArea.bottom}, left:${viewport.safeArea.left})`;

export const createViewportText = (): Text =>
  new Text({
    text: "",
    style: {
      fontFamily: "monospace",
      fontSize: 14,
      fill: 0x8cf0ff,
      stroke: { color: 0x000000, width: 3 },
    },
  });
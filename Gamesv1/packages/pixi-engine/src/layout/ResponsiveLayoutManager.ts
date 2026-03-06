import type { CreationEngine } from "../engine";
import {
  buildLayoutViewport,
  readSafeAreaInsetsFromCss,
  type LayoutViewport,
  type SafeAreaInsets,
} from "./LayoutContext";
import { LayoutDebugOverlay } from "./LayoutDebugOverlay";

export interface ResponsiveLayoutManagerOptions {
  safeArea?: Partial<SafeAreaInsets>;
  debugEnabled?: boolean;
  debugToggleKey?: string;
}

export class ResponsiveLayoutManager {
  private readonly overlay = new LayoutDebugOverlay();
  private readonly options: ResponsiveLayoutManagerOptions;
  private viewport: LayoutViewport;
  private debugEnabled: boolean;

  constructor(
    private readonly app: CreationEngine,
    options: ResponsiveLayoutManagerOptions = {},
  ) {
    this.options = options;
    this.debugEnabled = Boolean(options.debugEnabled);
    this.viewport = buildLayoutViewport(
      this.app.renderer.width,
      this.app.renderer.height,
      this.resolveSafeArea(options.safeArea),
    );

    this.app.stage.addChild(this.overlay);
    this.overlay.visible = this.debugEnabled;
    this.overlay.renderViewport(this.viewport);

    if (typeof window !== "undefined") {
      const key = options.debugToggleKey ?? "KeyL";
      window.addEventListener("keydown", (event) => {
        if (event.code === key) {
          this.toggleDebug();
        }
      });
    }
  }

  public updateFromRenderer(): LayoutViewport {
    this.viewport = buildLayoutViewport(
      this.app.renderer.width,
      this.app.renderer.height,
      this.resolveSafeArea(this.options.safeArea),
    );

    this.overlay.renderViewport(this.viewport);
    this.overlay.visible = this.debugEnabled;

    return this.viewport;
  }

  public getViewport(): LayoutViewport {
    return this.viewport;
  }

  public isDebugEnabled(): boolean {
    return this.debugEnabled;
  }

  public setDebugEnabled(value: boolean): void {
    this.debugEnabled = value;
    this.overlay.visible = value;
  }

  public toggleDebug(): boolean {
    this.setDebugEnabled(!this.debugEnabled);
    return this.debugEnabled;
  }

  private resolveSafeArea(overrides?: Partial<SafeAreaInsets>): SafeAreaInsets {
    const fromCss = readSafeAreaInsetsFromCss();
    return {
      top: overrides?.top ?? fromCss.top,
      right: overrides?.right ?? fromCss.right,
      bottom: overrides?.bottom ?? fromCss.bottom,
      left: overrides?.left ?? fromCss.left,
    };
  }
}

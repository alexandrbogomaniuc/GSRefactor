import type { CreationEngine } from "./engine";

let instance: CreationEngine | null = null;

export function setEngine(app: CreationEngine) {
  instance = app;
}

export function engine(): CreationEngine {
  if (!instance) {
    throw new Error("Engine has not been initialized.");
  }

  return instance;
}

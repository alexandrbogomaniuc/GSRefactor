export const RUNTIME_SLOT_KEYS = [
  "preloader.wordmark",
  "preloader.background.desktop",
  "preloader.background.landscape",
  "preloader.background.portrait",
  "uiAtlas.reel-frame-panel",
  "symbolAtlas.symbol-0-egg",
  "symbolAtlas.symbol-9-rooster",
  "symbolAtlas.collector-symbol",
  "vfxAtlas.lightning-arc-01",
] as const;

export type RuntimeSlotKey = (typeof RUNTIME_SLOT_KEYS)[number];

export type RuntimeSlotState = "mapped" | "generic" | "placeholder" | "missing";

export type RuntimeSlotRecord = {
  key: RuntimeSlotKey;
  state: RuntimeSlotState;
  source?: string;
  notes?: string;
};

const slotRegistry = new Map<RuntimeSlotKey, RuntimeSlotRecord>(
  RUNTIME_SLOT_KEYS.map((key) => [key, { key, state: "missing" }]),
);

export const setRuntimeSlotRecord = (record: RuntimeSlotRecord): void => {
  slotRegistry.set(record.key, record);
};

export const getRuntimeSlotRecord = (key: RuntimeSlotKey): RuntimeSlotRecord =>
  slotRegistry.get(key) ?? { key, state: "missing" };

export const listRuntimeSlotRecords = (): RuntimeSlotRecord[] =>
  RUNTIME_SLOT_KEYS.map((key) => getRuntimeSlotRecord(key));

export const listMissingRuntimeAssetKeys = (): RuntimeSlotKey[] =>
  listRuntimeSlotRecords()
    .filter((record) => record.state === "missing" || record.state === "placeholder")
    .map((record) => record.key);

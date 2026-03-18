import {
  getRuntimeSlotRecord,
  type RuntimeSlotKey,
  type RuntimeSlotRecord,
} from "./runtimeSlotContract";

export type SlotResolution = RuntimeSlotRecord & {
  fallbackUsed: boolean;
};

export const resolveRuntimeSlot = (
  key: RuntimeSlotKey,
  fallbackSource?: string,
): SlotResolution => {
  const record = getRuntimeSlotRecord(key);
  if (record.state === "mapped" && record.source) {
    return { ...record, fallbackUsed: false };
  }

  if (fallbackSource) {
    return {
      key,
      state: record.state === "missing" ? "generic" : record.state,
      source: fallbackSource,
      notes: record.notes,
      fallbackUsed: true,
    };
  }

  return {
    ...record,
    fallbackUsed: true,
  };
};

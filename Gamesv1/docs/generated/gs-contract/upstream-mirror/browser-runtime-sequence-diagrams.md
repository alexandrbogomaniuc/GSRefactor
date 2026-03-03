# GS Browser Runtime Sequence Diagrams

## Standard round flow

1. Browser -> GS: `POST /slot/v1/bootstrap`
2. Browser -> GS: `POST /slot/v1/opengame`
3. Browser -> GS: `POST /slot/v1/playround`
4. GS -> Browser: envelope with `wallet`, `round`, `presentationPayload`
5. Browser -> GS: `POST /slot/v1/gethistory` (read-only)

## Feature action flow

1. Browser -> GS: `POST /slot/v1/featureaction`
2. GS -> Browser: envelope with `feature` + `presentationPayload`

## Restore flow

1. Browser -> GS: `POST /slot/v1/resumegame` (includes `resumeRef`)
2. GS -> Browser: envelope with `restore` and current session/wallet truth

## Close flow

1. Browser -> GS: `POST /slot/v1/closegame` (includes `closeReason`)
2. GS -> Browser: close acknowledgement envelope

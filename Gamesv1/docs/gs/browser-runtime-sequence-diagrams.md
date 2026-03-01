# GS Browser Runtime Sequence Diagrams

Status: canonical flow diagrams for browser -> GS runtime.

## 1) Bootstrap + Open

```text
Browser -> GS: POST /v1/bootstrap
GS -> Browser: session + wallet + runtimeConfig + capabilities
Browser -> GS: POST /v1/opengame
GS -> Browser: open ack + sequencing snapshot
```

## 2) Play Round

```text
Browser -> GS: POST /v1/playround (requestCounter/idempotency/clientOperationId)
GS -> Browser: round settled + wallet update + presentationPayload
Browser: render only from presentationPayload + policy config
```

## 3) Feature Action

```text
Browser -> GS: POST /v1/featureaction
GS -> Browser: updated state metadata + presentationPayload
```

## 4) Resume / Restore

```text
Browser -> GS: POST /v1/resumegame
GS -> Browser: restored snapshot + unfinished round payload (if any)
Browser: replay presentation without inventing financial truth
```

## 5) History

```text
Browser -> GS: POST /v1/gethistory
GS -> Browser: history rows + sequencing snapshot
```

## 6) Close

```text
Browser -> GS: POST /v1/closegame
GS -> Browser: close acknowledgement
```

# Refactor Service Contracts

Versioned interface contracts for extracted microservices.

## OpenAPI
- `openapi/config-service-v1.yaml`
- `openapi/session-service-v1.yaml`
- `openapi/gameplay-orchestrator-v1.yaml`
- `openapi/wallet-adapter-v1.yaml`
- `openapi/bonus-frb-service-v1.yaml`
- `openapi/history-service-v1.yaml`
- `openapi/protocol-adapter-v1.yaml`

These files are the baseline for compatibility-facade routing and third-party integration onboarding.

## Event Schemas
- `jsonschema/session-outbox-event-v1.schema.json`

## Validators
- `validators/validate-session-event-stream.js`

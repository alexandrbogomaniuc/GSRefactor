# Slot Round Lifecycle

The `RoundStateMachine` governs round progression for client presentation while GS remains authoritative for financial/session truth.

## Lifecycle Diagram

```mermaid
stateDiagram-v2
    [*] --> IDLE
    IDLE --> ENTERING : init_enter()
    ENTERING --> READY : session_loaded
    READY --> SPINNING : transaction_request_sent
    SPINNING --> AWAITING_RESULT : response_received
    AWAITING_RESULT --> PRESENTING_WIN : min_spin_time_reached
    PRESENTING_WIN --> IN_FEATURE : feature_triggered
    IN_FEATURE --> PRESENTING_WIN : feature_step_complete
    PRESENTING_WIN --> ROUND_END : presentation_complete
    IN_FEATURE --> ROUND_END : feature_complete
    ROUND_END --> READY : cleanup

    ANY_STATE --> IDLE : fatal_error_or_reload
```

## Key Rules

1. GS response is authoritative for result, wallet, and recoverable state.
2. Client applies min-spin-time gating before final win presentation.
3. Turbo/skip changes only presentation timing, never financial outcome.
4. Round end occurs after visual completion; not at response arrival.

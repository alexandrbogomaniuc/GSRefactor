<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>GS Modernization Docs Index</title>
    <link rel="stylesheet" href="/support/css/bootstrap.min.css"/>
    <style>
        body { padding-bottom: 30px; }
        .section {
            margin-top: 16px;
            border: 1px solid #ddd;
            border-radius: 4px;
            padding: 12px;
            background: #fff;
        }
        .small-note { color: #666; font-size: 12px; }
        code { white-space: pre-wrap; word-break: break-word; }
    </style>
</head>
<body>
<div class="container col-xs-12 col-sm-12 col-md-10 col-md-offset-1">
    <h2>GS Modernization Docs Index</h2>
    <p class="small-note">
        Quick operator index for modernization plans, phase outputs, and runtime evidence artifacts.
        All paths are repository-relative under <code>/Users/alexb/Documents/Dev/Dev_new</code>.
    </p>

    <div class="section">
        <h4>Operator Pages</h4>
        <table class="table table-bordered table-condensed">
            <thead>
            <tr>
                <th>Page</th>
                <th>Purpose</th>
                <th>URL</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Modernization Progress Dashboard</td>
                <td>Visual progress, checklist, local tracking</td>
                <td><a href="/support/modernizationProgress.html">/support/modernizationProgress.html</a></td>
            </tr>
            <tr>
                <td>Configuration Portal</td>
                <td>Cluster + bank config workflow and effective values</td>
                <td><a href="/support/configPortal.jsp">/support/configPortal.jsp</a></td>
            </tr>
            <tr>
                <td>Cluster Hosts</td>
                <td>Centralized host/port keys with descriptions</td>
                <td><a href="/support/clusterHosts.jsp">/support/clusterHosts.jsp</a></td>
            </tr>
            <tr>
                <td>Modernization Runbook</td>
                <td>Execution commands for readiness and evidence packs</td>
                <td><a href="/support/modernizationRunbook.jsp">/support/modernizationRunbook.jsp</a></td>
            </tr>
            <tr>
                <td>Phase 8 Discrepancy Viewer</td>
                <td>Read-only viewer for Phase 8 Wave 3 discrepancy export JSON</td>
                <td><a href="/support/phase8DiscrepancyViewer.html">/support/phase8DiscrepancyViewer.html</a></td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="section">
        <h4>Program Baseline Docs</h4>
        <table class="table table-bordered table-striped table-condensed">
            <thead>
            <tr>
                <th>Document</th>
                <th>Purpose</th>
                <th>Path</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>GS Behavior Map and Runtime Flow Blueprint</td>
                <td>Legacy behavior and runtime flow baseline</td>
                <td><code>docs/16-gs-behavior-map-and-runtime-flow-blueprint.md</code></td>
            </tr>
            <tr>
                <td>New Team Onboarding and New Game Playbook</td>
                <td>Onboarding and integration operations</td>
                <td><code>docs/17-new-team-onboarding-and-new-game-playbook.md</code></td>
            </tr>
            <tr>
                <td>Architecture Recommendations Modernization Plan</td>
                <td>Target architecture and migration direction</td>
                <td><code>docs/18-architecture-recommendations-modernization-plan.md</code></td>
            </tr>
            <tr>
                <td>Requirements from User</td>
                <td>Program constraints and non-negotiable requirements</td>
                <td><code>docs/19-requirements-from-user.md</code></td>
            </tr>
            <tr>
                <td>Modernization Roadmap</td>
                <td>Milestones, dependencies, risk sequence</td>
                <td><code>docs/21-modernization-roadmap-v1.md</code></td>
            </tr>
            <tr>
                <td>Two-Week Execution Plan</td>
                <td>Sprint-level concrete tasks and acceptance</td>
                <td><code>docs/22-sprint-01-two-week-execution-plan.md</code></td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="section">
        <h4>Phase Deliverables</h4>
        <table class="table table-bordered table-striped table-condensed">
            <thead>
            <tr>
                <th>Phase</th>
                <th>Current Deliverable Docs</th>
                <th>Path(s)</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Phase 0 - Baseline and Parity</td>
                <td>Inventory, parity capture, reconnect evidence</td>
                <td><code>docs/23-phase-0-baseline-and-parity-capture.md</code><br/><code>docs/phase0/</code></td>
            </tr>
            <tr>
                <td>Phase 2 - Observability</td>
                <td>Correlation standard and probes</td>
                <td><code>docs/29-trace-correlation-standard-v1.md</code><br/><code>docs/phase2/</code></td>
            </tr>
            <tr>
                <td>Phase 3 - Config Platform</td>
                <td>Config service foundation and portal model</td>
                <td><code>docs/32-gs-config-portal-all-levels-spec.md</code><br/><code>docs/33-phase3-config-service-foundation.md</code><br/><code>docs/135-phase3-config-portal-persistent-approvals-browser-local-20260224-090500.md</code><br/><code>docs/136-phase3-config-portal-publish-rollback-guardrails-visualization-20260224-091500.md</code></td>
            </tr>
            <tr>
                <td>Phase 4 - Protocol Adapter</td>
                <td>JSON/XML bank-mode adapter, parity, runtime probes</td>
                <td><code>docs/46-phase4-protocol-adapter-scaffold-20260220-171616.md</code><br/><code>docs/52-phase4-runtime-readiness-check-tooling-20260220-175155.md</code><br/><code>docs/138-phase4-runtime-evidence-pack-docker-api-denied-degraded-classification-20260224-093000.md</code><br/><code>docs/phase4/</code></td>
            </tr>
            <tr>
                <td>Phase 5 - Core Extraction</td>
                <td>Session/gameplay/wallet/bonus/history scaffolds and canary tooling</td>
                <td><code>docs/34-phase5-session-service-foundation.md</code><br/><code>docs/53-phase5-gameplay-redis-state-blob-foundation-20260220-183300.md</code><br/><code>docs/58-phase5-gameplay-runtime-evidence-pack-tooling-20260220-180700.md</code><br/><code>docs/65-phase5-wallet-adapter-shadow-hook-and-canary-20260220-185600.md</code><br/><code>docs/66-phase5-wallet-runtime-evidence-pack-tooling-20260220-185700.md</code><br/><code>docs/67-phase5-bonus-frb-shadow-hook-and-canary-20260220-190200.md</code><br/><code>docs/68-phase5-bonus-frb-runtime-evidence-pack-tooling-20260220-190300.md</code><br/><code>docs/69-phase5-history-shadow-hook-and-canary-20260220-191000.md</code><br/><code>docs/70-phase5-history-runtime-evidence-pack-tooling-20260220-191100.md</code><br/><code>docs/phase5/</code></td>
            </tr>
            <tr>
                <td>Phase 6 - Multiplayer Separation</td>
                <td>Boundary and bypass architecture</td>
                <td><code>docs/39-phase6-multiplayer-boundary-and-bypass-v1.md</code><br/><code>docs/71-phase6-multiplayer-service-scaffold-and-routing-20260220-191300.md</code><br/><code>docs/72-phase6-multiplayer-runtime-evidence-pack-tooling-20260220-191400.md</code><br/><code>docs/73-phase6-gs-multiplayer-shadow-bridge-20260220-191800.md</code><br/><code>docs/74-phase6-multiplayer-routing-policy-probe-and-test-gate-20260220-192600.md</code><br/><code>docs/phase6/</code></td>
            </tr>
            <tr>
                <td>Quality Gates</td>
                <td>Local post-change verification suite and reports</td>
                <td><code>docs/75-phase5-6-local-verification-suite-20260223-130100.md</code><br/><code>docs/quality/local-verification/</code></td>
            </tr>
            <tr>
                <td>Phase 7 - Cassandra Upgrade</td>
                <td>Plan, rehearsal, compatibility matrix, runbook, tested validation report and no-go closure report</td>
                <td><code>docs/40-phase7-cassandra-upgrade-plan-v1.md</code><br/><code>docs/41-phase7-cassandra-rehearsal-checklist-v1.md</code><br/><code>docs/42-phase7-cassandra-cutover-rollback-runbook-v1.md</code><br/><code>docs/43-phase7-cassandra-driver-compatibility-matrix-v1.md</code><br/><code>docs/44-phase7-cassandra-schema-data-parity-template-v1.md</code><br/><code>docs/131-phase7-cassandra-evidence-pack-degraded-docker-api-denied-statuses-20260224-081500.md</code><br/><code>docs/132-phase7-cassandra-rehearsal-report-blocked-state-prefill-from-manifest-20260224-082000.md</code><br/><code>docs/133-phase7-cassandra-schema-data-validation-report-dual-cluster-rehearsal-20260224-084500.md</code><br/><code>docs/134-phase7-cassandra-rehearsal-report-tested-no-go-and-phase-deliverable-closure-20260224-090000.md</code><br/><code>docs/phase7/</code></td>
            </tr>
            <tr>
                <td>Phase 8 - Precision Modernization</td>
                <td>0.001 support audit kickoff (hardcoded minimums, rounding, float/double hotspots)</td>
                <td><code>docs/19-requirements-from-user.md</code><br/><code>docs/83-phase8-precision-minbet-audit-scan-kickoff-20260223-145000.md</code><br/><code>docs/84-phase8-precision-regression-vector-smoke-20260223-150000.md</code><br/><code>docs/85-phase8-precision-remediation-wave-plan-buckets-20260223-151500.md</code><br/><code>docs/86-phase8-wave1-reporting-bucket-refinement-and-vector-smoke-20260223-153000.md</code><br/><code>docs/87-phase8-wave1-reporting-display-code-remediation-batch1-20260223-154500.md</code><br/><code>docs/88-phase8-wave1-numberutils-asmoney-parity-and-remediation-batch2-20260223-160000.md</code><br/><code>docs/89-phase8-wave1-closure-and-wave2-coinrule-vectors-20260223-161500.md</code><br/><code>docs/90-phase8-wave2-settings-coinrule-code-remediation-batch1-20260223-163500.md</code><br/><code>docs/91-phase8-wave2-scale-ready-helper-path-batch2-20260223-164500.md</code><br/><code>docs/92-phase8-wave3-dualcalc-comparison-vectors-kickoff-20260223-170000.md</code><br/><code>docs/93-phase8-wave3-parity-hooks-and-dashboard-file-sync-visibility-20260223-171500.md</code><br/><code>docs/94-phase8-wave3-discrepancy-evidence-counters-and-snapshots-20260223-173000.md</code><br/><code>docs/95-phase8-wave3-discrepancy-export-tool-and-visibility-20260223-174500.md</code><br/><code>docs/96-phase8-wave3-discrepancy-viewer-support-page-20260223-180000.md</code><br/><code>docs/97-phase8-wave3-discrepancy-viewer-compare-mode-20260223-183000.md</code><br/><code>docs/98-phase8-wave3-viewer-guided-validation-thresholds-20260223-184500.md</code><br/><code>docs/99-phase8-wave3-viewer-compact-comparison-report-export-20260223-190000.md</code><br/><code>docs/100-phase8-wave3-viewer-named-threshold-policy-profiles-20260223-191500.md</code><br/><code>docs/101-phase8-wave3-cli-compare-export-with-policy-profiles-20260223-193000.md</code><br/><code>docs/102-phase8-wave3-cli-compare-export-threshold-overrides-20260223-194500.md</code><br/><code>docs/103-phase8-wave3-viewer-import-cli-compare-report-json-20260223-200500.md</code><br/><code>docs/104-phase8-wave3-viewer-imported-compare-report-diff-mode-20260223-202000.md</code><br/><code>docs/105-phase8-wave3-imported-report-diff-filters-and-dragdrop-20260223-203500.md</code><br/><code>docs/106-phase8-wave3-imported-report-diff-triage-filters-20260223-205000.md</code><br/><code>docs/107-phase8-wave3-imported-report-diff-local-triage-presets-20260223-210500.md</code><br/><code>docs/108-phase8-wave3-imported-report-diff-triage-preset-json-share-20260223-213500.md</code><br/><code>docs/109-phase8-wave3-imported-report-diff-triage-preset-bundle-dragdrop-20260223-220500.md</code><br/><code>docs/110-phase8-wave3-triage-preset-bundle-compatibility-badges-20260223-225500.md</code><br/><code>docs/111-phase8-wave3-triage-preset-bundle-import-preview-counts-20260223-231500.md</code><br/><code>docs/112-phase8-wave3-triage-preset-bundle-overwrite-confirm-guard-20260223-233500.md</code><br/><code>docs/113-phase8-wave3-triage-preset-guard-profiles-and-threshold-20260223-235500.md</code><br/><code>docs/114-phase8-wave3-triage-preset-guard-settings-localstorage-20260224-001500.md</code><br/><code>docs/115-phase8-wave3-viewer-local-reset-for-presets-and-guard-20260224-003000.md</code><br/><code>docs/116-phase8-wave3-viewer-guard-settings-json-share-20260224-011500.md</code><br/><code>docs/117-phase8-wave3-viewer-guard-settings-json-dragdrop-20260224-013000.md</code><br/><code>docs/118-phase8-wave3-viewer-guard-settings-compatibility-status-20260224-014500.md</code><br/><code>docs/119-phase8-wave3-viewer-guard-settings-import-preview-20260224-020000.md</code><br/><code>docs/120-phase8-wave3-viewer-guard-preview-diff-vs-current-20260224-021500.md</code><br/><code>docs/121-phase8-wave3-viewer-artifact-based-triage-preset-suggestions-20260224-023000.md</code><br/><code>docs/122-phase8-wave3-viewer-triage-suggestion-pre-save-summary-export-20260224-024500.md</code><br/><code>docs/123-phase8-wave3-viewer-save-suggestion-and-bundle-flow-20260224-030000.md</code><br/><code>docs/124-phase8-wave3-core-apply-mode-scaffold-and-vector-gate-20260224-040000.md</code><br/><code>docs/125-phase8-precision-policy-matrix-and-generator-gate-20260224-050000.md</code><br/><code>docs/126-phase8-history-reporting-precision-vector-gate-20260224-053000.md</code><br/><code>docs/127-phase8-wallet-contract-precision-vector-gate-20260224-060000.md</code><br/><code>docs/128-phase8-nonprod-canary-readiness-and-evidence-pack-20260224-063000.md</code><br/><code>docs/129-phase8-nonprod-canary-execution-script-and-sandbox-blocker-20260224-070000.md</code><br/><code>docs/130-phase8-nonprod-canary-auto-close-finalizer-and-one-command-close-path-20260224-073000.md</code><br/><code>docs/phase8/precision/</code></td>
            </tr>
            <tr>
                <td>Phase 9 - Branding/Namespace</td>
                <td>ABS rename wave plan</td>
                <td><code>docs/30-phase9-abs-rename-wave-plan-gs-v1.md</code><br/><code>docs/139-phase9-abs-compatibility-mapping-manifest-and-validator-20260224-094500.md</code><br/><code>docs/140-phase9-abs-rename-candidate-scan-and-review-only-block-gate-20260224-100000.md</code><br/><code>docs/141-phase9-w0-safe-target-path-filter-profile-for-candidate-scan-20260224-101500.md</code><br/><code>docs/142-phase9-manifest-wave-path-profiles-and-full-vs-profile-diff-report-20260224-103000.md</code><br/><code>docs/143-phase9-w0-execution-plan-generator-from-profile-scan-20260224-104500.md</code><br/><code>docs/144-phase9-w0-patch-plan-export-grouped-by-file-with-snippets-20260224-110000.md</code><br/><code>docs/145-phase9-w0-text-replace-dry-run-apply-tool-with-review-only-guard-20260224-111500.md</code></td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="section">
        <h4>Live Session Logs</h4>
        <table class="table table-bordered table-condensed">
            <thead>
            <tr>
                <th>File</th>
                <th>Purpose</th>
                <th>Path</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Work Diary</td>
                <td>Increment-by-increment implementation timeline</td>
                <td><code>docs/12-work-diary.md</code></td>
            </tr>
            <tr>
                <td>Launch Forensics</td>
                <td>Runtime launch/websocket/wallet troubleshooting evidence</td>
                <td><code>docs/11-game-launch-forensics.md</code></td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

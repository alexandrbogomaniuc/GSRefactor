<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>GS Modernization Runbook</title>
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
        pre { background: #f7f9fc; border: 1px solid #d9e1ea; }
    </style>
</head>
<body>
<div class="container col-xs-12 col-sm-12 col-md-10 col-md-offset-1">
    <h2>GS Modernization Runbook</h2>
    <p class="small-note">
        Refactor-only operations guide for bank canary validation. Legacy runtime remains unchanged.
    </p>

    <div class="section">
        <h4>Prerequisites</h4>
        <ul>
            <li>Work from <code>$REPO_ROOT</code>.</li>
            <li>Use bank canary <code>6275</code> for refactor checks.</li>
            <li>Do not modify legacy stack in <code>$LEGACY_ROOT</code>.</li>
        </ul>
        <pre><code># Set once in your shell before running commands from this page
export REPO_ROOT=/absolute/path/to/Dev_new
export LEGACY_ROOT=/absolute/path/to/legacy-root
</code></pre>
    </div>

    <div class="section">
        <h4>Post-Change Local Verification (Required Before/After Feature Batches)</h4>
        <pre><code>cd $REPO_ROOT
$REPO_ROOT/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh
</code></pre>
        <p class="small-note">
            Generates offline verification report (syntax/help/config/manifest checks + executable local behavior smoke) under
            <code>docs/quality/local-verification/</code>. Use this even when runtime endpoints are unavailable.
        </p>
    </div>

    <div class="section">
        <h4>Program Deploy / Cutover Readiness Aggregation (Before Refactor Deploy/Canary)</h4>
        <pre><code>cd $REPO_ROOT
bash $REPO_ROOT/gs-server/deploy/scripts/program-deploy-readiness-status-report.sh
</code></pre>
        <p class="small-note">
            Aggregates latest Phase 4/5/6 status reports, Phase 7 Cassandra rehearsal result, legacy parity baseline status, security hardening status, and the latest local verification suite result into a single blocker list under <code>docs/release-readiness/</code>.
        </p>
    </div>

    <div class="section">
        <h4>Refactor Environment Deploy (All Dependencies, Externalized Config)</h4>
        <pre><code>cd $REPO_ROOT
# Edit centralized hosts/ports only:
vi $REPO_ROOT/gs-server/deploy/config/cluster-hosts.properties

# Sync generated env/resources
$REPO_ROOT/gs-server/deploy/scripts/sync-cluster-hosts.sh

# Follow the full deploy/reboot runbook (ordered dependencies, c1-refactor switch, health checks):
docs/168-refactor-environment-deploy-and-dependency-startup-runbook-20260224-141500.md
</code></pre>
        <p class="small-note">
            Host/port configuration must remain externalized in <code>cluster-hosts.properties</code>. Do not hardcode runtime endpoints in service code or ad-hoc compose commands.
        </p>
    </div>

    <div class="section">
        <h4>Legacy Mixed-Topology Validation Pack (Refactored GS + Legacy MP/Client)</h4>
        <pre><code>cd $REPO_ROOT
# Dry-run checklist + report template (no runtime calls):
bash $REPO_ROOT/gs-server/deploy/scripts/legacy-mixed-topology-validation-pack.sh --dry-run true

# Runtime preflight (HTTP reachability checks + operator checklist report):
bash $REPO_ROOT/gs-server/deploy/scripts/legacy-mixed-topology-validation-pack.sh \
  --refactor-gs-url http://127.0.0.1:18081 \
  --legacy-mp-url http://127.0.0.1:8088 \
  --legacy-client-url http://127.0.0.1:8090 \
  --bank-id 6275 \
  --game-id 838
</code></pre>
        <p class="small-note">
            Produces a dedicated mixed-topology validation report under <code>docs/validation/legacy-mixed-topology/</code> with endpoint reachability status and a repeatable operator checklist for launch/reconnect/FRB evidence capture.
        </p>
    </div>

    <div class="section">
        <h4>Phase 8 Wave 3 Discrepancy Snapshot Export (Optional, Compare Mode Diagnostics)</h4>
        <pre><code>cd $REPO_ROOT
# After running GS with compare mode enabled in a non-prod environment:
$REPO_ROOT/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export.sh \
  --log-file /path/to/gs.log \
  --out-file /tmp/phase8-wave3-discrepancy-export.json

# Parser smoke (offline, no GS runtime required):
$REPO_ROOT/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export-smoke.sh
</code></pre>
        <p class="small-note">
            Parses <code>phase8-precision-dual-calc</code> snapshot log lines emitted by Wave 3 parity hooks into a structured JSON summary.
            Compare mode remains disabled by default; enable only in non-production validation.
        </p>
        <p class="small-note">
            Optional viewer page for exported JSON: <code>/support/phase8DiscrepancyViewer.html</code> (supports file upload, pasted JSON, embedded samples, A-vs-B compare mode, guided validation thresholds/pass-fail badges, named threshold policy profiles, compact JSON/Markdown comparison report export, compact compare-report JSON import/inspection, imported-artifact diff mode for two compact compare reports, changed-only diff filters, drag/drop import onto Imported A/B text areas, rule-status diff filters, metric-name search triage, browser-local saved triage presets, artifact-based triage preset suggestion generation from imported compare reports (applies suggested filters + preset name without auto-save), compact pre-save suggestion summary preview/export (JSON/Markdown), one-click "Save Suggestion + Bundle JSON" (saves the suggested preset and exports a combined preset+summary bundle artifact), triage preset JSON export/import for cross-machine sharing, preset-bundle drag/drop import onto the preset JSON textarea, preset-bundle schema/version compatibility status badges, preset import preview counts (incoming/new/overwrite) before merge, a configurable high-overwrite confirmation guard (strict/default/relaxed/disabled/custom threshold) with browser-local guard setting persistence, a one-click reset for viewer-local presets/guard settings, and overwrite-guard settings JSON export/import plus drag/drop import with schema/version compatibility status metadata, pre-apply import preview, and preview-vs-current diff for cross-machine safety-profile sharing in <code>file://</code> mode). For core GS settings/coin-rule precision activation prep, Wave 3 also now includes disabled-by-default apply-mode scaffolding via system properties <code>abs.gs.phase8.precision.scaleReady.apply</code> and <code>abs.gs.phase8.precision.scaleReady.minorUnitScale</code> (safe default remains legacy scale=2), a versioned GS precision policy and generated verification matrix via <code>gs-server/deploy/config/phase8-precision-policy.json</code> and <code>gs-server/deploy/scripts/phase8-precision-verification-matrix.sh</code>, an offline history/reporting export precision vector gate via <code>gs-server/deploy/scripts/phase8-precision-history-reporting-export-vector-smoke.sh</code>, an offline wallet contract/rounding precision vector gate via <code>gs-server/deploy/scripts/phase8-precision-wallet-contract-vector-smoke.sh</code>, a non-prod canary readiness/evidence scaffold via <code>gs-server/deploy/scripts/phase8-precision-nonprod-canary-readiness-check.sh</code> + <code>gs-server/deploy/scripts/phase8-precision-nonprod-canary-evidence-pack.sh</code>, and an executable non-prod canary runner via <code>gs-server/deploy/scripts/phase8-precision-nonprod-canary-run.sh</code> (sandbox here blocks Docker daemon writes/restarts, so the last blocker still requires local execution with JVM flags + captured runtime evidence). For non-UI automation, use CLI compare/export: <code>gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-compare-export.sh</code> (named policy profiles with optional per-run threshold overrides).
        </p>
    </div>

    <div class="section">
        <h4>Current Runtime Status Snapshot</h4>
        <table class="table table-bordered table-condensed">
            <thead>
            <tr>
                <th>Check</th>
                <th>Current Known Status</th>
                <th>Reference</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Phase 4 runtime readiness</td>
                <td><span class="label label-warning">NOT_READY (last known)</span></td>
                <td><code>docs/52-phase4-runtime-readiness-check-tooling-20260220-175155.md</code></td>
            </tr>
            <tr>
                <td>Phase 5 runtime readiness</td>
                <td><span class="label label-warning">NOT_READY (last known)</span></td>
                <td><code>docs/55-phase5-runtime-readiness-check-tooling-20260220-180600.md</code></td>
            </tr>
            <tr>
                <td>Phase 5 gameplay evidence</td>
                <td><span class="label label-default">latest report captured</span></td>
                <td><code>docs/phase5/gameplay/phase5-gameplay-runtime-evidence-20260220-180650.md</code></td>
            </tr>
            <tr>
                <td>Phase 5 wallet evidence</td>
                <td><span class="label label-warning">NOT_READY (last known)</span></td>
                <td><code>docs/phase5/wallet/phase5-wallet-runtime-evidence-20260220-184505.md</code></td>
            </tr>
            <tr>
                <td>Phase 5 bonus/FRB evidence</td>
                <td><span class="label label-warning">NOT_READY (last known)</span></td>
                <td><code>docs/phase5/bonus-frb/phase5-bonus-frb-runtime-evidence-20260220-185313.md</code></td>
            </tr>
            <tr>
                <td>Phase 5 history evidence</td>
                <td><span class="label label-warning">NOT_READY (last known)</span></td>
                <td><code>docs/phase5/history/phase5-history-runtime-evidence-20260220-190016.md</code></td>
            </tr>
            <tr>
                <td>Phase 6 multiplayer evidence</td>
                <td><span class="label label-warning">NOT_READY (last known)</span></td>
                <td><code>docs/phase6/multiplayer/phase6-multiplayer-runtime-evidence-20260220-190732.md</code></td>
            </tr>
            </tbody>
        </table>
        <p class="small-note">
            Refresh this snapshot after each evidence-pack run by updating the reference report path.
        </p>
    </div>

    <div class="section">
        <h4>Phase 4 Protocol Adapter Runtime Check</h4>
        <pre><code>cd $REPO_ROOT
$REPO_ROOT/gs-server/deploy/scripts/phase4-runtime-readiness-check.sh
$REPO_ROOT/gs-server/deploy/scripts/phase4-protocol-security-logic-smoke.sh
$REPO_ROOT/gs-server/deploy/scripts/phase4-protocol-runtime-evidence-pack.sh \
  --bank-id 6275 \
  --transport host \
  --base-url http://127.0.0.1:18078 \
  --gs-base-url http://127.0.0.1:18081
# Optional runtime JSON security probe (requires non-prod HMAC secret configured in protocol-adapter):
$REPO_ROOT/gs-server/deploy/scripts/phase4-protocol-json-security-canary-probe.sh \
  --bank-id 6275 \
  --base-url http://127.0.0.1:18078 \
  --hmac-secret <non-prod-test-secret>
</code></pre>
        <p class="small-note">
            Output reports: <code>docs/phase4/protocol/phase4-protocol-runtime-evidence-*.md</code>
        </p>
        <p class="small-note">
            Default protocol-adapter and GS endpoints now come from <code>cluster-hosts.properties</code>; command flags can still override.
        </p>
        <p class="small-note">
            Local protocol security smoke validates JSON hash/replay logic offline (no runtime stack required) before canary/runtime probes.
        </p>
        <p class="small-note">
            Runtime JSON security probe is optional and can gracefully skip when runtime HMAC secret is unavailable (<code>--require-secret false</code> default).
        </p>
    </div>

    <div class="section">
        <h4>Phase 5 Gameplay + Redis Runtime Check</h4>
        <pre><code>cd $REPO_ROOT
$REPO_ROOT/gs-server/deploy/scripts/phase5-runtime-readiness-check.sh
$REPO_ROOT/gs-server/deploy/scripts/phase5-gameplay-runtime-evidence-pack.sh \
  --bank-id 6275 \
  --transport host \
  --gs-base-url http://127.0.0.1:18081 \
  --gameplay-base-url http://127.0.0.1:18074 \
  --require-redis-hit true
</code></pre>
        <p class="small-note">
            Output reports: <code>docs/phase5/gameplay/phase5-gameplay-runtime-evidence-*.md</code>
        </p>
        <p class="small-note">
            Default GS/gameplay/redis readiness endpoints now come from <code>cluster-hosts.properties</code>; command flags can still override.
        </p>
    </div>

    <div class="section">
        <h4>Phase 5 Wallet Adapter Runtime Check</h4>
        <pre><code>cd $REPO_ROOT
$REPO_ROOT/gs-server/deploy/scripts/phase5-wallet-runtime-readiness-check.sh
$REPO_ROOT/gs-server/deploy/scripts/phase5-wallet-runtime-evidence-pack.sh \
  --bank-id 6275 \
  --transport host \
  --gs-base-url http://127.0.0.1:18081 \
  --wallet-base-url http://127.0.0.1:18075
</code></pre>
        <p class="small-note">
            Output reports: <code>docs/phase5/wallet/phase5-wallet-runtime-evidence-*.md</code>
        </p>
        <p class="small-note">
            Default GS and wallet endpoints now come from <code>cluster-hosts.properties</code>; command flags can still override.
        </p>
    </div>

    <div class="section">
        <h4>Phase 5 Bonus/FRB Runtime Check</h4>
        <pre><code>cd $REPO_ROOT
$REPO_ROOT/gs-server/deploy/scripts/phase5-bonus-frb-runtime-readiness-check.sh
$REPO_ROOT/gs-server/deploy/scripts/phase5-bonus-frb-runtime-evidence-pack.sh \
  --bank-id 6275 \
  --transport host \
  --bonus-base-url http://127.0.0.1:18076
</code></pre>
        <p class="small-note">
            Output reports: <code>docs/phase5/bonus-frb/phase5-bonus-frb-runtime-evidence-*.md</code>
        </p>
        <p class="small-note">
            Default GS and bonus/FRB endpoints now come from <code>cluster-hosts.properties</code>; command flags can still override.
        </p>
    </div>

    <div class="section">
        <h4>Phase 5 History Runtime Check</h4>
        <pre><code>cd $REPO_ROOT
$REPO_ROOT/gs-server/deploy/scripts/phase5-history-runtime-readiness-check.sh
$REPO_ROOT/gs-server/deploy/scripts/phase5-history-runtime-evidence-pack.sh \
  --bank-id 6275 \
  --transport host \
  --history-base-url http://127.0.0.1:18077
</code></pre>
        <p class="small-note">
            Output reports: <code>docs/phase5/history/phase5-history-runtime-evidence-*.md</code>
        </p>
        <p class="small-note">
            Default GS and history endpoints now come from <code>cluster-hosts.properties</code>; command flags can still override.
        </p>
    </div>

    <div class="section">
        <h4>Phase 6 Multiplayer Runtime Check</h4>
        <p class="small-note">
            Default bank <code>6275</code> is configured with multiplayer disabled (<code>isMultiplayer=false</code> equivalent),
            so the policy probe should PASS with bypass reasons. Sync canary is optional and should only be enabled for a bank flagged multiplayer-enabled.
        </p>
        <p class="small-note">
            Default GS and multiplayer endpoints for these scripts now come from <code>cluster-hosts.properties</code> (external host keys); command flags can still override them.
        </p>
        <pre><code>cd $REPO_ROOT
$REPO_ROOT/gs-server/deploy/scripts/phase6-multiplayer-runtime-readiness-check.sh
$REPO_ROOT/gs-server/deploy/scripts/phase6-multiplayer-routing-policy-probe.sh \
  --bank-id 6275 \
  --game-id 838 \
  --transport host \
  --multiplayer-base-url http://127.0.0.1:18079
$REPO_ROOT/gs-server/deploy/scripts/phase6-multiplayer-runtime-evidence-pack.sh \
  --bank-id 6275 \
  --game-id 838 \
  --transport host \
  --multiplayer-base-url http://127.0.0.1:18079
</code></pre>
        <p class="small-note">
            Output reports: <code>docs/phase6/multiplayer/phase6-multiplayer-runtime-evidence-*.md</code>
        </p>
        <p class="small-note">
            Optional sync canary (only for multiplayer-enabled bank): add <code>--run-sync-canary true</code> to the evidence pack command.
        </p>
    </div>

    <div class="section">
        <h4>Phase 8 Precision / Min-Bet Audit Scan (GS-only)</h4>
        <pre><code>cd $REPO_ROOT
$REPO_ROOT/gs-server/deploy/scripts/phase8-precision-minbet-audit-scan.sh
# Optional deterministic vector smoke (0.001 / line-total exactness):
$REPO_ROOT/gs-server/deploy/scripts/phase8-precision-regression-vector-smoke.sh
# Wave 1 (reporting/display) deterministic cent/thousandth rounding vectors:
$REPO_ROOT/gs-server/deploy/scripts/phase8-precision-wave1-reporting-vector-smoke.sh
# Wave 1 NumberUtils.asMoney parity (legacy Math.round semantics guard):
$REPO_ROOT/gs-server/deploy/scripts/phase8-precision-wave1-numberutils-asmoney-parity-smoke.sh
# Wave 2 (game settings / coin rules) line-based normalization + nearest-coin vectors:
$REPO_ROOT/gs-server/deploy/scripts/phase8-precision-wave2-settings-coinrule-vector-smoke.sh
# Bucketed remediation planning report (safe wave order from audit hotspots):
$REPO_ROOT/gs-server/deploy/scripts/phase8-precision-remediation-buckets.sh
# Final non-prod canary close path (auto-closes Phase 8 if runtime evidence passes):
$REPO_ROOT/gs-server/deploy/scripts/phase8-precision-nonprod-canary-run.sh
# Manual closure only (after runtime canary evidence exists):
$REPO_ROOT/gs-server/deploy/scripts/phase8-precision-close-after-canary.sh
# Then sync dashboard embedded data if checklist/evidence changed:
$REPO_ROOT/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh
</code></pre>
        <p class="small-note">
            Output reports: <code>docs/phase8/precision/phase8-precision-minbet-audit-*.md</code>
        </p>
        <p class="small-note">
            Scope is GS-only (<code>game-server</code> + <code>refactor-services</code>) and is used to prioritize 0.001 precision remediation waves without changing runtime behavior yet.
        </p>
        <p class="small-note">
            Vector smoke is a non-runtime deterministic guard for exact thousandths conversion and line-total calculations before touching GS money arithmetic.
        </p>
        <p class="small-note">
            Bucket report converts raw scan output into a safe-first remediation wave plan (reporting/statistics -> settings -> config templates -> core financial paths).
        </p>
        <p class="small-note">
            Wave 1 vector smoke targets non-runtime reporting/display conversions only (2-decimal cent formatting and rounding boundaries) before any code changes.
        </p>
        <p class="small-note">
            Separate Wave 1 parity smoke protects legacy <code>NumberUtils.asMoney</code> Math.round behavior (including negative half-cent edge cases) during refactors.
        </p>
        <p class="small-note">
            Wave 2 vector smoke prepares line-based default bet normalization and nearest-coin selection tests before touching game settings/coin-rule code paths.
        </p>
        <p class="small-note">
            The Phase 8 non-prod canary runner now includes an auto-close finalizer: after successful runtime evidence capture it clears the last precision policy blocker, regenerates the matrix, marks <code>pu-precision-audit</code> done, and syncs the dashboard snapshot.
        </p>
    </div>

    <div class="section">
        <h4>Quick Links</h4>
        <ul>
            <li><a href="/support/modernizationProgress.html">/support/modernizationProgress.html</a></li>
            <li><a href="/support/modernizationDocs.jsp">/support/modernizationDocs.jsp</a></li>
            <li><a href="/support/configPortal.jsp">/support/configPortal.jsp</a></li>
            <li><a href="/support/clusterHosts.jsp">/support/clusterHosts.jsp</a></li>
        </ul>
    </div>
</div>
</body>
</html>

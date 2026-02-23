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
            <li>Work from <code>/Users/alexb/Documents/Dev/Dev_new</code>.</li>
            <li>Use bank canary <code>6275</code> for refactor checks.</li>
            <li>Do not modify legacy stack in <code>/Users/alexb/Documents/Dev</code>.</li>
        </ul>
    </div>

    <div class="section">
        <h4>Post-Change Local Verification (Required Before/After Feature Batches)</h4>
        <pre><code>cd /Users/alexb/Documents/Dev/Dev_new
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh
</code></pre>
        <p class="small-note">
            Generates offline verification report (syntax/help/config/manifest checks + executable local behavior smoke) under
            <code>docs/quality/local-verification/</code>. Use this even when runtime endpoints are unavailable.
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
        <pre><code>cd /Users/alexb/Documents/Dev/Dev_new
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-runtime-readiness-check.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-security-logic-smoke.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-runtime-evidence-pack.sh \
  --bank-id 6275 \
  --transport host \
  --base-url http://127.0.0.1:18078 \
  --gs-base-url http://127.0.0.1:18081
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
    </div>

    <div class="section">
        <h4>Phase 5 Gameplay + Redis Runtime Check</h4>
        <pre><code>cd /Users/alexb/Documents/Dev/Dev_new
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-runtime-readiness-check.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-gameplay-runtime-evidence-pack.sh \
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
        <pre><code>cd /Users/alexb/Documents/Dev/Dev_new
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-wallet-runtime-readiness-check.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-wallet-runtime-evidence-pack.sh \
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
        <pre><code>cd /Users/alexb/Documents/Dev/Dev_new
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-bonus-frb-runtime-readiness-check.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-bonus-frb-runtime-evidence-pack.sh \
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
        <pre><code>cd /Users/alexb/Documents/Dev/Dev_new
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-history-runtime-readiness-check.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-history-runtime-evidence-pack.sh \
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
        <pre><code>cd /Users/alexb/Documents/Dev/Dev_new
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-runtime-readiness-check.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-routing-policy-probe.sh \
  --bank-id 6275 \
  --game-id 838 \
  --transport host \
  --multiplayer-base-url http://127.0.0.1:18079
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-runtime-evidence-pack.sh \
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

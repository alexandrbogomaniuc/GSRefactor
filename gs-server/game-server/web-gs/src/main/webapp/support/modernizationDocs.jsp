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
                <td><code>docs/32-gs-config-portal-all-levels-spec.md</code><br/><code>docs/33-phase3-config-service-foundation.md</code></td>
            </tr>
            <tr>
                <td>Phase 4 - Protocol Adapter</td>
                <td>JSON/XML bank-mode adapter, parity, runtime probes</td>
                <td><code>docs/46-phase4-protocol-adapter-scaffold-20260220-171616.md</code><br/><code>docs/52-phase4-runtime-readiness-check-tooling-20260220-175155.md</code><br/><code>docs/phase4/</code></td>
            </tr>
            <tr>
                <td>Phase 5 - Core Extraction</td>
                <td>Session/gameplay/wallet/bonus/history scaffolds and canary tooling</td>
                <td><code>docs/34-phase5-session-service-foundation.md</code><br/><code>docs/53-phase5-gameplay-redis-state-blob-foundation-20260220-183300.md</code><br/><code>docs/58-phase5-gameplay-runtime-evidence-pack-tooling-20260220-180700.md</code><br/><code>docs/65-phase5-wallet-adapter-shadow-hook-and-canary-20260220-185600.md</code><br/><code>docs/66-phase5-wallet-runtime-evidence-pack-tooling-20260220-185700.md</code><br/><code>docs/phase5/</code></td>
            </tr>
            <tr>
                <td>Phase 6 - Multiplayer Separation</td>
                <td>Boundary and bypass architecture</td>
                <td><code>docs/39-phase6-multiplayer-boundary-and-bypass-v1.md</code></td>
            </tr>
            <tr>
                <td>Phase 7 - Cassandra Upgrade</td>
                <td>Plan, rehearsal, compatibility matrix and runbook</td>
                <td><code>docs/40-phase7-cassandra-upgrade-plan-v1.md</code><br/><code>docs/41-phase7-cassandra-rehearsal-checklist-v1.md</code><br/><code>docs/42-phase7-cassandra-cutover-rollback-runbook-v1.md</code><br/><code>docs/phase7/</code></td>
            </tr>
            <tr>
                <td>Phase 8 - Precision Modernization</td>
                <td>Requirements baseline for 0.001 path</td>
                <td><code>docs/19-requirements-from-user.md</code></td>
            </tr>
            <tr>
                <td>Phase 9 - Branding/Namespace</td>
                <td>ABS rename wave plan</td>
                <td><code>docs/30-phase9-abs-rename-wave-plan-gs-v1.md</code></td>
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

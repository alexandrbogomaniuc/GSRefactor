#!/usr/bin/env node
/**
 * i18n-check — Validate translation completeness across all games.
 *
 * For each game with a locales/ folder, compares all language directories
 * against the reference language (English) and reports missing keys per namespace.
 *
 * Usage:
 *   npx tsx tools/i18n-check/src/index.ts                     # check all games
 *   npx tsx tools/i18n-check/src/index.ts --game premium-slot # check one game
 *
 * Exit code 1 if missing keys are found.
 */

import fs from 'node:fs';
import path from 'node:path';

// ─── Types ───────────────────────────────────────────────────────────────────

interface KeyIssue {
    game: string;
    lang: string;
    namespace: string;
    missingKeys: string[];
    extraKeys: string[];
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

function loadJson(filePath: string): Record<string, string> {
    try {
        const raw = fs.readFileSync(filePath, 'utf-8');
        return JSON.parse(raw);
    } catch {
        return {};
    }
}

function getSubDirs(dirPath: string): string[] {
    if (!fs.existsSync(dirPath)) return [];
    return fs.readdirSync(dirPath, { withFileTypes: true })
        .filter((e: fs.Dirent) => e.isDirectory())
        .map((e: fs.Dirent) => e.name);
}

function getJsonFiles(dirPath: string): string[] {
    if (!fs.existsSync(dirPath)) return [];
    return fs.readdirSync(dirPath)
        .filter((f: string) => f.endsWith('.json'))
        .map((f: string) => f.replace('.json', ''));
}

// ─── Main Check Logic ────────────────────────────────────────────────────────

function checkGame(gameDir: string): KeyIssue[] {
    const gameName = path.basename(gameDir);
    const localesDir = path.join(gameDir, 'locales');

    if (!fs.existsSync(localesDir)) {
        return [];
    }

    const languages = getSubDirs(localesDir);
    if (!languages.includes('en')) {
        return [{
            game: gameName,
            lang: 'en',
            namespace: '*',
            missingKeys: ['ENTIRE_LANGUAGE_MISSING'],
            extraKeys: [],
        }];
    }

    // Get all namespaces from the reference language (en)
    const enDir = path.join(localesDir, 'en');
    const namespaces = getJsonFiles(enDir);

    const issues: KeyIssue[] = [];

    for (const lang of languages) {
        if (lang === 'en') continue;

        for (const ns of namespaces) {
            const refFile = path.join(localesDir, 'en', `${ns}.json`);
            const langFile = path.join(localesDir, lang, `${ns}.json`);

            const refKeys = Object.keys(loadJson(refFile));
            const langKeys = Object.keys(loadJson(langFile));

            const missingKeys = refKeys.filter(k => !langKeys.includes(k));
            const extraKeys = langKeys.filter(k => !refKeys.includes(k));

            if (missingKeys.length > 0 || extraKeys.length > 0) {
                issues.push({
                    game: gameName,
                    lang,
                    namespace: ns,
                    missingKeys,
                    extraKeys,
                });
            }
        }

        // Check if the language has namespace files that don't exist in en
        const langNamespaces = getJsonFiles(path.join(localesDir, lang));
        const extraNamespaces = langNamespaces.filter(ns => !namespaces.includes(ns));
        for (const ns of extraNamespaces) {
            issues.push({
                game: gameName,
                lang,
                namespace: ns,
                missingKeys: [],
                extraKeys: ['ENTIRE_NAMESPACE_EXTRA'],
            });
        }
    }

    return issues;
}

// ─── Report Formatting ──────────────────────────────────────────────────────

function formatReport(allIssues: KeyIssue[]): string {
    if (allIssues.length === 0) {
        return '✅ All translations are complete. No missing keys found.';
    }

    const lines: string[] = [
        '❌ Translation issues found:',
        '',
    ];

    for (const issue of allIssues) {
        lines.push(`  📦 ${issue.game} / ${issue.lang} / ${issue.namespace}`);
        if (issue.missingKeys.length > 0) {
            lines.push(`     Missing keys (${issue.missingKeys.length}):`);
            for (const key of issue.missingKeys) {
                lines.push(`       - ${key}`);
            }
        }
        if (issue.extraKeys.length > 0) {
            lines.push(`     Extra keys (not in en):`);
            for (const key of issue.extraKeys) {
                lines.push(`       + ${key}`);
            }
        }
        lines.push('');
    }

    return lines.join('\n');
}

// ─── CLI ─────────────────────────────────────────────────────────────────────

function parseArgs(argv: string[]): { game?: string } {
    let game: string | undefined;
    for (let i = 2; i < argv.length; i++) {
        if (argv[i] === '--game' && argv[i + 1]) {
            game = argv[++i];
        }
    }
    return { game };
}

const { game } = parseArgs(process.argv);
const projectRoot = path.resolve(process.cwd());
const gamesRoot = path.join(projectRoot, 'games');

console.log('🌐 i18n Check: validating translations...\n');

const gameDirs = getSubDirs(gamesRoot)
    .filter((g: string) => !game || g === game)
    .map((g: string) => path.join(gamesRoot, g));

if (gameDirs.length === 0) {
    console.log('  No game directories found.');
    process.exit(0);
}

let allIssues: KeyIssue[] = [];
let gamesChecked = 0;

for (const dir of gameDirs) {
    const localesDir = path.join(dir, 'locales');
    if (!fs.existsSync(localesDir)) continue;

    gamesChecked++;
    const gameName = path.basename(dir);
    const langs = getSubDirs(localesDir);
    const namespaces = getJsonFiles(path.join(localesDir, 'en'));
    console.log(`  📦 ${gameName}: ${langs.length} languages, ${namespaces.length} namespaces`);

    const issues = checkGame(dir);
    allIssues = allIssues.concat(issues);
}

console.log('');
console.log(formatReport(allIssues));

if (allIssues.length > 0) {
    process.exit(1);
} else {
    console.log(`\nChecked ${gamesChecked} game(s).`);
}


/**
 * @gamesv1/i18n — Lightweight i18n loader for the Igaming platform.
 *
 * Features:
 *   - Namespace-based translation files (common, paytable, rules)
 *   - Automatic fallback to 'en' when a key is missing
 *   - Optional brand overrides (CUSTOMER_BRAND_NAME substitution)
 *   - Safe: never crashes on missing keys — returns the key itself as fallback
 *   - Zero external dependencies
 */

// ─── Types ───────────────────────────────────────────────────────────────────

export type Namespace = 'common' | 'paytable' | 'rules';

export interface I18nConfig {
    /** Current language code (e.g. 'en', 'de', 'es') */
    language: string;
    /** Fallback language when a key is missing. Default: 'en' */
    fallbackLanguage?: string;
    /** Base URL or path prefix to load locale JSON files from */
    localesPath: string;
    /** List of namespaces to load. Default: ['common', 'paytable', 'rules'] */
    namespaces?: Namespace[];
    /** Optional brand overrides for template variables */
    brandOverrides?: Record<string, string>;
}

type TranslationMap = Record<string, string>;
type NamespaceStore = Record<string, TranslationMap>;
type LanguageStore = Record<string, NamespaceStore>;

// ─── I18n Class ──────────────────────────────────────────────────────────────

export class I18n {
    private store: LanguageStore = {};
    private language: string = 'en';
    private fallbackLanguage: string = 'en';
    private namespaces: Namespace[] = ['common', 'paytable', 'rules'];
    private brandOverrides: Record<string, string> = {};
    private _initialized: boolean = false;

    /**
     * Initialize the i18n system. Loads all namespace files for the
     * requested language and the fallback language.
     */
    public async init(config: I18nConfig): Promise<void> {
        this.language = config.language;
        this.fallbackLanguage = config.fallbackLanguage || 'en';
        this.namespaces = config.namespaces || ['common', 'paytable', 'rules'];
        this.brandOverrides = config.brandOverrides || {};

        const languagesToLoad = [this.language];
        if (this.fallbackLanguage !== this.language) {
            languagesToLoad.push(this.fallbackLanguage);
        }

        for (const lang of languagesToLoad) {
            this.store[lang] = {};
            for (const ns of this.namespaces) {
                this.store[lang][ns] = await this.loadNamespace(config.localesPath, lang, ns);
            }
        }

        this._initialized = true;
        console.log(`[i18n] Initialized: lang=${this.language}, fallback=${this.fallbackLanguage}, namespaces=${this.namespaces.join(',')}`);
    }

    /**
     * Translate a key. Format: "namespace:key" or just "key" (defaults to 'common').
     *
     * Supports interpolation: t('common:WELCOME', { name: 'Alex' })
     * will replace {{name}} with 'Alex'.
     *
     * Built-in brand variable: {{BRAND_NAME}} is replaced from brandOverrides.
     *
     * NEVER throws — returns the raw key if nothing is found.
     */
    public t(key: string, params?: Record<string, string | number>): string {
        const { ns, k } = this.parseKey(key);

        // Try current language
        let value = this.store[this.language]?.[ns]?.[k];

        // Fallback to fallback language
        if (value === undefined && this.fallbackLanguage !== this.language) {
            value = this.store[this.fallbackLanguage]?.[ns]?.[k];
        }

        // Ultimate fallback: return the key itself (never crash)
        if (value === undefined) {
            console.warn(`[i18n] Missing key: "${key}" (lang=${this.language})`);
            return k;
        }

        // Apply brand overrides as default interpolation variables
        const allParams: Record<string, string | number> = {
            ...this.brandOverrides,
            ...(params || {}),
        };

        // Interpolation: replace {{variable}} patterns
        return value.replace(/\{\{(\w+)\}\}/g, (_match, varName: string) => {
            const replacement = allParams[varName];
            if (replacement !== undefined) return String(replacement);
            // Safe: leave the placeholder if no value found
            return `{{${varName}}}`;
        });
    }

    /**
     * Change language at runtime (e.g., from dev switcher).
     * Reloads all namespaces for the new language.
     */
    public async changeLanguage(lang: string, localesPath: string): Promise<void> {
        this.language = lang;
        if (!this.store[lang]) {
            this.store[lang] = {};
            for (const ns of this.namespaces) {
                this.store[lang][ns] = await this.loadNamespace(localesPath, lang, ns);
            }
        }
        console.log(`[i18n] Language changed to: ${lang}`);
    }

    /** Get current language */
    public get currentLanguage(): string {
        return this.language;
    }

    /** Check if initialized */
    public get initialized(): boolean {
        return this._initialized;
    }

    /** Get all loaded keys for a namespace + language (for tooling/validation) */
    public getKeys(lang: string, ns: Namespace): string[] {
        return Object.keys(this.store[lang]?.[ns] || {});
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private parseKey(key: string): { ns: Namespace; k: string } {
        const parts = key.split(':');
        if (parts.length === 2) {
            return { ns: parts[0] as Namespace, k: parts[1] };
        }
        return { ns: 'common', k: key };
    }

    private async loadNamespace(basePath: string, lang: string, ns: Namespace): Promise<TranslationMap> {
        const url = `${basePath}/${lang}/${ns}.json`;
        try {
            const response = await fetch(url);
            if (!response.ok) {
                console.warn(`[i18n] Could not load ${url} (${response.status}), using empty map.`);
                return {};
            }
            return await response.json();
        } catch {
            console.warn(`[i18n] Failed to fetch ${url}, using empty map.`);
            return {};
        }
    }
}

// ─── Singleton ───────────────────────────────────────────────────────────────

/** Global singleton instance — import and use directly */
export const i18n = new I18n();


/**
 * Redaction layer to prevent logging or exposing secrets/tokens/passwords.
 * Replaces matched patterns with "[REDACTED]".
 */
export function redactSecrets(text: string): string {
    if (typeof text !== "string") return text;

    // Basic regex for common tokens, keys, passwords
    const patterns = [
        /(password|secret|token|key|auth|bearer)[\s="':]+([A-Za-z0-9_\-\.]+)/gi,
        /(ghp_[A-Za-z0-9_]+)/g, // GitHub classic PAT
        /(github_pat_[A-Za-z0-9_]+)/g // GitHub fine-grained PAT
    ];

    let redactedText = text;
    patterns.forEach(pattern => {
        redactedText = redactedText.replace(pattern, (match, prefix) => {
            // If it has a prefix (like 'password=' or 'Bearer '), keep the prefix. Otherwise just redact.
            return prefix ? `${prefix}: "[REDACTED]"` : "[REDACTED]";
        });
    });

    return redactedText;
}

export function safeLog(...args: any[]) {
    const redactedArgs = args.map(arg => {
        if (typeof arg === "string") return redactSecrets(arg);
        if (typeof arg === "object") return JSON.parse(redactSecrets(JSON.stringify(arg)));
        return arg;
    });
    console.log(...redactedArgs);
}

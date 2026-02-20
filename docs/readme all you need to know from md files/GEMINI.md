# AGENT OPERATIONAL RULES

## 1. DEBUGGING PROTOCOLS
*   **CHECK CONSOLE LOGS:** When diagnosing any web application or client-side issue, **ALWAYS** use the `chrome-devtools` MCP to check the browser console logs (`list_console_messages`). Do not rely solely on user descriptions.
*   **VERIFY NETWORK:** If console logs indicate connection errors, use `list_network_requests` to inspect the failing calls.

## 2. MCP BEST PRACTICES
*   **Target Specific Pages:** ALWAYS Call `list_pages` first, then explicitly use `select_page` (by ID) before querying logs or DOM.
*   **Handling Empty Logs:** If `list_console_messages` returns empty but errors are suspected:
    1.  **Do not assume no error.**
    2.  **Use Active Probing:** Use `evaluate_script` to dump critical global variables (e.g., `window.location`, `APP`, `window.config`) directly. This is the **Source of Truth** for the runtime state.
    3.  Example probe: `() => { return { loc: window.location.href, config: window.gameConfig } }`.

## 3. DOCUMENTATION ANALYSIS
*   **Full Scope:** When asked to read documentation, ensure you check subdirectories (Specs, Tests) and handle binary files by inferring context or reading adjacent metadata.

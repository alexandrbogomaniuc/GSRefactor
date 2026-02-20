# Troubleshooting Guide: JSP Syntax & Formatting Errors

## Issue: HTTP 500 Server Error on Game Launch
Symptoms: The game fails to load, and the browser displays an "HTTP ERROR 500" from Jetty/Jasper with a `JasperException: Unable to compile class for JSP`.

### Cause 1: Broken Java Syntax
In `template.jsp`, a `ThreadLog.info` call was incorrectly split across multiple lines without proper concatenation or closing quotes.
**Incorrect:**
```jsp
ThreadLog.info("MQ TMPL: DEBUG -
serverName=" + serverName + " , serverPort=" + serverPort);
```

### Cause 2: Tool-Induced Formatting Corruption
Automated file editing tools (like `write_to_file` or `multi_replace_file_content`) may sometimes wrap lines or add extreme indentation in JSP files. This can break Java variable declarations by separating the type from the variable name.
**Incorrect:**
```jsp
String
cdnUrl = request.getParameter(...);
```
This causes the compiler to fail because it sees `String` as an incomplete statement.

## Backup & Stability

### Known Good Backups
The following backup has been verified (SHA256 hash match) as the current working version:
- **Master Doc Backup:** [active_template.jsp.bak](file:///e:/Dev/Igaming%20Project/readme%20all%20you%20need%20to%20know%20from%20md%20files/active_template.jsp.bak)
- **Deployment Backup:** [template.jsp.bak](file:///e:/Dev/Igaming%20Project/Game%20provider%20side/Source%20code/mq-gs-clean-version/deploy/docker/GP3/deployments/ROOT/real/html5pc/template.jsp.bak)
- **Source Backup (MP Alternative):** [template.jsp](file:///e:/Dev/Igaming%20Project/Game%20provider%20side/Source%20code/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/mp/template.jsp)

### Documentation Storage
All technical findings and troubleshooting guides are consolidated in:
- **Master Doc Folder:** [readme all you need to know from md files](file:///e:/Dev/Igaming%20Project/readme%20all%20you%20need%20to%20know%20from%20md%20files/)

---

## Resolution Strategy

### 1. Unified Fixed Template
Always use a clean, well-formatted structure for `template.jsp`. Ensure that Java blocks `<% ... %>` have consistent line endings and no accidental line breaks within statements.

### 2. Surgical Write via PowerShell
To bypass formatting issues introduced by higher-level tools, use a native PowerShell HEREDOC to write the file directly. This ensures that the exact string content, including whitespace and line breaks, is preserved.

**Command Example:**
```powershell
$template = @'
[FULL JSP CONTENT HERE]
'@
Set-Content -Path "C:\path\to\template.jsp" -Value $template -NoNewline
```

### 3. Verify in All Locations
The project maintains `template.jsp` in multiple locations. Always synchronize the fix to both the source and deployment directories:
1. **Source:** [template.jsp](file:///e:/Dev/Igaming%20Project/Game%20provider%20side/Source%20code/mq-gs-clean-version/game-server/web-gs/src/main/webapp/real/html5pc/template.jsp)
2. **Deployment:** [template.jsp](file:///e:/Dev/Igaming%20Project/Game%20provider%20side/Source%20code/mq-gs-clean-version/deploy/docker/GP3/deployments/ROOT/real/html5pc/template.jsp)
3. **Container:** `/var/lib/jetty/webapps/ROOT/real/html5pc/template.jsp` (Verify via `docker exec`)

---

## Verification Steps
1. Search for `ThreadLog` and `String` declarations to ensure they are on single lines:
   ```powershell
   Select-String -Path "template.jsp" -Pattern "ThreadLog"
   ```
2. Check the browser console for `PIXI-CHECK` logs to confirm the JSP rendered and initialized the loader.
3. If a black screen persists, check for WebSocket connection errors in the Network tab.

---

## Issue: WebSocket Connection "undefined"
Symptoms: The game renders but displays a "Connection Error" or stays on a loading screen. Browser console shows: `WebSocket connection to 'ws://.../undefined' failed`.

### Cause: Missing URL Parameters
The game client (`game.js`) often expects critical parameters like `WEB_SOCKET_URL` and `SID` to be present in the **current page URL's query string**, even if they are also defined in `window.gameConfig`. If they are missing from the URL, the client-side `InteractionController` may default to `undefined`.

### Resolution: Self-Healing Redirect
Inject a small JavaScript block at the top of `template.jsp` (in the `<head>`) to detect missing parameters and redirect once with the correct values injected.

**Implementation:**
```javascript
(function() {
    var urlParams = new URLSearchParams(window.location.search);
    if (!urlParams.has('WEB_SOCKET_URL')) {
        var url = new URL(window.location.href);
        url.searchParams.set('WEB_SOCKET_URL', '<%=wsUrl%>');
        window.location.replace(url.toString());
    }
})();
```

---

## Backup & Stability

import * as fs from "fs";
import * as path from "path";

/**
 * Example implementation of the docs.scanLinks tool.
 * Extracts all URLs from provided docs and code, classifies as public/private/login-required.
 */
export async function scanLinksHandler(paths: string[]): Promise<any> {
    const foundLinks: { url: string; classification: string; file: string }[] = [];
    const urlRegex = /https?:\/\/[^\s"'<>;)]+/gi;

    for (const scanPath of paths) {
        try {
            if (!fs.existsSync(scanPath)) continue;

            const stat = fs.statSync(scanPath);
            if (stat.isFile()) {
                const content = fs.readFileSync(scanPath, "utf-8");
                let match;
                while ((match = urlRegex.exec(content)) !== null) {
                    const url = match[0];

                    // Classification logic based on keywords
                    let classification = "public";
                    if (url.includes("internal") || url.includes("localhost") || url.includes("127.0.0.1")) {
                        classification = "private";
                    } else if (url.includes("admin") || url.includes("login") || url.includes("auth")) {
                        classification = "login-required";
                    }

                    foundLinks.push({ url, classification, file: scanPath });
                }
            } else if (stat.isDirectory()) {
                // Recursive scanning logic can go here for directories
                // Placeholder for real implementation
            }
        } catch (e) {
            console.error(`Error scanning path ${scanPath}:`, e);
        }
    }

    return {
        scannedPaths: paths,
        totalLinksFound: foundLinks.length,
        links: foundLinks
    };
}

import js from "@eslint/js";
import prettier from "eslint-plugin-prettier/recommended";
import tseslint from "typescript-eslint";

export default tseslint.config(
  { ignores: ["dist"] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended, prettier],
    files: ["**/*.{ts,tsx}"],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module",
    },
    rules: {
      "no-restricted-globals": ["error", "WebSocket"],
      "no-restricted-properties": [
        "error",
        {
          object: "window",
          property: "postMessage",
          message: "Operator messaging is out of canonical runtime scope for Gamesv1.",
        },
      ],
    },
  },
);

# Launch Matrix

Use this exact sequence for a clean local restart of Game 7000 on the Beta 2C branch.

## Branch Checkout

From the repo root:

```bash
git fetch origin --prune
git checkout codex/qa/7000-beta2c-launch-matrix-20260309-1337
git pull --ff-only
```

## Kill Stale Vite Servers

Before starting any provider server, clear the three QA ports:

```bash
for port in 8081 8082 8083; do
  pids="$(lsof -tiTCP:$port -sTCP:LISTEN)"
  if [ -n "$pids" ]; then
    kill $pids
  fi
done
```

Verify the ports are free:

```bash
lsof -nP -iTCP:8081-8083 -sTCP:LISTEN
```

The command above should print nothing after the kill loop.

## Run The Provider Matrix

Start each provider from the repo root in its own terminal:

```bash
corepack pnpm -C Gamesv1/games/7000 dev:openai
```

```bash
corepack pnpm -C Gamesv1/games/7000 dev:nanobanana
```

```bash
corepack pnpm -C Gamesv1/games/7000 dev:donorlocal
```

## Open These URLs

- OpenAI: `http://127.0.0.1:8081/?allowDevFallback=1`
- NanoBanana: `http://127.0.0.1:8082/?allowDevFallback=1`
- donorlocal: `http://127.0.0.1:8083/?allowDevFallback=1`

If you want to inspect the active provider object in the browser console, run:

```js
window.__game7000ProviderPack
```

## Expected On-Screen QA Badge

The main screen should show a persistent provider badge with:

- `requested`
- `effective`
- `safePlaceholder`
- `missingKeys`
- `fallback` when a fallback reason exists

If a provider still shows placeholder tiles after a clean restart, capture:

```js
window.__game7000ProviderPack
```

and note the first failing asset request or frame fallback from the network/console output.

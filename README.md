# Chronos Garden

Where immutable data structures bloom into generative art.

Chronos Garden is a **sovereign temporal simulation**—a single-container Clojure application demonstrating how immutable state, persistent data structures, and REPL-driven development enable capabilities impossible in imperative architectures: scrubbing through millennia of organic growth in milliseconds, branching parallel timelines, and merging histories without corruption.

Built with **radical security transparency**: Distroless containers, zero-dynamic-resolution code, and zero-trust deployment on Fly.io's free tier ($0/month). No Kubernetes. No managed databases. No complexity without purpose.

![Status](https://img.shields.io/badge/security-distroless-brightgreen)
![Platform](https://img.shields.io/badge/platform-fly.io-blue)
![Cost](https://img.shields.io/badge/cost-$0-success)

## Key Features

- 🌱 **Temporal Engine:** Pure functions generating organic growth via persistent vectors
- ⏳ **Time Travel:** Scrub through 10,000+ immutable states at 60fps
- 🌿 **Branching Universes:** Split timelines, explore alternate histories, merge selected branches
- 🔒 **Security-First:** EDN Fortress input validation, non-root Distroless containers, read-only filesystems
- 💸 **Zero-Cost:** Runs entirely on Fly.io free tier (auto-stop when idle)
- ⚡ **REPL-Driven:** Modify growth algorithms live; history re-renders instantly via pure functions

## Architecture

**The Stack:**
- **Backend:** Clojure (JVM 21) · DataScript (immutable DB) · Ring
- **Frontend:** ClojureScript · Re-frame · HTML5 Canvas
- **Shared:** `.cljc` files for domain logic (true isomorphic Clojure)
- **Security:** Distroless (gcr.io/distroless/java21) · UID 65534 · Zero capabilities
- **Platform:** Fly.io (3 VM free tier) · Auto-stop enabled

**Security Invariants:**
- Zero dynamic resolution (no `resolve`, `eval`, `load-string`)
- EDN Fortress: Strict `clojure.edn/read-string` with whitelist-only tags
- Cryptographic seed derivation (SHA-256) preventing prediction attacks
- Immutable timeline: Merkle-ready state vectors with append-only audit logs

## Quick Start

### Local Development
```bash
git clone https://github.com/YOUR_USERNAME/chronos-garden.git
cd chronos-garden

# Start backend (port 8080)
clj -M -m chronos.main

# Start frontend (port 8280)
npx shadow-cljs watch app
```

Open `http://localhost:8280` in your browser.

### Deploy to Fly.io (Free Tier)
```bash
# Install flyctl
curl -L https://fly.io/install.sh | sh

# Authenticate
fly auth login

# Launch (creates app, doesn't deploy yet)
fly launch --name chronos-garden --no-deploy

# Set secrets
fly secrets set APP_SECRET=$(openssl rand -base64 32)

# Deploy
fly deploy

# Open in browser
fly open
```

**Verify Security:**
```bash
# Confirm non-root execution
fly ssh console -C "id"
# Expected: uid=65534(nonroot) gid=65534(nonroot)

# Confirm read-only filesystem
fly ssh console -C "touch /etc/test" 
# Expected: Read-only file system error (good!)
```

## Security Model

This application treats all infrastructure as hostile. The container is:

- **Distroless:** No shell, no package manager, minimal attack surface
- **Non-root:** Runs as UID 65534 (nobody), cannot install packages or modify system
- **Immutable:** Read-only root filesystem; only `/tmp` is writable (tmpfs)
- **Network-silent:** No egress except HTTPS (explicitly allowed in fly.toml)

See [SECURITY.md](SECURITY.md) for detailed threat model and incident response procedures.

## Why Clojure?

This project demonstrates **homoiconicity as a superpower**. The "DNA" of our generative organisms is literally Clojure data—S-expressions that can be safely manipulated, evolved, and executed via `sci` (Small Clojure Interpreter).

Immutability isn't a buzzword here; it's the entire product. Because our timeline is a persistent vector of immutable maps, we can:

- Keep 10,000 states in memory for $0 (structural sharing)
- Branch timelines instantly (O(1) persistence)
- Travel to any point in history (vector indexing)
- Survive arbitrary code pushes (pure functions re-evaluate history deterministically)

Try that with mutable state.

## Contributing

This is a reference architecture for secure Clojure deployment. Contributions should demonstrate:

- **Security:** No dynamic resolution, strict input validation, defense in depth
- **Simplicity:** Resist the urge to add Kubernetes, microservices, or complex orchestration
- **Performance:** Maintain 60fps scrubbing on 10,000+ state histories

Security-related changes require:
- Property-based tests (`test.check`)
- Static analysis pass (`clj-kondo`)
- Dependency vulnerability scan (`nvd-clojure`)
- Manual forensics review (grep for forbidden patterns)

See [DEPLOY.md](DEPLOY.md) for operational runbooks.

## License

MIT License - See [LICENSE](LICENSE) file.

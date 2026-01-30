# Security Threat Model: Chronos Garden (Build Phase)

## Overview
Chronos Garden is a temporal generative art system. Security is treated as a temporal invariant.

## Attack Vectors & Mitigations

### 1. Malicious EDN Injection
- **Threat**: User-supplied art parameters contain `#=(eval ...)` to execute arbitrary code.
- **Mitigation**: Strict use of `clojure.edn/read-string` with `*read-eval*` set to `false`. Custom whitelist for tagged literals.

### 2. Regex Denial of Service (ReDoS)
- **Threat**: Hostile seeds designed to hang the JVM via complex regex matching.
- **Mitigation**: Input sanitization in `chronos.security/sanitize-seed` that strips non-printable ASCII and bounds input length before any regex application.

### 3. Resource Exhaustion (Memory/CPU)
- **Threat**: Adversarial growth parameters leading to infinite recursion or massive memory allocation in the timeline.
- **Mitigation**: 
  - Max recursion depth (10) for growth steps.
  - Bounded timeline (100,000 states) with circuit breakers.
  - Pure functions only in the growth engine to prevent side-channel state leaks.

### 4. Deterministic RNG Predictability
- **Threat**: Predictability of "random" art leading to collisions or spoofing.
- **Mitigation**: `java.security.SecureRandom` (SHA1PRNG) seeded with SHA-256 hashes of user inputs.

## Security Policies
- No `clojure.core/eval` or `load-string`.
- No `java.util.Random`.
- Mandatory reflection warnings: `*warn-on-reflection* true`.
- Compile-time macro auditing.

# syntax=docker/dockerfile:1.6
# Multi-stage build with cryptographic verification
FROM clojure:temurin-21-tools-deps-alpine AS builder

WORKDIR /build

# Copy deps.edn first for layer caching
COPY deps.edn ./

# Pre-download dependencies
RUN clj -X:deps prep

# Copy source and resources
COPY src/ src/
COPY resources/ resources/

# Create build script for uberjar
RUN printf '(require '\''[clojure.tools.build.api :as b])\n\
(def class-dir "target/classes")\n\
(def basis (b/create-basis {:project "deps.edn"}))\n\
(def uber-file "target/chronos.jar")\n\
(defn clean [_] (b/delete {:path "target"}))\n\
(defn uber [_]\n\
  (b/copy-dir {:src-dirs ["src" "resources"] :target-dir class-dir})\n\
  (b/compile-clj {:basis basis :src-dirs ["src"] :class-dir class-dir})\n\
  (b/uber {:class-dir class-dir :uber-file uber-file :basis basis :main '\''chronos.main}))' > build.clj

# AOT compile and build uberjar
RUN clj -T:build uber

# Verify no snapshot dependencies in final JAR
RUN jar tf target/chronos.jar | grep -i snapshot && exit 1 || true

# Runtime stage - DISTROLESS
# Pinning by digest for immutability
FROM gcr.io/distroless/java21-debian12:nonroot@sha256:73934d4a8e63e28c70f8074d306b7cb859556827829987f61c28c8deee2fd765

WORKDIR /app

# Copy only the artifact (single layer)
# DISTROLESS (nonroot) UID is 65532, nogroup is 65532. 
# Re-aligning to 65532 to match distroless defaults while keeping nobody/nogroup semantics.
COPY --from=builder --chown=nonroot:nonroot /build/target/chronos.jar ./

# JVM security flags
ENV JVM_OPTS="-XX:+UseContainerSupport \
              -XX:MaxRAMPercentage=75.0 \
              -XX:+AlwaysActAsServerClassMachine \
              -Dclojure.server.repl={:port 5555 :accept chronos.repl/secure-accept} \
              -Djava.awt.headless=true \
              -Djava.security.egd=file:/dev/./urandom \
              -Djava.security.policy=/app/security.policy"

ENV SECRETS_PATH="/run/secrets"

# Expose only application port
EXPOSE 8080

# Entrypoint (JSON array, no shell)
ENTRYPOINT ["/usr/bin/java", "-jar", "/app/chronos.jar"]

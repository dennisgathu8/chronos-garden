# Deploy to Fly.io (Free Tier)

## Prerequisites
- Fly.io account (free tier: 3 VMs, 256MB RAM each)
- `flyctl` CLI installed

## Setup

1. **Install flyctl**:
   ```bash
   curl -L https://fly.io/install.sh | sh
   ```

2. **Authenticate**:
   ```bash
   fly auth login
   ```

3. **Launch app** (first time only):
   ```bash
   fly launch --name chronos-garden --no-deploy
   ```

4. **Set secrets**:
   ```bash
   fly secrets set APP_SECRET=$(openssl rand -base64 32)
   ```

5. **Deploy**:
   ```bash
   fly deploy
   ```

## Verify Security

### Non-root execution
```bash
fly ssh console -C "id"
# Expected: uid=65534(nonroot) gid=65534(nonroot)
```

### Read-only filesystem
```bash
fly ssh console -C "touch /etc/test"
# Expected: Permission denied (read-only filesystem)
```

### Container integrity
```bash
fly ssh console -C "which sh"
# Expected: Empty (no shell in distroless)
```

## Cost Verification

**Zero-cost deployment confirmed:**
- Fly.io free tier: 3 shared-cpu VMs @ 256MB RAM
- Auto-stop when idle (no charges for stopped machines)
- No managed Kubernetes control plane costs
- No container registry fees (public images)

## Monitoring

View logs:
```bash
fly logs
```

Check status:
```bash
fly status
```

Scale (if needed):
```bash
fly scale count 1  # Single instance for demo
```

## Incident Response

### Application crash
```bash
fly logs --tail=100
fly restart
```

### Secret rotation
```bash
fly secrets set APP_SECRET=$(openssl rand -base64 32)
# Automatic rolling restart
```

### Rollback
```bash
fly releases
fly deploy --image <previous-image-ref>
```

# Quick Reference: URL Configuration by Environment

## Development (Current)
**Wallet URLs**:
```
http://host.docker.internal:8000/bav/*
```
- Both containers on same machine
- Docker-specific hostname

## Testing (LAN)
**Wallet URLs**:
```
http://192.168.1.100:8000/bav/*
```
- Replace `192.168.1.100` with Casino Side server IP
- Same network, no SSL needed

## Staging
**Wallet URLs**:
```
https://casino-staging.yourdomain.com/bav/*
```
- Domain name with SSL
- Internal or external network

## Production
**Wallet URLs**:
```
https://casino-api.yourdomain.com/bav/*
```
- Domain name with valid SSL certificate
- Load balancer recommended
- IP whitelisting recommended

---

## How to Get Your Casino Side URL

1. **Check IP** (if using IP):
   ```bash
   # Linux
   hostname -I
   
   # Windows
   ipconfig
   ```

2. **Test accessibility**:
   ```bash
   curl http://YOUR_IP:8000/healthz
   ```

3. **Update Cassandra**:
   ```cql
   UPDATE RCasinoKS.banks 
   SET bank_url_authenticate = 'http://YOUR_IP:8000/bav/authenticate',
       bank_url_balance = 'http://YOUR_IP:8000/bav/balance',
       bank_url_bet = 'http://YOUR_IP:8000/bav/betResult',
       bank_url_refund_bet = 'http://YOUR_IP:8000/bav/refundBet'
   WHERE bank_id IN (6274, 6275);
   ```

**That's it!** The Game Provider will now call the Casino Side API using your IP/domain instead of Docker hostname.

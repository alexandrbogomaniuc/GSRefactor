#!/bin/bash
# Graceful shutdown script executed before container stops
# Deregister from load balancer before stopping the service

if [ -n "$SERVER_VM_IP" ] && [ -n "$LB_VM_IP" ]; then
    echo "[pre_stop] Deregistering from load balancer..."

    # Determine server type from container name or environment
    if echo "$HOSTNAME" | grep -q "mp"; then
        SERVER_TYPE="mp"
        if [ -f ~/.$SERVER_TYPE.sid ]; then
            SID=$(cat ~/.$SERVER_TYPE.sid)
            curl -s "http://${LB_VM_IP}/mp/dlb/remove?sid=${SID}" || echo "[pre_stop] Failed to deregister from load balancer"
        fi
    elif echo "$HOSTNAME" | grep -q "gs"; then
        SERVER_TYPE="gs"
        curl -s "http://${LB_VM_IP}/gs/dlb/remove?ip=${SERVER_VM_IP}&port=8080" || echo "[pre_stop] Failed to deregister from load balancer"
    fi

    echo "[pre_stop] Deregistration complete"
    sleep 2  # Allow time for load balancer to update
fi
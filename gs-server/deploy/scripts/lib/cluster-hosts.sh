#!/usr/bin/env bash
# Shared helper for reading deploy/config/cluster-hosts.properties with safe fallbacks.

cluster_hosts_config_file() {
  if [[ -n "${CLUSTER_HOSTS_FILE:-}" ]]; then
    printf '%s\n' "${CLUSTER_HOSTS_FILE}"
    return 0
  fi
  printf '%s\n' "/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties"
}

cluster_hosts_get() {
  local key="$1"
  local fallback="${2:-}"
  local file
  local value=""
  file="$(cluster_hosts_config_file)"

  if [[ -f "${file}" ]]; then
    value="$(
      awk -F= -v key="${key}" '
        /^[[:space:]]*#/ {next}
        /^[[:space:]]*$/ {next}
        {
          k=$1
          v=substr($0, index($0, "=") + 1)
          gsub(/^[[:space:]]+|[[:space:]]+$/, "", k)
          gsub(/^[[:space:]]+|[[:space:]]+$/, "", v)
          if (k == key) {
            print v
            exit
          }
        }
      ' "${file}"
    )"
  fi

  if [[ -n "${value}" ]]; then
    printf '%s\n' "${value}"
  else
    printf '%s\n' "${fallback}"
  fi
}

cluster_hosts_http_url() {
  local host_key="$1"
  local port_key="$2"
  local fallback_host="$3"
  local fallback_port="$4"
  local host
  local port
  host="$(cluster_hosts_get "${host_key}" "${fallback_host}")"
  port="$(cluster_hosts_get "${port_key}" "${fallback_port}")"
  printf 'http://%s:%s\n' "${host}" "${port}"
}

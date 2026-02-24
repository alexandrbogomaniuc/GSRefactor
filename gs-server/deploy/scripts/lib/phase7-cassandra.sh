#!/usr/bin/env bash
# Shared helpers for Phase 7 Cassandra scripts (Docker/cqlsh wrappers and degraded-mode detection).

phase7_docker_bin() {
  printf '%s\n' "${DOCKER_BIN:-docker}"
}

phase7_is_docker_api_denied_output() {
  local text="${1:-}"
  [[ "${text}" == *"permission denied while trying to connect to the docker API"* ]]
}

phase7_cqlsh_exec() {
  local container="$1"
  local cql="$2"
  local output
  local docker_bin
  docker_bin="$(phase7_docker_bin)"

  if output="$("${docker_bin}" exec "${container}" cqlsh -e "${cql}" 2>&1)"; then
    printf '%s\n' "${output}"
    return 0
  fi

  local code=$?
  if phase7_is_docker_api_denied_output "${output}"; then
    printf '%s\n' "${output}" >&2
    return 3
  fi

  printf '%s\n' "${output}" >&2
  return "${code}"
}

phase7_write_docker_api_denied_stub() {
  local out_file="$1"
  local container="$2"
  local scope="$3"
  mkdir -p "$(dirname "${out_file}")"
  {
    echo "timestamp_utc=$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
    echo "container=${container}"
    echo "status=SKIP_DOCKER_API_DENIED"
    echo "scope=${scope}"
    echo "reason=docker_api_socket_permission_denied"
  } > "${out_file}"
}

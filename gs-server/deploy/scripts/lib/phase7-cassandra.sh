#!/usr/bin/env bash
# Shared helpers for Phase 7 Cassandra scripts (Docker/cqlsh wrappers and degraded-mode detection).

phase7_docker_bin() {
  printf '%s\n' "${DOCKER_BIN:-docker}"
}

phase7_refactor_compose_file() {
  printf '%s\n' "${PHASE7_REFACTOR_COMPOSE_FILE:-/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml}"
}

phase7_refactor_compose_env_file() {
  printf '%s\n' "${PHASE7_REFACTOR_COMPOSE_ENV_FILE:-/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/.env}"
}

phase7_refactor_compose_project() {
  printf '%s\n' "${PHASE7_REFACTOR_COMPOSE_PROJECT:-refactor}"
}

phase7_container_to_refactor_service() {
  local container="$1"
  local project
  project="$(phase7_refactor_compose_project)"
  # Compose container name: <project>-<service>-<index>
  local base="${container%-*}"
  local suffix="${container##*-}"
  if [[ "${suffix}" =~ ^[0-9]+$ && "${base}" == "${project}-"* ]]; then
    printf '%s\n' "${base#${project}-}"
    return 0
  fi
  return 1
}

phase7_cqlsh_exec_compose_fallback() {
  local container="$1"
  local cql="$2"
  local service
  service="$(phase7_container_to_refactor_service "${container}")" || return 9
  docker compose -p "$(phase7_refactor_compose_project)" \
    -f "$(phase7_refactor_compose_file)" \
    --env-file "$(phase7_refactor_compose_env_file)" \
    exec -T "${service}" sh -lc 'CQLSH_BIN="$(command -v cqlsh || true)"; if [ -z "$CQLSH_BIN" ] && [ -x /opt/cassandra/bin/cqlsh ]; then CQLSH_BIN=/opt/cassandra/bin/cqlsh; fi; if [ -z "$CQLSH_BIN" ]; then echo "cqlsh_not_found" >&2; exit 127; fi; "$CQLSH_BIN" -e "$1"' -- "${cql}"
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
    if output="$(phase7_cqlsh_exec_compose_fallback "${container}" "${cql}" 2>&1)"; then
      printf '%s\n' "${output}"
      return 0
    fi
    local compose_code=$?
    printf '%s\n' "${output}" >&2
    if [[ ${compose_code} -eq 9 ]]; then
      return 3
    fi
    return "${compose_code}"
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

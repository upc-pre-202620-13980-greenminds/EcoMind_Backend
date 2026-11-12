#!/usr/bin/env bash
set -euo pipefail

# Keep this project's CLI session separate from other Azure accounts.
export AZURE_CONFIG_DIR="${ECOMIND_AZURE_CONFIG_DIR:-${XDG_CONFIG_HOME:-$HOME/.config}/ecomind/azure}"
umask 077
mkdir -p "$AZURE_CONFIG_DIR"
exec az "$@"

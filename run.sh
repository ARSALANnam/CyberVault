#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
[ -f CyberVault.jar ] || ./build.sh
java -jar CyberVault.jar
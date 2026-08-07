#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

rm -rf build && mkdir -p build
javac -encoding UTF-8 -d build src/CyberVault.java

EXTRA=""
if [ -f assets/icon.png ]; then
  cp assets/icon.png build/
  EXTRA="icon.png"
fi

if [ -f assets/tray.png ]; then
  cp assets/tray.png build/
  EXTRA="$EXTRA tray.png"
fi

cd build
echo "Main-Class: CyberVault" > manifest.mf
jar cfm ../CyberVault.jar manifest.mf CyberVault*.class $EXTRA
cd ..
echo "✅ Built: $(pwd)/CyberVault.jar"

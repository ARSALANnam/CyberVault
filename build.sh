#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

rm -rf build && mkdir -p build
javac -encoding UTF-8 -d build src/CyberVault.java

EXTRA=""
if [ -d assets ]; then
    mkdir -p build/assets
    cp -r assets/. build/assets/
    EXTRA="assets"
fi

cd build
echo "Main-Class: CyberVault" > manifest.mf
jar cfm ../CyberVault.jar manifest.mf CyberVault*.class $EXTRA
cd ..
echo "✅ Built: $(pwd)/CyberVault.jar"

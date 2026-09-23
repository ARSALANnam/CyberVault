#!/bin/bash

mkdir -p build

find src -name "*.java" > sources.txt

javac -encoding UTF-8 -d build @sources.txt

jar cfe CyberVault.jar CyberVault -C build .

rm sources.txt

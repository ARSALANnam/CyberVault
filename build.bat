@echo off
if exist build rmdir /s /q build
mkdir build
javac -encoding UTF-8 -d build src\main.CyberVault.java || exit /b 1
cd build
echo Main-Class: main.CyberVault> manifest.mf
jar cfm ..\main.CyberVault.jar manifest.mf main.CyberVault*.class
cd ..
echo.
echo Built: main.CyberVault.jar
echo Run:   java -jar main.CyberVault.jar
pause

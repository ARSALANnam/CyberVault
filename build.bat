@echo off
if exist build rmdir /s /q build
mkdir build
javac -encoding UTF-8 -d build src\CyberVault.java || exit /b 1
cd build
echo Main-Class: CyberVault> manifest.mf
jar cfm ..\CyberVault.jar manifest.mf CyberVault*.class
cd ..
echo.
echo Built: CyberVault.jar
echo Run:   java -jar CyberVault.jar
pause
@echo off
if not exist CyberVault.jar call build.bat
start "" javaw -jar CyberVault.jar
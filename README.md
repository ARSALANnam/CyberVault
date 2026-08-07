<h1 align="center">CYBERVAULT</h1>

<p align="center">
  A cyberpunk-styled, fully offline password manager &amp; API token vault.<br>
  Pure Java Swing. Zero dependencies. AES-256-GCM encrypted.
</p>



<p align="center"><img src="assets/ScreeShot0-1.png" width="700"></p>
<p align="center"><img src="assets/ScreeShot0-2.png" width="700"></p>
<p align="center"><img src="assets/ScreeShot0-3.png" width="700"></p>
<p align="center"><img src="assets/ScreeShot0-4.png" width="700"></p>

| | |
|---|---|
| 🔐 | **Master-key vault** — PBKDF2 (120k iterations) + AES-256-GCM authenticated encryption |
| 👤 | **Password entries** — title, username / email, password, URL & notes |
| 🤖 | **API token vault** — keep Hugging Face, OpenAI, GitHub… tokens safe |
| ⚡ | **Password generator** — 8–64 chars, custom pools, no-ambiguous mode, live entropy meter |
| 🔎 | **Live search** across entries and tokens |
| 👁 | **Show / hide** secrets with one click |
| 📋 | **One-click copy** — clipboard auto-clears after 20 s |
| 🔒 | **Lock vault** — wipes keys & decrypted data from RAM |
| 🌃 | **Neon cyberpunk UI** — custom-painted buttons, slider, scrollbars & hex logo |
| 💾 | **100% offline** — data lives only in `~/.cybervault/vault.dat` |


## Requirements
- JDK 8+

## Build
```bash
./build.sh        # Linux / macOS
build.bat         # Windows
```

# Run
java -jar CyberVault.jar

# Security
- Vault file: ~/.cybervault/vault.dat (encrypted)
- The master key is never stored and cannot be recovered.

# ScreenShots 

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-8%2B-orange?style=flat-square">
  <img alt="Crypto" src="https://img.shields.io/badge/Crypto-AES--256--GCM-00d9ff?style=flat-square">
  <img alt="Dependencies" src="https://img.shields.io/badge/Dependencies-0-brightgreen?style=flat-square">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-ff2a6d?style=flat-square">
  <img alt="Release" src="https://img.shields.io/github/v/release/ARSALANnam/CyberVault?style=flat-square">
</p>

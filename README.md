<h1 align="center">CYBERVAULT</h1>

<p align="center">
  A cyberpunk-styled, fully offline password manager &amp; API token vault.<br>
  Pure Java Swing. Zero dependencies. AES-256-GCM encrypted.
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-8%2B-orange?style=flat-square">
  <img alt="Crypto" src="https://img.shields.io/badge/Crypto-AES--256--GCM-00d9ff?style=flat-square">
  <img alt="Dependencies" src="https://img.shields.io/badge/Dependencies-0-brightgreen?style=flat-square">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-ff2a6d?style=flat-square">
  <img alt="Release" src="https://img.shields.io/github/v/release/ARSALANnam/CyberVault?style=flat-square">
  <img alt="AI" src="https://img.shields.io/badge/Human%20%2B-AI-9d4eff?style=flat-square">
</p>


## ✨ Features

- 🔐  **Master-key vault** — PBKDF2 (120k iterations) + AES-256-GCM authenticated encryption 
- 👤  **Password entries** — title, username / email, password, URL & notes 
- 🤖  **API token vault** — keep Hugging Face, OpenAI, GitHub… tokens safe 
- ⚡  **Password generator** — 8–64 chars, custom pools, no-ambiguous mode, live entropy meter 
- 🔎  **Live search** across entries and tokens 
- 👁  **Show / hide** secrets with one click 
- 📋  **One-click copy** — clipboard auto-clears after 20 s 
- 🔒  **Lock vault** — wipes keys & decrypted data from RAM 
- 🌃  **Neon cyberpunk UI** — custom-painted buttons, slider, scrollbars & hex logo 
- 💾  **100% offline** — data lives only in `~/.cybervault/vault.dat` 


# 🖼 More screenshots

<p align="center"><img src="assets/ScreeShot0-1.png" width="700"></p>
<p align="center"><img src="assets/ScreeShot0-2.png" width="700"></p>
<p align="center"><img src="assets/ScreeShot0-3.png" width="700"></p>
<p align="center"><img src="assets/ScreeShot0-4.png" width="700"></p>



## Requirements
- JDK 8+



<br>
<br>

## 🚀 Quick start

**Linux / macOS**

```bash
./run.sh          # builds if needed, then runs
```

**Windows**

```bat
run.bat
```

Manual:

```bash
./build.sh                 # or build.bat on Windows
java -jar CyberVault.jar
```

**First run:** create a master key (min 6 chars) → it is **never stored and cannot be recovered** → start adding entries.


<br>
<br>

## 🔐 Security model

```
master key ──PBKDF2-HMAC-SHA256 (120 000 iters, 16 B salt)──▶ AES-256 key
vault data ──serialize──▶ AES/GCM/NoPadding (12 B IV, 128-bit tag) ──▶ vault.dat
```

File format:

```
[ salt · 16 B ][ IV · 12 B ][ ciphertext + auth tag ]
```

- Wrong master key **or** a single tampered bit → decryption fails (GCM authentication).
- Passwords are handled as `char[]` and zeroed in memory after use.
- **Lock** clears key, salt and decrypted data from RAM.
- Clipboard is cleared 20 s after copying (only if unchanged).

> ⚠️ This is a personal project. Use it at your own risk and **don't forget your master key**.



<br>
<br>

## 🤖 AI Assistance

This project was built with an AI pair programmer ([Qwen](https://qwen.ai)).
Every line of code was reviewed, understood and tested by me before shipping —
the AI accelerated the process, but the decisions (and the bugs 😄) are mine.

> Transparency matters: you deserve to know how the software you trust is made.

<br>
<br>

## 🧾 License

MIT — see [LICENSE](LICENSE).

---

<br>
<br>
<p align="center">Made with ⚡, pure Java Swing & an AI pair programmer</p>
<p align="center">May The Force Be With You</p>

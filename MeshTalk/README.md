# MeshTalk

MeshTalk is a decentralized, Bluetooth mesh-powered messaging app inspired by Jack Dorsey's Bitchat protocol, wrapped in a WhatsApp-style UI. No central server, no metadata collection—just pure peer-to-peer messaging.

## Features
- Bluetooth mesh networking (multi-hop, peer discovery)
- End-to-end encrypted messaging
- WhatsApp-inspired UI (chats, contacts, settings)
- Message persistence (Room database)
- QR code contact sharing (planned)
- Delivery/read receipts (planned)

## Getting Started

### Prerequisites
- Android Studio (Giraffe or newer)
- Android device with Bluetooth LE support (API 24+)

### Build & Run
1. Clone this repo:
   ```sh
   git clone <your-repo-url>
   ```
2. Open `MeshTalk` in Android Studio.
3. Build and run on a real device (emulators do not support Bluetooth mesh).

### Permissions
- Bluetooth, BLE, and location permissions are required for mesh networking.

## Project Structure
- `app/` — Main Android app (UI, navigation)
- `meshcore/` — Bluetooth mesh logic
- `data/` — Room database (messages)
- `crypto/` — Encryption utilities (libsodium)

## Roadmap
- [x] WhatsApp-style UI
- [x] Bluetooth mesh core (inspired by Bitchat)
- [ ] Media/file transfer
- [ ] QR code contact sharing
- [ ] Delivery/read receipts
- [ ] Internet fallback (optional)

## Credits
- Inspired by [Bitchat](https://github.com/permissionlesstech/bitchat-android)
- UI inspired by WhatsApp

## License
MIT
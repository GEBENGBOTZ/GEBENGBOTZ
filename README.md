# GEBENGBOTZ Remote Support

Android companion + web dashboard + relay server.

## MVP capabilities
- User-started Android connection with a visible foreground notification
- Pairing-code protected WebSocket
- Online device list
- Device model, Android version and battery status
- Explicit request/response for status diagnostics

No hidden monitoring, arbitrary shell commands, clipboard harvesting, file scraping, or remote lock/freeze commands.

## APK build
GitHub Actions builds a debug APK on push and on manual workflow dispatch.
Open **Actions → Android APK → run → Artifacts**.

## Relay server
See [server README](server/README.md). Set a strong private `PAIRING_CODE`, deploy behind HTTPS/WSS, then enter the WSS endpoint in both dashboard and Android app.

## Dashboard
Dashboard source: `dashboard/index.html`. After deploying the relay, open the root URL and enter the WSS endpoint and pairing code.

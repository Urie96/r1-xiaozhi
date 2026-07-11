# Local HTTP API

R1 HTTP server listens on port `8089`. Open `http://<speaker-ip>:8089/` from a computer on the same network to use the built-in control page.

The page is embedded in the injected Java code instead of APK assets because the Janus build preserves the signed ZIP body from `device_original.apk`; adding an asset would modify that signed body.

## Web Console

### `GET /`

### `GET /index.html`

Both paths return the same self-contained HTML page. Its CSS and JavaScript are inline, and it uses relative `fetch` requests to read status, update configuration, and control the speaker.

## Unified Status

### `GET /api/status`

This is the only GET API. It returns all status and saved configuration in one response:

```json
{
  "ok": true,
  "config": {
    "vadFrontSilenceMs": 20000,
    "vadBackSilenceMs": 400,
    "wsUrl": "ws://home.lubui.com:3116/ws",
    "enterAsrDelayMs": 500,
    "multiTurnEnabled": true,
    "restartRequired": false
  },
  "bluetooth": {
    "ok": true,
    "action": "status",
    "supported": true,
    "adapterEnabled": false,
    "adapterState": 10,
    "bluetoothMode": false
  },
  "volume": {
    "ok": true,
    "action": "status",
    "current": 7,
    "max": 15,
    "percent": 47
  },
  "asr": {
    "ok": true,
    "action": "status",
    "contextReady": true,
    "engineState": 0,
    "wakeup": true,
    "asr": false,
    "recognition": false,
    "ttsPlaying": false
  },
  "sleep": {
    "ok": true,
    "action": "status",
    "sleeping": false,
    "deviceStatus": 0,
    "contextReady": true
  }
}
```

The old GET APIs `/health`, `/api/config`, `/api/bluetooth`, `/api/volume`, `/api/asr`, and `/api/sleep` have been removed and return `404`.

## XiaoZhi Configuration

### `POST /api/config`

Validates and persists a partial JSON object. Configuration is loaded when the APK process starts, so restart the process or device when `restartRequired` is `true`.

| Field | Range / default | Description |
| --- | --- | --- |
| `vadFrontSilenceMs` | `1000–120000`, default `20000` | Maximum silence before the user starts speaking. |
| `vadBackSilenceMs` | `100–5000`, default `400` | Silence used to detect the end of speech. |
| `wsUrl` | default `ws://home.lubui.com:3116/ws` | XiaoZhi WebSocket URL using `ws`, `wss`, `http`, or `https`. |
| `enterAsrDelayMs` | `0–10000`, default `500` | Delay before entering ASR after playback in multi-turn mode. |
| `multiTurnEnabled` | default `true` | Enters ASR after playback when true; returns to wakeup when false. |

Unknown fields and invalid values return `400`; no settings are saved unless the entire request is valid.

```sh
curl -X POST http://<speaker-ip>:8089/api/config \
  -H 'Content-Type: application/json' \
  -d '{"vadFrontSilenceMs":15000,"vadBackSilenceMs":600,"multiTurnEnabled":false}'
```

## Speaker Controls

### Bluetooth

- `POST /api/bluetooth/on` enables the adapter when needed and enters R1 Bluetooth mode through the original triple-click path.
- `POST /api/bluetooth/off` leaves R1 Bluetooth mode without force-disabling the Android adapter.

### Volume

- `POST /api/volume/up` raises volume one step.
- `POST /api/volume/down` lowers volume one step.
- `POST /api/volume/max` sets maximum volume.
- `POST /api/volume/min` sets minimum volume.
- `POST /api/volume/set` accepts query `level=<n>` or `percent=<n>`, or JSON `{ "level": n }` / `{ "percent": n }`.

```sh
curl -X POST 'http://<speaker-ip>:8089/api/volume/set?percent=40'
```

### ASR

- `POST /api/asr/wakeup` triggers `ExoConstants.DO_ENTER_ASR_BY_MIC` on the ANT pipeline. It returns `503` when the ANT context is not ready.

### Sleep

- `POST /api/sleep/start` starts sleep through the original double-click path.
- `POST /api/sleep/end` ends sleep through the original one-click path.

The original sleep state transition is asynchronous. Refresh `GET /api/status` to confirm the settled state.

## R1 ASR Traffic Router

### `POST /trafficRouter/cs`

Compatibility endpoint used by the original R1 native ASR pipeline. It accepts the original traffic-router request format and forwards captured Opus frames to the XiaoZhi upstream session.

| Body | Behavior |
| --- | --- |
| Empty body, no active session | Starts an upstream turn and returns the original-style init acknowledgement. |
| Non-empty body | Treats the body as length-prefixed R1 Opus frames and forwards them upstream. |
| Empty body, active session exists | Finishes the upstream turn and returns a fake NLU payload containing the turn UUID. |

The `UI` header supplies the device ID and defaults to `feixun-r1`. The bridge extracts the session ID from the first bracket pair in the original `P` header.

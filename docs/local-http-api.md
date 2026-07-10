# Local HTTP API

R1 local HTTP server listens on `127.0.0.1:8089` and only accepts the canonical paths below. Removed aliases return `404`.

## System

### `GET /health`

Health probe.

Response:

```json
{"status":"ok"}
```

## R1 ASR Traffic Router

### `POST /trafficRouter/cs`

Compatibility endpoint used by the original R1 native ASR pipeline. It accepts the original traffic-router request format and forwards captured Opus frames to the XiaoZhi upstream session.

Headers used by the bridge:

| Header | Description |
| --- | --- |
| `UI` | Device id. Defaults to `feixun-r1` when missing. |
| `P` | Original traffic-router parameter header. The bridge extracts `SID` from the first bracket pair. |

Request body behavior:

| Body | Behavior |
| --- | --- |
| Empty body, no active session | Starts an upstream turn and returns the original-style init ack. |
| Non-empty body | Treats body as length-prefixed R1 Opus frames and forwards frames upstream. |
| Empty body, active session exists | Finishes the upstream turn and returns a fake NLU payload containing the upstream turn UUID. |

## Bluetooth

### `GET /api/bluetooth`

Returns Bluetooth adapter and R1 Bluetooth mode status.

Response fields:

| Field | Description |
| --- | --- |
| `ok` | Whether the request succeeded. |
| `action` | `status`. |
| `supported` | Whether Android has a Bluetooth adapter. |
| `adapterEnabled` | Present when an adapter exists. |
| `adapterState` | Present when an adapter exists. Android adapter state integer. |
| `bluetoothMode` | Whether the R1 device status is Bluetooth mode. |
| `error` | Present on failure. |

### `POST /api/bluetooth/on`

Enters R1 Bluetooth mode through the original triple-click control path and enables the adapter when needed.

### `POST /api/bluetooth/off`

Leaves R1 Bluetooth mode through the original device control path. It does not force-disable the Android adapter.

Bluetooth action responses include the status fields above plus `requestedBluetoothMode`.

## Volume

### `GET /api/volume`

Returns current volume status.

Response fields:

| Field | Description |
| --- | --- |
| `ok` | Whether the request succeeded. |
| `action` | `status`, `up`, `down`, `max`, `min`, or `set`. |
| `current` | Current Android volume level. |
| `max` | Maximum Android volume level. |
| `percent` | Current volume as an integer percentage. |
| `requestedLevel` | Present when an absolute level was requested. |
| `requestedPercent` | Present when a percentage was requested. |
| `error` | Present on failure. |

### `POST /api/volume/up`

Raises volume by one original volume step.

### `POST /api/volume/down`

Lowers volume by one original volume step.

### `POST /api/volume/max`

Sets volume to maximum.

### `POST /api/volume/min`

Sets volume to minimum.

### `POST /api/volume/set`

Sets volume by absolute level or percentage.

Accepted inputs:

| Input | Description |
| --- | --- |
| Query `level=<n>` | Absolute Android volume level. Must be between `1` and `max`. |
| Query `percent=<n>` | Percent volume. Must be between `0` and `100`. |
| JSON body `{ "level": n }` | Absolute Android volume level. |
| JSON body `{ "percent": n }` | Percent volume. |

Example:

```sh
curl -X POST 'http://127.0.0.1:8089/api/volume/set?percent=40'
```

## ASR

### `GET /api/asr`

Returns ANT engine readiness and speech state.

Response fields:

| Field | Description |
| --- | --- |
| `ok` | Whether the request succeeded. |
| `action` | `status` or `wakeupAsr`. |
| `contextReady` | Whether the bridge has received an `ANTHandlerContext`. |
| `engineState` | Present when context is ready. Original ANT engine state. |
| `wakeup` | Present when context is ready. Whether wakeup is active. |
| `asr` | Present when context is ready. Whether ASR is active. |
| `recognition` | Present when context is ready. Whether recognition is active. |
| `ttsPlaying` | Present when context is ready. Whether TTS is playing. |
| `stateError` | Present if engine state reading fails. |
| `error` | Present on request failure. |

### `POST /api/asr/wakeup`

Triggers `ExoConstants.DO_ENTER_ASR_BY_MIC` on the ANT pipeline to enter microphone ASR.

Returns `503` when the ANT context has not been registered yet.

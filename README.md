# FFAGameV2

Professional multi-arena FFA minigame plugin for Nukkit/Cloudburst servers.  
This project is a full rewrite with clean architecture, form-only UX, JSON-first content storage, and optional MySQL profile storage.

## Highlights

- Full multi-arena system with per-arena world binding.
- Arena-safe zones with 2-click selection mode (auto-save).
- Form-only UX (`/ffa`, `/ffaadmin`) with strict sector routing.
- Advanced kit system (snapshot, item manager, armor manager, ordered remove flow).
- Live arena scoreboard (arena-only, per-player toggle, EN/DE language support).
- Profile/stat persistence by UUID with:
  - `MYSQL` (recommended for production), or
  - `JSON` fallback/alternative.
- Runtime database outage protection:
  - stats service goes unavailable,
  - all arenas forced offline,
  - 3 reconnect attempts in 15s intervals,
  - plugin disables itself if DB cannot recover.

## Runtime Behavior

### Form-Only Navigation

- No subcommand tree.
- Player UI is fully form-driven.
- `/ffa`:
  - outside arena: Arena / Stats / Settings.
  - inside arena: Leave Arena / Kits / Settings.
- `/ffaadmin`: Admin forms only (OP or `ffa.admin` permission).

### Arena Rules

- Join is only possible when arena status is `Live`.
- Block break/place/drop are blocked in arena.
- Safe-zone blocks combat and damage interactions.
- Safe-zone setup:
  1. Start 2-click mode in admin forms.
  2. Click block #1.
  3. Click block #2.
  4. Zone is stored automatically.

### Stats + Database Outage Mode (MySQL)

If `storageBackend` is `MYSQL` and the connection drops:

1. Stats/service is marked unavailable immediately.
2. All arenas are forced to `Offline`.
3. Active arena sessions are ended.
4. Reconnect is attempted 3 times every 15 seconds.
5. If still unreachable, plugin logs recommendation to switch to `JSON` and disables itself.

When MySQL recovers before attempt 3, stats service is restored.  
Arenas stay offline until an admin re-enables them.

## Architecture

Base package:

```text
makisimperium.ffa
```

Core modules:

- `arena`: arena models, persistence, catalog/session services.
- `kit`: kit models, JSON repository, selection/application/registry services.
- `player`: profile model, preferences, stats, repository abstractions.
- `ui.form`: complete form routing and flows.
- `listener`: game protection, lifecycle, form interaction, zone placement.
- `scoreboard`: arena-only scoreboard service.
- `i18n`: JSON language bundle loading and key resolution.
- `config`: JSON config model and loader.

## Storage Model

- No YAML storage for runtime data systems.
- JSON used for:
  - config,
  - arenas,
  - kits,
  - language bundles,
  - optional JSON player profiles.
- UUID is the canonical key for player persistence.
- Player names are display-only.

## Configuration

`plugins/FFAGameV2/config.json`:

```json
{
  "storageBackend": "MYSQL",
  "database": {
    "host": "127.0.0.1",
    "port": 3306,
    "database": "ffa",
    "username": "root",
    "password": "change-me",
    "table": "ffa_player_profiles",
    "useSsl": false,
    "allowPublicKeyRetrieval": true
  },
  "scoreboard": {
    "enabledByDefault": true,
    "updateIntervalTicks": 20
  }
}
```

### Storage Backend Options

- `MYSQL`: strict DB mode with outage handling and reconnect policy.
- `JSON`: no external DB dependency.

If MySQL is not configured, plugin informs and uses JSON storage.

## Commands

- `/ffa` - Open player menu (forms).
- `/ffaadmin` - Open admin console (forms).

## Permissions

- `ffa.admin` (default: op)

## Build

Maven package output is configured as:

```text
target/FFAGameV2.jar
```

Build command:

```bash
mvn clean package
```

## Release Notes (Current)

- Package namespace migrated to `makisimperium.ffa`.
- Bootstrap class renamed to `FFABootstrap`.
- Maven artifact cleaned and output jar standardized to `FFAGameV2.jar`.
- UI displays arena/kit names instead of internal IDs.
- Localization auto-persists discovered translation keys into `lang/en.json` and `lang/de.json`.

## License

Private project unless explicitly licensed otherwise by the owner.

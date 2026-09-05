# ExUI primary navigation — Manage batches

TEC needs a global Manage Cases nav link to a batch subsystem (not case-scoped). That lives in
ExUI (`rpx-xui-webapp`) `menuConfigs`, not in this repo’s CCD definition.

For the architect proposal (problem, ownership, risks), see
[exui-manage-batches-plan.md](./exui-manage-batches-plan.md).

## How ExUI builds the primary nav

1. Browser loads `headerConfig` from `/external/config/ui/`.
2. Server builds that from `setupMenuConfig(environment)` →
   [base-config.ts](https://github.com/hmcts/rpx-xui-webapp/blob/master/api/configuration/menuConfigs/base-config.ts)
   (+ AAT diffs). The UI payload does **not** come from a live `HEADER_CONFIG` env var.
3. `HeaderConfigService` picks one menu list by matching the user’s IdAM roles against regex keys.
4. The first matching non-default key wins; otherwise the `.+` fallback is used.
5. Each item is a `NavigationItem`: at least `text`, `href`, and `active`. Items may also use
   `roles` and LaunchDarkly `flags`.

## Local simulation (this repo)

Stock XUI cannot be configured via env for primary nav. This POC simulates the ExUI
`menuConfigs` change with a reverse proxy:

| Piece | Role |
| --- | --- |
| `XUI_PORT=3002` in `build.gradle` | Real Manage Cases container |
| `bin/start-xui-manage-batches-proxy.sh` | Started by `bootWithCCD`; listens on **:3000** |
| `bin/xui-manage-batches-proxy.py` | Rewrites `/external/config/ui/` to add a `caseworker-tec` menu including **Manage batches** |
| `/tec-manage-batches` | Placeholder page served by the proxy (stand-in for the batch subsystem) |

### Verify

1. Prefer a clean stack so XUI is recreated on port 3002:
   `./bin/stop-boot-with-ccd.sh` then `./gradlew bootWithCCD`
2. If the nav proxy was already running from an older revision, refresh it:
   `./bin/restart-xui-manage-batches-proxy.sh`
3. Open **http://localhost:3000** (not `:3002`) and sign in as `tec-demo@test.com` / `password`
4. After login the browser address bar should stay on port **3000**. If it switches to
   `:3002`, you are past the proxy and will not see Manage batches — restart the proxy
   (step 2) and sign in again via `:3000`.
5. Primary nav should include **Manage batches** between Create case and Find case
   (no **Case list** item — open cases via **Manage cases** title)
6. Clicking it opens the local placeholder at `/tec-manage-batches`

Proxy logs: `bin/.xui-manage-batches-proxy.log`

## Draft menu shape

See [`exui-header-config.example.json`](./exui-header-config.example.json).

| Item | Local `href` |
| --- | --- |
| Create case | `/cases/case-filter` |
| **Manage batches** | `/tec-manage-batches` (local); absolute URL in a real ExUI release |
| Find case | `/cases/case-search` (right-aligned) |

**Case list** is omitted from the TEC menu. Clerks still reach `/cases` via the **Manage cases**
title (home) link, bookmarks, or other in-app routes.

### Role key

`caseworker-tec` matches clerk (`caseworker-tec`) and, as an unanchored regex substring,
`caseworker-tec-system`. Tighten to `^caseworker-tec$` in a real ExUI PR if only clerks should
see the link.

## Production change

Add the same menu key in ExUI `api/configuration/menuConfigs/base-config.ts` and release via the
ExUI pipeline. Do not rely on `RSE_LIB_XUI_ENV_HEADER_CONFIG` — the UI ignores it for
`headerConfig`.

## Related TEC roles

| Java role | IdAM role |
| --- | --- |
| `CLERK` | `caseworker-tec` |
| `SYSTEM` | `caseworker-tec-system` |

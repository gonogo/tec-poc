# ExUI primary navigation — Manage batches

TEC needs a global Manage Cases nav link to a batch subsystem (not case-scoped). That lives in
ExUI (`rpx-xui-webapp`) via `HEADER_CONFIG`, not in this repo’s CCD definition.

## How ExUI builds the primary nav

1. Node config exposes `headerConfig` from the `HEADER_CONFIG` environment variable.
2. `HeaderConfigService` picks one menu list by matching the user’s IdAM roles against regex keys.
3. The first matching non-default key wins; otherwise the `.+` fallback is used.
4. Each item is a `NavigationItem`: at least `text`, `href`, and `active`.

Default items (when no custom config applies) are Case list, Create case, and Find case
(`AppConstants.DEFAULT_MENU_ITEMS`). Work Allocation and other platform features extend the
deployed `HEADER_CONFIG` further in real environments.

## Draft: TEC menu with Manage batches

See [`exui-header-config.example.json`](./exui-header-config.example.json).

| Item | `href` |
| --- | --- |
| Case list | `/cases` |
| Create case | `/cases/case-filter` |
| **Manage batches** | Absolute URL of the batch subsystem (placeholder in the example) |
| Find case | `/cases/case-search` (right-aligned, existing ExUI pattern) |

### Role key

`^caseworker-tec` matches:

- `caseworker-tec` (clerk)
- `caseworker-tec-system` (system — also matches because the pattern is unanchored at the end)

Tighten to `^caseworker-tec$` if only clerks should see Manage batches.

### Merging into the live platform config

Do **not** replace the whole `HEADER_CONFIG` with only the TEC fragment. In AAT/prod, merge:

1. Keep every existing role key (judicial, solicitor, WA, etc.) unchanged.
2. Add (or extend) the `^caseworker-tec` key with the menu above.
3. Keep the existing `.+` (and other) menus so non-TEC users are unaffected.
4. Replace `https://REPLACE_WITH_TEC_BATCHES_URL` with the real batch service URL per environment.

If TEC users should also see Work Allocation items (for example My work), copy those entries from
the deployed menu for the matching role into the TEC list — ExUI selects **one** full list per
user, it does not merge keys.

### Same-origin alternative

If SSO/cookies or CSP make a cross-origin link awkward, put the batch UI behind a path on the
Manage Cases host (for example `/manage-batches`) and set:

```json
"href": "/manage-batches"
```

That still requires the same `HEADER_CONFIG` change plus ingress/proxy work.

## What this repo cannot do

`bootWithCCD` / `build.gradle` only pass limited XUI env (jurisdictions, documents APIs, etc.).
Primary nav is owned by Manage Cases configuration (helm/flux `HEADER_CONFIG`). Applying this
draft needs an ExUI / platform config change.

## Related TEC roles

| Java role | IdAM role |
| --- | --- |
| `CLERK` | `caseworker-tec` |
| `SYSTEM` | `caseworker-tec-system` |

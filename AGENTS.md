# Agent notes — TEC POC

## Canonical technical documentation

Prefer these files over inventing CCD / ExUI topology. They are the GOV.UK Tech Docs sources under `design_docs/source/`:

| Topic | Path |
| --- | --- |
| Decentralised CCD architecture | [`design_docs/source/ccd-architecture.html.md.erb`](design_docs/source/ccd-architecture.html.md.erb) |
| CFTLib / database ownership | [`design_docs/source/cftlib-shared-database.html.md.erb`](design_docs/source/cftlib-shared-database.html.md.erb) |
| ExUI Upload batch file nav (local proxy) | [`design_docs/source/exui-navigation.html.md.erb`](design_docs/source/exui-navigation.html.md.erb) |
| ExUI Upload batch file architect proposal | [`design_docs/source/exui-manage-batches-plan.html.md.erb`](design_docs/source/exui-manage-batches-plan.html.md.erb) |
| Example menuConfigs JSON | [`design_docs/source/exui-header-config.example.json`](design_docs/source/exui-header-config.example.json) |

Site overview: [`design_docs/source/index.html.md.erb`](design_docs/source/index.html.md.erb).

## Local preview

`./gradlew bootWithCCD` starts Middleman via `bin/start-design-docs.sh` at **http://localhost:4567** (soft-fails if Ruby/Bundler are missing). Or run `./bin/start-design-docs.sh` alone.

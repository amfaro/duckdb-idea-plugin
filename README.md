# DuckDB for JetBrains IDEs

<!-- Plugin description -->
JetBrains IDE plugin that registers **DuckDB** as a first-class SQL dialect — surfacing it in the per-file **SQL Dialects** picker (Settings → Languages & Frameworks → SQL Dialects) and grouping DuckDB data sources and consoles under a dedicated `Dbms.DUCKDB` constant.

This is a **dialect-only** plugin — it does not bundle the DuckDB JDBC driver, format SQL, or run linters. It exists so other tools (e.g. [`jarify-jetbrains`](https://github.com/amfaro/jarify-jetbrains)) and the IDE itself can identify "this file is DuckDB SQL" without piggy-backing on `Dbms.UNKNOWN`.
<!-- Plugin description end -->

## Status

**Tier 1 (in progress).** Mirrors the [Tarantool plugin](https://github.com/tarantool/tarantool-idea-plugin)'s starting point: a minimal `SqlLanguageDialect` that delegates lexing/parsing to SQL-92. Real DuckDB lexer, keyword/function tables, and grammar are tracked as separate issues — see the roadmap.

## Why a separate plugin?

DataGrip ships DuckDB at "Basic Support" tier — the JDBC driver is bundled and connections are recognised, but there's no `Dbms.DUCKDB` constant and no DuckDB-aware dialect through 2026.1 (build 261). JetBrains' tracking issues — [DBE-15099](https://youtrack.jetbrains.com/issue/DBE-15099/Driver-for-DuckDB) and [duckdb/duckdb-java#51](https://github.com/duckdb/duckdb-java/issues/51) — remain open with no public roadmap.

A vendor-adjacent dialect plugin is the established pattern for this gap. Precedents:

- [`tarantool/tarantool-idea-plugin`](https://github.com/tarantool/tarantool-idea-plugin) — Tier-1 stub delegating to SQL-92
- [`galaxy-sea/TDengine-Driver-Integration`](https://github.com/galaxy-sea/TDengine-Driver-Integration) — Tier-2 with `.bnf` grammar and JFlex lexer

## Roadmap

See [open issues](https://github.com/amfaro/duckdb-idea-plugin/issues). Tiers, in dependency order:

1. **Tier 1** — register `Dbms.DUCKDB` and a stub `DuckDbSqlDialect` that delegates to SQL-92 (current focus)
2. **Tier 2** — custom JFlex lexer, DuckDB keyword set, builtin function table seeded from `pragma function_list()`
3. **Tier 3** — real `.bnf` grammar covering DuckDB-specific syntax (PIVOT/UNPIVOT, struct/list/map literals, EXCLUDE/REPLACE in SELECT, lambdas, ATTACH, etc.)
4. **DataGrip integration** — `<database.dialect dbms="DUCKDB">` so generated SQL, completion, and the schema tree are DuckDB-aware
5. **Marketplace publication**

## Development

```bash
mise run run-ide          # launch a sandbox IDE with the plugin loaded
mise run build            # produce build/distributions/*.zip
mise run verify           # run JetBrains plugin verifier
mise run test             # run unit tests
mise run publish          # publish to Marketplace (needs JETBRAINS_MARKETPLACE_TOKEN)
```

JDK 17 is required. `mise.toml` pins `temurin-17`.

## License

MIT. See [`LICENSE`](LICENSE).

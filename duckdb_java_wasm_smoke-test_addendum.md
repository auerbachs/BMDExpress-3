# Java JDBC ↔ DuckDB‑Wasm Compatibility (Troubleshooting Guide)

## Bottom line

There’s **no fundamental file‑format incompatibility** between a `.duckdb` file created via **Java/JDBC** and one opened by **DuckDB‑Wasm** in the browser. DuckDB guarantees cross‑client compatibility and backward‑compatible storage (a **newer** DuckDB can read files from an **older** DuckDB). In practice, failures almost always come from **version skew, WAL/checkpointing, environment limits, or extensions**.

---

## The usual culprits (and how they look)

1. **Version direction (writer > reader)**

   * **Symptom:** Browser fails to open with a message about file/format version.
   * **Cause:** Your Java/JDBC build is **newer** than the WASM bundle you ship (Wasm lags native releases). Backward compat only goes **old → new**, not new → old.
   * **Fix:** Pin both ends to the **same minor** (e.g., `1.3.x`) or ensure the writer is **not newer** than the Wasm reader.

2. **Opted‑in newer storage version**

   * **Symptom:** File opens on native/JDBC but not in Wasm, even when versions seem close.
   * **Cause:** The DB (or an attached DB) was created with an explicit, **newer `STORAGE_VERSION`** than your Wasm build understands.
   * **Fix:** Recreate/attach with a **compatible** storage version (see concrete commands below).

3. **Unflushed WAL / dirty shutdown**

   * **Symptom:** Browser sees missing recent rows or can’t apply last changes; no `.wal` file accessible over HTTPS.
   * **Cause:** Java session ended without a **`CHECKPOINT`** (or clean close), leaving new data only in `*.wal`.
   * **Fix:** Always issue `CHECKPOINT;` (or close cleanly) before publishing the file.

4. **WASM environment constraints (HTTPS/CORS/Range)**

   * **Symptom:** "Can’t open file" or network errors when using remote `ATTACH`.
   * **Cause:** Missing **HTTPS**, **CORS**, or **HTTP Range** support on your server/CDN; remember remote `ATTACH` is **read‑only**.
   * **Fix:** Serve via HTTPS with permissive CORS; for writes, **copy into OPFS** and operate locally.

5. **Extensions parity**

   * **Symptom:** DB opens, but certain queries fail (missing functions/types).
   * **Cause:** You used extensions in Java that aren’t available/enabled in Wasm.
   * **Fix:** Stick to core features or `INSTALL/LOAD` Wasm‑compatible extensions.

---

## Minimal, decisive diagnostics

Run on **both** Java/JDBC and Wasm:

```sql
-- 1) Engine version
SELECT version();        -- e.g., 'v1.3.2'
PRAGMA version;          -- shows library_version, source_id

-- 2) What storage tags are present on attached DBs?
SELECT database_name, tags
FROM duckdb_databases(); -- check for storage_version in tags
```

**Interpretation:**

* If **browser < writer**, you’ve hit forward‑incompat. Align versions.
* If `tags` shows a **storage\_version** newer than your Wasm build, recreate with a compatible storage version.
* If the file seems fine but rows are missing, you likely didn’t **checkpoint** before shipping.

---

## Concrete fixes (copy/paste)

### A) The easy, robust path: pin versions

* Choose a minor series (e.g., `1.3.x`).
* Use matching **JDBC** and **`@duckdb/duckdb-wasm`** versions.
* Recreate the DB (or open and `CHECKPOINT`) before publishing.

### B) Force a compatible storage version when creating the file in Java

```sql
-- In the Java/JDBC session that prepares the public DB file:
ATTACH '/absolute/path/my.duckdb' (STORAGE_VERSION 'v1.3.2') AS target;

-- Build/populate tables under target.main.* here
-- e.g., CREATE TABLE target.main.items AS SELECT ...;

CHECKPOINT;   -- flush into the main file
DETACH target;
```

*(Adjust the storage version string to the exact version supported by your Wasm bundle.)*

### C) If you already wrote with a too‑new engine

* Open the DB with the **newer** native/JDBC you used, then **export/import** or CTAS into a DB created at the **older** compatible version, or simply **rebuild** with pinned versions.

### D) Always ship a clean single file

```sql
CHECKPOINT;   -- run before copying/uploading the .duckdb file
```

Ensure no lingering `.wal` next to the file you deploy.

---

## Environment checklist for Wasm

* **HTTPS** only (browsers block mixed content).
* **CORS** headers allow cross‑origin fetch if the DB isn’t same‑origin.
* **HTTP Range** requests supported (common on CDNs; helps large files).
* **Remote is read‑only**: for writes, use OPFS

  * Typical flow: `ATTACH` remote → `CREATE TABLE local.t AS SELECT * FROM remote.t` → operate on `local`.

---

## Why earlier guidance didn’t flag your issue

The prior write‑up assumed you’d follow the recommended practice to **pin the same minor version** across Java and Wasm. DuckDB’s compatibility promises are real—but they’re **directional** (old → new). In your setup, either **version skew** (writer newer than Wasm) or **WAL/checkpointing** likely bit you. That’s on me for not requesting your exact versions/error upfront.

---

## Quick success recipe (recap)

1. On Java: use JDBC matching your Wasm minor (e.g., both **1.3.x**). After writes, `CHECKPOINT;`.
2. Publish `my.duckdb` over **HTTPS** with **CORS** (and Range) enabled.
3. In Wasm: `ATTACH 'https://…/my.duckdb' AS remote;` (read‑only) → copy into OPFS if you need R/W.
4. If it still fails, run the **diagnostics** above and align `version()` / `storage_version` accordingly.


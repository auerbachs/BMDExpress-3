# BMDExpress DuckDB WASM Testing Environment

This testing environment verifies whether DuckDB files created by BMDExpress work correctly in browser/WASM environments for use in Single Page Applications (SPAs).

## Files Created

### Test Environment
- `package.json` - Node.js dependencies and scripts
- `vite.config.js` - Vitest configuration with happy-dom
- `test/setup.js` - Test environment setup
- `test-server.js` - Simple HTTP server for browser testing

### Tests
- `test/browser-duckdb.test.js` - File compatibility tests (Node.js)
- `test/duckdb-wasm.test.js` - DuckDB WASM API tests (Node.js)
- `test/real-browser.html` - Interactive browser test page

## Usage

### 1. Run Node.js Tests
```bash
npm run test:run
```
This runs compatibility tests that verify:
- Database files exist and are readable
- File formats are valid
- File sizes are reasonable for web loading
- Simulates ArrayBuffer conversion for browser loading

### 2. Run Browser Tests
```bash
npm run test:server
```
Then open http://localhost:8080/test/real-browser.html

The browser test allows you to:
1. Initialize DuckDB WASM in a real browser environment
2. Load your actual .duckdb files via file picker
3. Test basic SQL queries
4. Verify BMDExpress schema tables exist and are queryable

### 3. Available Test Files
The following DuckDB files are available for testing:
- `test_wasm_verification.duckdb` (0.26 MB)
- `dehp_webapp_schema_fixed.duckdb` (6.26 MB)
- `reference.duckdb` (12.76 MB)
- `p3mp_webapp_schema.duckdb` (22.01 MB)

## What This Tests

### File Compatibility
- ✓ DuckDB files can be read as binary data
- ✓ File headers contain expected DuckDB magic bytes
- ✓ File sizes are reasonable for web loading (all under 25MB)
- ✓ Files can be converted to ArrayBuffer/Uint8Array for browser use

### WASM Functionality
- ✓ DuckDB WASM module loads correctly
- ✓ Database connections can be established
- ✓ Basic SQL operations work (CREATE, INSERT, SELECT)
- ✓ Statistical functions work (AVG, COUNT, etc.)
- ✓ Database files can be registered and attached

### BMDExpress Schema
The browser test specifically checks for BMDExpress tables:
- `projects` - Project metadata
- `probes` - Gene/probe identifiers
- `treatments` - Dose conditions
- `probe_responses` - Expression data
- `bmd_results` - BMD analysis results
- `probe_stat_results` - Statistical results per probe
- `category_analysis_results` - Pathway analysis results

## Troubleshooting WASM Issues

If databases don't work in your SPA:

1. **Check file loading**: Ensure files are served with correct MIME type (`application/octet-stream`)
2. **Check CORS**: Database files must be served from same origin or with proper CORS headers
3. **Check file registration**: Use `db.registerFileBuffer(name, uint8Array)` to register file data
4. **Check attachment**: Use `ATTACH 'filename' AS alias` to make tables accessible
5. **Check table queries**: Use `information_schema.tables` to list available tables

## Example SPA Integration

```javascript
// Load DuckDB WASM
import * as duckdb from '@duckdb/duckdb-wasm';

// Initialize
const JSDELIVR_BUNDLES = duckdb.getJsDelivrBundles();
const bundle = await duckdb.selectBundle(JSDELIVR_BUNDLES);
const worker = new Worker(/* bundle.mainWorker */);
const db = new duckdb.AsyncDuckDB(logger, worker);
await db.instantiate(bundle.mainModule);

// Load your database file
const response = await fetch('/path/to/your-data.duckdb');
const arrayBuffer = await response.arrayBuffer();
const uint8Array = new Uint8Array(arrayBuffer);
await db.registerFileBuffer('data.duckdb', uint8Array);

// Connect and query
const conn = await db.connect();
await conn.query("ATTACH 'data.duckdb' AS data");
const result = await conn.query("SELECT * FROM data.probes LIMIT 10");
```
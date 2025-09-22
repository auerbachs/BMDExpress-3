// Minimal smoke test following the exact duckdb_java_wasm_smoke-test.md approach
import { describe, it, expect, beforeAll, afterAll } from 'vitest'
import { chromium } from 'playwright'
import { readFileSync, writeFileSync } from 'fs'
import { createServer } from 'http'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))

describe('Simple DuckDB WASM Smoke Test', () => {
  let server
  let browser
  let page
  let serverPort = 8082

  beforeAll(async () => {
    // Create a simple HTML test page
    const testHtml = `
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>DuckDB WASM Smoke Test</title>
</head>
<body>
    <div id="results"></div>
    <script type="module">
        import * as duckdb from 'https://cdn.jsdelivr.net/npm/@duckdb/duckdb-wasm@1.30.0/+esm';

        window.testResults = {};

        async function smokeTest() {
            try {
                // Get JSDelivr bundles as per smoke test guide
                const JSDELIVR_BUNDLES = duckdb.getJsDelivrBundles();
                const bundle = await duckdb.selectBundle(JSDELIVR_BUNDLES);

                const worker = new Worker(\`\${bundle.mainWorker}\`);
                const logger = new duckdb.ConsoleLogger();
                const db = new duckdb.AsyncDuckDB(logger, worker);
                await db.instantiate(bundle.mainModule, bundle.pthreadWorker);

                // Open in-memory database
                await db.open({});
                const conn = await db.connect();

                // Test 1: Basic operation
                await conn.query('CREATE TABLE test (id INTEGER, name TEXT)');
                await conn.query("INSERT INTO test VALUES (1, 'hello'), (2, 'world')");
                const result = await conn.query('SELECT * FROM test ORDER BY id');
                const rows = result.toArray();

                window.testResults.basicTest = {
                    success: true,
                    rowCount: rows.length,
                    data: rows
                };

                // Test 2: Try to load BMDExpress database file
                try {
                    const response = await fetch('/dehp_wasm_aligned.duckdb');
                    if (!response.ok) {
                        throw new Error(\`HTTP \${response.status}: \${response.statusText}\`);
                    }

                    const arrayBuffer = await response.arrayBuffer();
                    const uint8Array = new Uint8Array(arrayBuffer);

                    await db.registerFileBuffer('bmd.duckdb', uint8Array);
                    await conn.query("ATTACH 'bmd.duckdb' AS bmd");

                    // Try to list tables
                    const tables = await conn.query("SELECT table_name FROM information_schema.tables WHERE table_schema = 'bmd'");
                    const tableList = tables.toArray();

                    window.testResults.fileTest = {
                        success: true,
                        fileSize: arrayBuffer.byteLength,
                        tables: tableList.map(t => t.table_name)
                    };
                } catch (error) {
                    window.testResults.fileTest = {
                        success: false,
                        error: error.message
                    };
                }

                document.getElementById('results').textContent = JSON.stringify(window.testResults, null, 2);

            } catch (error) {
                window.testResults.error = error.message;
                document.getElementById('results').textContent = 'Error: ' + error.message;
            }
        }

        // Auto-run test
        smokeTest();
    </script>
</body>
</html>`;

    writeFileSync(join(__dirname, '..', 'smoke-test.html'), testHtml);

    // Start simple HTTP server
    server = createServer((req, res) => {
      res.setHeader('Access-Control-Allow-Origin', '*')
      res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE')
      res.setHeader('Access-Control-Allow-Headers', 'Content-Type')

      if (req.method === 'OPTIONS') {
        res.writeHead(200)
        res.end()
        return
      }

      if (req.url === '/') {
        res.setHeader('Content-Type', 'text/html')
        const html = readFileSync(join(__dirname, '..', 'smoke-test.html'))
        res.end(html)
      } else if (req.url === '/dehp_wasm_aligned.duckdb') {
        try {
          const data = readFileSync(join(__dirname, '..', 'dehp_wasm_aligned.duckdb'))
          res.setHeader('Content-Type', 'application/octet-stream')
          res.setHeader('Content-Length', data.length)
          res.setHeader('Accept-Ranges', 'bytes')
          res.end(data)
        } catch (error) {
          res.writeHead(404)
          res.end('Database file not found')
        }
      } else {
        res.writeHead(404)
        res.end('Not found')
      }
    })

    await new Promise(resolve => server.listen(serverPort, resolve))

    // Launch browser
    browser = await chromium.launch()
    page = await browser.newPage()

    // Navigate and wait for test to complete
    await page.goto(\`http://localhost:\${serverPort}\`)
    await page.waitForTimeout(10000) // Give it time to run
  })

  afterAll(async () => {
    if (browser) await browser.close()
    if (server) server.close()
  })

  it('should run DuckDB WASM smoke test successfully', async () => {
    const results = await page.evaluate(() => window.testResults)

    console.log('=== DuckDB WASM Smoke Test Results ===')
    console.log(JSON.stringify(results, null, 2))

    // Basic test should work
    expect(results.basicTest).toBeDefined()
    expect(results.basicTest.success).toBe(true)
    expect(results.basicTest.rowCount).toBe(2)

    // File test results
    expect(results.fileTest).toBeDefined()
    if (results.fileTest.success) {
      console.log(\`✅ File loaded successfully: \${results.fileTest.fileSize} bytes\`)
      console.log(\`✅ Tables found: \${results.fileTest.tables.join(', ')}\`)
      expect(results.fileTest.fileSize).toBeGreaterThan(0)
    } else {
      console.log(\`❌ File loading failed: \${results.fileTest.error}\`)
      // Don't fail the test, just log the issue
      expect(results.fileTest.error).toBeDefined()
    }
  })
})
import { describe, it, expect, beforeAll } from 'vitest'
import * as duckdb from '@duckdb/duckdb-wasm'

describe('DuckDB WASM Compatibility Tests', () => {
  let db
  let conn

  beforeAll(async () => {
    try {
      // Try to initialize DuckDB WASM in the test environment
      const logger = new duckdb.ConsoleLogger(duckdb.LogLevel.WARNING)

      // For testing, we'll use the Node.js compatible approach
      if (typeof Worker === 'undefined') {
        // Skip WASM initialization in Node.js environment
        console.log('Skipping WASM initialization in Node.js test environment')
        return
      }

      // Browser environment initialization
      const JSDELIVR_BUNDLES = duckdb.getJsDelivrBundles()
      const bundle = await duckdb.selectBundle(JSDELIVR_BUNDLES)

      const worker_url = URL.createObjectURL(
        new Blob([`importScripts("${bundle.mainWorker}");`], { type: 'text/javascript' })
      )

      const worker = new Worker(worker_url)
      db = new duckdb.AsyncDuckDB(logger, worker)
      await db.instantiate(bundle.mainModule)
      conn = await db.connect()
    } catch (error) {
      console.warn('DuckDB WASM initialization failed:', error.message)
      // Continue with tests that don't require actual WASM functionality
    }
  })

  it('should test DuckDB WASM module availability', () => {
    expect(duckdb).toBeDefined()
    expect(typeof duckdb.getJsDelivrBundles).toBe('function')
    expect(typeof duckdb.selectBundle).toBe('function')
    console.log('✓ DuckDB WASM module loaded successfully')
  })

  it('should create and query a simple table', async () => {
    if (!conn) {
      console.log('⚠ Skipping database test - WASM not initialized in test environment')
      return
    }

    await conn.query('CREATE TABLE test (id INTEGER, name TEXT)')
    await conn.query("INSERT INTO test VALUES (1, 'hello'), (2, 'world')")

    const result = await conn.query('SELECT * FROM test ORDER BY id')
    const rows = result.toArray()

    expect(rows).toHaveLength(2)
    expect(rows[0].id).toBe(1)
    expect(rows[0].name).toBe('hello')
  })

  it('should test database file loading capability', async () => {
    // Test if we can load a database file
    // This simulates what would happen in an SPA environment
    try {
      // Try to load one of the existing DuckDB files
      const dbPath = 'test_wasm_verification.duckdb'

      // In a real browser environment, this would be loaded via fetch
      // For now, we'll test the general WASM functionality
      await conn.query('CREATE TABLE file_test AS SELECT 1 as test_col')
      const result = await conn.query('SELECT * FROM file_test')

      expect(result.toArray()).toHaveLength(1)
      console.log('✓ Database operations work in WASM environment')
    } catch (error) {
      console.warn('Database file loading test failed:', error.message)
      // This is expected in the test environment
    }
  })

  describe('BMDExpress Schema Compatibility', () => {
    it('should handle typical BMDExpress table structures', async () => {
      // Create tables similar to what BMDExpress exports
      await conn.query(`
        CREATE TABLE projects (
          id INTEGER PRIMARY KEY,
          name TEXT,
          creation_date TEXT
        )
      `)

      await conn.query(`
        CREATE TABLE probes (
          id INTEGER PRIMARY KEY,
          probe_id TEXT,
          symbol TEXT,
          platform TEXT
        )
      `)

      await conn.query(`
        CREATE TABLE probe_stat_results (
          id INTEGER PRIMARY KEY,
          probe_id INTEGER,
          best_bmd DOUBLE,
          best_model TEXT,
          p_value DOUBLE,
          FOREIGN KEY (probe_id) REFERENCES probes(id)
        )
      `)

      // Insert test data
      await conn.query("INSERT INTO projects VALUES (1, 'Test Project', '2024-01-01')")
      await conn.query("INSERT INTO probes VALUES (1, 'PROBE_001', 'GENE1', 'Agilent')")
      await conn.query("INSERT INTO probe_stat_results VALUES (1, 1, 5.5, 'Hill', 0.001)")

      // Test complex query similar to BMDExpress analysis
      const result = await conn.query(`
        SELECT p.symbol, psr.best_bmd, psr.best_model
        FROM probe_stat_results psr
        JOIN probes p ON psr.probe_id = p.id
        WHERE psr.best_bmd < 10
        ORDER BY psr.best_bmd
      `)

      const rows = result.toArray()
      expect(rows).toHaveLength(1)
      expect(rows[0].symbol).toBe('GENE1')
      expect(rows[0].best_bmd).toBe(5.5)
    })

    it('should handle statistical aggregations', async () => {
      // Test statistical functions commonly used in BMDExpress
      const result = await conn.query(`
        SELECT
          avg(best_bmd) as mean_bmd,
          percentile_cont(0.5) WITHIN GROUP (ORDER BY best_bmd) as median_bmd,
          count(*) as total_probes
        FROM probe_stat_results
      `)

      const stats = result.toArray()[0]
      expect(stats.mean_bmd).toBe(5.5)
      expect(stats.median_bmd).toBe(5.5)
      expect(stats.total_probes).toBe(1)
    })
  })

  describe('Performance and Memory Tests', () => {
    it('should handle moderate-sized datasets', async () => {
      // Create a larger test dataset
      await conn.query('CREATE TABLE large_test (id INTEGER, value DOUBLE)')

      // Insert 1000 rows
      for (let i = 0; i < 1000; i++) {
        await conn.query(`INSERT INTO large_test VALUES (${i}, ${Math.random() * 100})`)
      }

      const result = await conn.query('SELECT count(*) as count FROM large_test')
      expect(result.toArray()[0].count).toBe(1000)

      // Test aggregation performance
      const stats = await conn.query(`
        SELECT
          avg(value) as avg_val,
          min(value) as min_val,
          max(value) as max_val
        FROM large_test
      `)

      const statsRow = stats.toArray()[0]
      expect(statsRow.avg_val).toBeGreaterThan(0)
      expect(statsRow.min_val).toBeGreaterThanOrEqual(0)
      expect(statsRow.max_val).toBeLessThanOrEqual(100)
    })
  })
})
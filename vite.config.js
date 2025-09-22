import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    // Use different environments for different test files
    environmentMatchGlobs: [
      ['test/**/browser-*.test.js', 'node'],
      ['test/**/headless-*.test.js', 'node'],
      ['test/**/wasm-*.test.js', 'node']
    ],
    globals: true,
    setupFiles: ['./test/setup.js'],
    testTimeout: 30000, // DuckDB WASM can be slow to initialize
    hookTimeout: 30000
  },
  optimizeDeps: {
    exclude: ['@duckdb/duckdb-wasm']
  }
})
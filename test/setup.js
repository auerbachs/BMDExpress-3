// Test setup for DuckDB WASM testing
// This file runs before each test file

// Mock fetch for file loading if needed
global.fetch = global.fetch || (() => Promise.reject(new Error('fetch not implemented')))

// Setup any global test utilities here
console.log('Test environment initialized for DuckDB WASM testing')
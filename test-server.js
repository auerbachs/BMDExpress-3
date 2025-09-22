#!/usr/bin/env node

import { createServer } from 'http';
import { readFileSync, statSync } from 'fs';
import { join, extname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = fileURLToPath(new URL('.', import.meta.url));

const mimeTypes = {
  '.html': 'text/html',
  '.js': 'application/javascript',
  '.css': 'text/css',
  '.duckdb': 'application/octet-stream',
  '.json': 'application/json'
};

const server = createServer((req, res) => {
  // Add CORS headers for development
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  // Handle CORS preflight
  if (req.method === 'OPTIONS') {
    res.writeHead(200);
    res.end();
    return;
  }

  let filePath = req.url === '/' ? '/test/real-browser.html' : req.url;
  filePath = join(__dirname, filePath);

  try {
    const stats = statSync(filePath);
    if (stats.isFile()) {
      const ext = extname(filePath);
      const contentType = mimeTypes[ext] || 'text/plain';

      res.setHeader('Content-Type', contentType);

      // Add special headers for DuckDB files
      if (ext === '.duckdb') {
        res.setHeader('Content-Length', stats.size);
        res.setHeader('Accept-Ranges', 'bytes');
      }

      const content = readFileSync(filePath);
      res.writeHead(200);
      res.end(content);
    } else {
      res.writeHead(404);
      res.end('File not found');
    }
  } catch (error) {
    if (error.code === 'ENOENT') {
      res.writeHead(404);
      res.end('File not found');
    } else {
      res.writeHead(500);
      res.end('Server error');
    }
  }
});

const port = process.env.PORT || 8080;
server.listen(port, () => {
  console.log(`\nBMDExpress DuckDB WASM Test Server`);
  console.log(`================================`);
  console.log(`Server running at: http://localhost:${port}`);
  console.log(`Test page: http://localhost:${port}/test/real-browser.html`);
  console.log(`\nAvailable DuckDB files to test:`);

  const dbFiles = [
    'test_wasm_verification.duckdb',
    'dehp_webapp_schema_fixed.duckdb',
    'reference.duckdb',
    'p3mp_webapp_schema.duckdb'
  ];

  dbFiles.forEach(file => {
    try {
      const stats = statSync(join(__dirname, file));
      const sizeMB = (stats.size / 1024 / 1024).toFixed(2);
      console.log(`  • ${file} (${sizeMB} MB) - http://localhost:${port}/${file}`);
    } catch (e) {
      console.log(`  • ${file} (not found)`);
    }
  });

  console.log(`\nPress Ctrl+C to stop the server`);
});

process.on('SIGINT', () => {
  console.log('\nShutting down test server...');
  server.close();
  process.exit(0);
});
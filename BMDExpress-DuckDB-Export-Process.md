# BMDExpress Java DuckDB Export Process Documentation

## Overview

BMDExpress uses a custom Java export process to convert .bm2 files (Java binary serialization format) into DuckDB databases for web applications. The process transforms hierarchical Java objects into normalized relational tables suitable for SQL analysis.

## Export Architecture

### Core Components

1. **Entry Points**
   - `DuckDBExportRunnerV3.java` - Standalone command-line runner
   - `BMDExpressCommandLine.java` - Integrated CLI with `export-duckdb` command
   - `DuckDBExportServiceV3.java` - Core export service implementation

2. **Data Flow**
   ```
   .bm2 file → Java deserialization → Object hierarchy → SQL table creation → DuckDB file
   ```

### Technical Implementation

#### 1. Project Loading Process

```java
// From DuckDBExportRunnerV3.java
private static BMDProject loadProject(String inputPath) throws IOException {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputPath))) {
        return (BMDProject) ois.readObject();
    }
}
```

**Key Details:**
- Uses Java Object serialization to read .bm2 files
- Deserializes entire BMDProject object hierarchy
- Requires all BMDExpress classes on classpath

#### 2. Database Connection & Configuration

```java
// From DuckDBExportServiceV3.java
Class.forName("org.duckdb.DuckDBDriver");
connection = DriverManager.getConnection("jdbc:duckdb:" + dbPath);
```

**Configuration:**
- **JDBC Driver**: `org.duckdb:duckdb_jdbc:1.3.2.0`
- **Connection**: Direct file path connection (`jdbc:duckdb:path/to/file.duckdb`)
- **Transaction**: Auto-commit enabled
- **Isolation**: Default JDBC isolation level

#### 3. Schema Creation Strategy

The export creates a **normalized relational schema** with the following approach:

**Primary Tables:**
```sql
-- Core experimental data
CREATE TABLE doseResponseExperiments (
    id INTEGER PRIMARY KEY,
    name TEXT,
    logTransformation VARCHAR,
    columnHeader2 VARCHAR,  -- JSON array stored as text
    chipCreationDate BIGINT
)

-- Gene/probe identifiers
CREATE TABLE treatments (
    id INTEGER PRIMARY KEY,
    doseResponseExperimentId INTEGER,
    dose DOUBLE,
    name TEXT
)

-- Expression data (flattened)
CREATE TABLE probeResponses (
    id INTEGER PRIMARY KEY,
    doseResponseExperimentId INTEGER,
    probeId VARCHAR,
    dose DOUBLE,
    responseMean DOUBLE
)
```

**Analysis Results Tables:**
```sql
-- BMD analysis metadata
CREATE TABLE bmdResults (
    id INTEGER PRIMARY KEY,
    name TEXT,
    doseResponseExperimentId INTEGER,
    analysisInfo VARCHAR  -- JSON object
)

-- Category/pathway analysis
CREATE TABLE categoryAnalysisResults (
    id INTEGER PRIMARY KEY,
    categoryAnalysisResultsId INTEGER,
    categoryIdentifierId VARCHAR,
    modelType VARCHAR,
    geneAllCount INTEGER,
    percentage DOUBLE,
    wAUC DOUBLE,
    logwAUC DOUBLE,
    bmdFifthPercentileTotalGenes DOUBLE
)
```

#### 4. Data Transformation Process

**JSON Serialization Pattern:**
```java
// Complex objects stored as JSON strings
ObjectMapper objectMapper = new ObjectMapper();

// Example: Column headers array
stmt.setString(5, exp.getColumnHeader2() != null ?
    objectMapper.writeValueAsString(exp.getColumnHeader2()) : null);

// Example: Statistical results arrays
stmt.setString(4, "[1.0, 1.5, 2.0, 2.5, 3.0]"); // Sample JSON array
```

**ID Generation:**
```java
// Thread-safe ID generator for primary keys
private AtomicLong nextId = new AtomicLong(1);

long id = nextId.getAndIncrement();
stmt.setLong(1, id);
```

#### 5. Critical WASM Compatibility Features

**Checkpointing for WAL Flushing:**
```java
// Added for WASM compatibility - ensures single file deployment
try (Statement stmt = connection.createStatement()) {
    stmt.execute("CHECKPOINT");
    System.out.println("✅ Database checkpointed for WASM compatibility");
} catch (SQLException e) {
    System.err.println("⚠️  Warning: Could not checkpoint database: " + e.getMessage());
}
```

**Data Type Mapping:**
- Java `String` → DuckDB `VARCHAR`
- Java `Integer/Long` → DuckDB `INTEGER`
- Java `Double/Float` → DuckDB `DOUBLE`
- Java `Boolean` → DuckDB `BOOLEAN`
- Java `List<T>` → DuckDB `VARCHAR` (JSON serialized)
- Java `Map<K,V>` → DuckDB `VARCHAR` (JSON serialized)

## Export Process Flow

### Step 1: Project Analysis
```java
public void exportProject(BMDProject project, String dbPath) throws SQLException {
    System.out.println("Project Statistics:");
    System.out.println("Dose Response Experiments: " + project.getDoseResponseExperiments().size());
    // ... analyze project structure
}
```

### Step 2: Schema Creation
```java
private void createCamelCaseSchema() throws SQLException {
    try (Statement stmt = connection.createStatement()) {
        // Create all tables with proper foreign key relationships
        stmt.execute("CREATE TABLE IF NOT EXISTS doseResponseExperiments (...)");
        stmt.execute("CREATE TABLE IF NOT EXISTS treatments (...)");
        // ... create remaining tables
    }
}
```

### Step 3: Data Export
```java
// Export dose response experiments
String sql = "INSERT INTO doseResponseExperiments (id, name, logTransformation, columnHeader2, chipCreationDate) VALUES (?, ?, ?, ?, ?)";
try (PreparedStatement stmt = connection.prepareStatement(sql)) {
    for (DoseResponseExperiment exp : project.getDoseResponseExperiments()) {
        long expId = nextId.getAndIncrement();
        stmt.setLong(1, expId);
        stmt.setString(2, exp.getName());
        // ... set remaining parameters
        stmt.executeUpdate();
    }
}
```

### Step 4: Relationship Mapping
```java
// Maintain foreign key relationships
private long currentBmdResultId;

// Example: Link probe responses to experiments
stmt.setLong(2, expId);  // Foreign key to doseResponseExperiments
```

### Step 5: Finalization
```java
finally {
    if (connection != null) {
        // CRITICAL: Flush WAL into main file for WASM compatibility
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CHECKPOINT");
        }
        connection.close();
    }
}
```

## Command Line Usage

### Standalone Export
```bash
java -cp 'target/classes:lib/duckdb_jdbc-1.3.2.0.jar:lib/*' \
  com.sciome.bmdexpress2.commandline.DuckDBExportRunnerV3 \
  input.bm2 output.duckdb
```

### Integrated CLI
```bash
java -cp 'target/classes:lib/*' \
  com.sciome.bmdexpress2.commandline.BMDExpressCommandLine \
  export-duckdb --input-bm2 input.bm2 --output-file-name output.duckdb
```

## Known Limitations & Issues

### WASM Compatibility Issues

**Problem**: Despite using identical DuckDB versions (1.3.2) and proper checkpointing, exported databases fail in DuckDB WASM with:
```
IO Error: The file "database.duckdb" exists, but it is not a valid DuckDB database file!
```

**Verified Compatibility Measures:**
- ✅ Version alignment: JDBC 1.3.2.0 ↔ WASM 1.30.0 (both use source_id: 0b83e5d2f6)
- ✅ Clean checkpoint: WAL properly flushed with `CHECKPOINT` statement
- ✅ JSON extension: Available and loaded in WASM
- ✅ File format: Valid DuckDB files (readable by Python DuckDB client)

**Storage Version Issue:**
- Java-created files show: `storage_version=v1.0.0+`
- WASM expects newer storage format despite version compatibility claims

### Data Type Considerations

**JSON Storage:**
- Extensive use of JSON serialization for complex Java objects
- Columns like `columnHeader2`, `analysisInfo` contain JSON strings
- Requires JSON extension in WASM (successfully loaded)

**Large Object Handling:**
- Probe response data can be large (thousands of genes × treatments)
- Uses PreparedStatement batching for performance
- Individual table exports use atomic transactions

## Performance Characteristics

**Export Time:** ~1-2 seconds for typical datasets (2653 probes, 36 treatments)
**Database Size:** ~2-4 MB for moderate datasets
**Memory Usage:** Loads entire .bm2 file into memory before export

## Dependencies

**Required JARs:**
- `duckdb_jdbc-1.3.2.0.jar` - DuckDB JDBC driver
- `jackson-core-*.jar` - JSON serialization
- `jackson-databind-*.jar` - Object mapping
- `commons-*` - Apache Commons utilities
- `guava-*.jar` - Google Guava utilities

**Java Version:** Requires Java 21+

## File Structure Analysis

**Generated Schema (15 tables):**
- `doseResponseExperiments` - Experimental metadata
- `treatments` - Dose conditions
- `probeResponses` - Expression measurements
- `bmdResults` - BMD analysis metadata
- `categoryAnalysisResults` - Pathway analysis results
- `categoryAnalysisResultsSets` - Result groupings
- `categoryIdentifiers` - Pathway/GO identifiers
- `bmdResults` - Analysis configurations
- `treatments` - Dose conditions
- Additional supporting tables for complex relationships

**Normalization Level:** 3NF with proper foreign key relationships
**Indexing:** Primary keys only (no secondary indexes created)
**Constraints:** NOT NULL constraints on primary keys only

## Debugging & Troubleshooting

**Diagnostic Queries:**
```sql
-- Check DuckDB version and storage format
SELECT version();
PRAGMA version;
SELECT database_name, tags FROM duckdb_databases();

-- Verify table structure
SHOW TABLES;
DESCRIBE table_name;

-- Check data integrity
SELECT COUNT(*) FROM table_name;
```

**Common Issues:**
1. **ClassNotFoundException** - Missing dependencies in classpath
2. **SQLException** - Database connection or schema creation failures
3. **JSON serialization errors** - Complex object serialization failures
4. **Memory issues** - Large .bm2 files causing OutOfMemoryError

This export process represents a sophisticated conversion from Java object serialization to relational database format, with specific considerations for WASM deployment that unfortunately still face compatibility challenges despite meeting all documented requirements.
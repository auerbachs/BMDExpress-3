# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BMDExpress-3 is a JavaFX application for dose-response modeling and benchmark dose analysis of high-dimensional biological data, particularly gene expression data. It uses EPA BMDS models and ToxicR statistical methods to analyze dose-response relationships and calculate benchmark dose values for genes and gene sets.

## Architecture

### Core Structure
- **Main Entry Point**: `com.sciome.bmdexpress2.BMDExpress3Main` - JavaFX application launcher (note: package is bmdexpress2, not bmdexpress3)
- **MVP Pattern**: Code follows Model-View-Presenter pattern with clear separation in `src/main/java/com/sciome/bmdexpress2/mvp/`
  - Models: Data structures and business logic
  - Views: FXML-based JavaFX UI components  
  - Presenters: Logic controllers connecting views and models
- **Command Line Interface**: Full CLI support in `com.sciome.bmdexpress2.commandline` package
- **Module System**: Uses Java modules (see `module-info.java`)

### Key Components
- **Statistical Analysis**: Pre-filtering (ANOVA, curve fitting, Williams trend), BMD analysis, category analysis
- **Data Models**: Expression data, dose-response experiments, statistical results
- **Visualization**: Chart generation using JFreeChart
- **Data Import/Export**: Support for various biological data formats
- **Event System**: Uses Google Guava EventBus for component communication

### Package Structure
- `com.sciome.bmdexpress2.mvp.model.*` - Core data models
- `com.sciome.bmdexpress2.util.*` - Utility classes for analysis algorithms
- `com.sciome.bmdexpress2.commandline.*` - CLI interface and configuration
- Legacy `org.ciit.*` packages - Original BMDExpress code

## Development Commands

### Build and Run
```bash
# Clean and compile
mvn clean compile

# Run application (GUI)
mvn javafx:run

# Build executable JAR with dependencies
mvn package assembly:single

# Run with command line arguments
java -jar target/bmdexpress3-3.0.0-SNAPSHOT-jar-with-dependencies.jar [CLI_OPTIONS]
```

### Testing
No formal test suite is configured - testing appears to be manual through the GUI and CLI.

### Dependencies
- **Java 21** (required) - Liberica JDK 21 Full is available and recommended
- **JavaFX 17.0.13** - UI framework (compatible with Java 21)
- **JFreeChart** - Charting and visualization
- **Jackson** - JSON serialization
- **Apache Commons** - Utilities (Math, Lang, CLI, IO)
- **Google Guava** - EventBus and utilities
- **ControlsFX** - Additional JavaFX controls
- **Sciome Commons Math** - Custom mathematical functions
- **DuckDB JDBC 1.3.2.0** - Database export functionality (added)

## Important Notes

- The application supports both GUI and headless command-line operation
- Data persistence uses custom .bm2 file format (JSON-based)
- Module system requires specific `--add-exports` flags for ControlsFX integration (configured in pom.xml)
- Note: DuckDB JDBC dependency is not declared in module-info.java but is available on classpath
- ToxicR integration provides Bayesian model averaging capabilities
- Forward toxicokinetic modeling for internal dose estimation

## CLI Usage Pattern
The command-line interface supports various analysis workflows:
- Expression data import
- Pre-filtering (ANOVA, curve fitting, Williams trend)
- BMD analysis with multiple model types
- Category/pathway analysis
- Data export in multiple formats

Configuration is typically done via JSON config files passed to the CLI runners.

## DuckDB Database Export Feature

A new DuckDB export capability has been implemented to convert .bm2 files to queryable SQL databases, providing a flattened, normalized alternative to the hierarchical JSON export.

### Files Added/Modified
- **New**: `src/main/java/com/sciome/bmdexpress2/service/DuckDBExportServiceV4.java` - Latest export service with enhanced schema and metadata support
- **New**: `src/main/java/com/sciome/bmdexpress2/commandline/DuckDBExportRunnerV4.java` - Updated standalone command-line runner
- **Modified**: `src/main/java/com/sciome/bmdexpress2/commandline/BMDExpressCommandLine.java` - Added `export-duckdb` command
- **Modified**: `pom.xml` - Added DuckDB JDBC driver dependency with WASM compatibility

### Database Schema
The exporter creates 15+ normalized tables:
- `projects` - Project metadata and names
- `dose_response_experiments` - Experimental designs with dose groups
- `treatments` - Individual dose/treatment conditions  
- `probes` - Gene/probe identifiers and annotations
- `probe_responses` - Flattened expression data (probe × treatment)
- `bmd_results` - BMD analysis metadata
- `probe_stat_results` - Statistical results per probe (best model, BMD values)
- `stat_results` - Individual model fit results for all models tested
- `category_analysis_results` - Pathway/GO analysis metadata
- `category_results` - Individual pathway/category results with statistics
- `prefilter_results` - ANOVA, Williams, Oriogen, CurveFit results
- `prefilter_probe_results` - Per-probe prefilter statistics
- `analysis_info` - Analysis metadata and parameters
- `chips` - Microarray/platform information
- `reference_gene_annotations` - Gene annotation mappings

### Usage

#### Command Line Export
```bash
# Standalone runner V4 (recommended for testing)
java -cp 'target/classes:lib/*' com.sciome.bmdexpress2.commandline.DuckDBExportRunnerV4 input.bm2 output.duckdb

# Integrated CLI command
java -cp 'target/classes:lib/*' com.sciome.bmdexpress2.commandline.BMDExpressCommandLine export-duckdb --input-bm2 input.bm2 --output-file-name output.duckdb
```

#### Querying the Database
```bash
# Install DuckDB CLI
brew install duckdb  # macOS
# or download from https://duckdb.org/

# Query your data
duckdb output.duckdb
```

#### Example SQL Queries
```sql
-- Find probes with low BMD values
SELECT p.symbol, psr.best_bmd, psr.best_model 
FROM probe_stat_results psr
JOIN probes p ON psr.probe_id = p.id  
WHERE psr.best_bmd < 10
ORDER BY psr.best_bmd;

-- Get enriched pathways with low p-values
SELECT category_description, bmd_median, p_value, genes_in_category
FROM category_results  
WHERE p_value < 0.05
ORDER BY p_value;

-- Compare BMD distributions across different methods
SELECT br.bmd_method, 
       percentile_cont(0.5) WITHIN GROUP (ORDER BY psr.best_bmd) as median_bmd,
       avg(psr.best_bmd) as mean_bmd
FROM probe_stat_results psr
JOIN bmd_results br ON psr.bmd_result_id = br.id  
GROUP BY br.bmd_method;

-- Export data for external analysis
COPY (SELECT * FROM probe_stat_results) TO 'bmd_results.csv' WITH CSV HEADER;
```

### Benefits over JSON Export
- **Queryable**: Use SQL to filter, aggregate, and join data across experiments
- **Efficient**: Normalized schema reduces data redundancy and storage
- **Interoperable**: Works with R, Python, and other data analysis tools
- **Scalable**: DuckDB handles large datasets efficiently
- **Direct**: No intermediate JSON serialization required

### Integration with Analysis Tools
```python
# Python
import duckdb
conn = duckdb.connect('mydata.duckdb')
df = conn.execute('SELECT * FROM probe_stat_results WHERE best_bmd < 10').df()
```

```r
# R
library(duckdb)
con <- dbConnect(duckdb(), 'mydata.duckdb')
results <- dbGetQuery(con, 'SELECT * FROM probe_stat_results WHERE best_bmd < 10')
```

### Technical Notes
- The .bm2 file format uses Java serialization, requiring BMDExpress classes on classpath
- DuckDB files work best on local drives (external drives may have locking issues)
- All object relationships are preserved through foreign key relationships
- Large datasets (probe responses) are efficiently stored and indexed
- Export handles missing data and null values appropriately
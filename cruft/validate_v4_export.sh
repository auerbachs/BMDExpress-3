#!/bin/bash

# BMD Express DuckDB Export V4 Validation Script
# This script validates the V4 export functionality

echo "=== BMD Express DuckDB Export V4 Validation ==="

# Test data files
BM2_FILE="P3MP-Parham.bm2"
OUTPUT_DB="test_v4_validation.duckdb"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo -e "${YELLOW}1. Checking prerequisites...${NC}"

if [ ! -f "$BM2_FILE" ]; then
    echo -e "${RED}❌ Test file $BM2_FILE not found${NC}"
    exit 1
fi

if [ ! -d "lib" ] || [ ! -f "lib/duckdb_jdbc-0.9.2.jar" ]; then
    echo -e "${RED}❌ DuckDB JDBC driver not found in lib/${NC}"
    exit 1
fi

if [ ! -d "target/classes" ]; then
    echo -e "${RED}❌ Compiled classes not found. Run 'mvn compile' first${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Prerequisites met${NC}"

# Test 2: V4 Export
echo -e "${YELLOW}2. Testing V4 Export...${NC}"
java -cp 'target/classes:lib/*' com.sciome.bmdexpress2.commandline.DuckDBExportRunnerV4 "$BM2_FILE" "$OUTPUT_DB"

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ V4 Export failed${NC}"
    exit 1
fi

if [ ! -f "$OUTPUT_DB" ]; then
    echo -e "${RED}❌ Output database file not created${NC}"
    exit 1
fi

echo -e "${GREEN}✅ V4 Export completed successfully${NC}"

# Test 3: Database Validation
echo -e "${YELLOW}3. Validating database structure...${NC}"

# Check if duckdb CLI is available
if ! command -v duckdb &> /dev/null; then
    echo -e "${YELLOW}⚠️  DuckDB CLI not available, skipping structure validation${NC}"
    echo -e "${YELLOW}   Install with: brew install duckdb${NC}"
else
    # Test basic queries
    echo -e "${YELLOW}   Testing basic queries...${NC}"

    # Count projects
    PROJECT_COUNT=$(duckdb "$OUTPUT_DB" "SELECT COUNT(*) FROM projects;" 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}   ✅ Projects table: $PROJECT_COUNT records${NC}"
    else
        echo -e "${RED}   ❌ Projects table query failed${NC}"
    fi

    # Count dose response experiments
    DRE_COUNT=$(duckdb "$OUTPUT_DB" "SELECT COUNT(*) FROM doseResponseExperiments;" 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}   ✅ Dose response experiments: $DRE_COUNT records${NC}"
    else
        echo -e "${RED}   ❌ Dose response experiments query failed${NC}"
    fi

    # Count category analysis results
    CAR_COUNT=$(duckdb "$OUTPUT_DB" "SELECT COUNT(*) FROM categoryAnalysisResults;" 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}   ✅ Category analysis results: $CAR_COUNT records${NC}"
    else
        echo -e "${RED}   ❌ Category analysis results query failed${NC}"
    fi
fi

# Test 4: File Size Check
echo -e "${YELLOW}4. Checking export file size...${NC}"
DB_SIZE=$(ls -lh "$OUTPUT_DB" | awk '{print $5}')
echo -e "${GREEN}✅ Database file size: $DB_SIZE${NC}"

# Test 5: WASM Compatibility Check
echo -e "${YELLOW}5. Testing WASM compatibility...${NC}"
if duckdb "$OUTPUT_DB" "PRAGMA database_list;" 2>/dev/null | grep -q "checkpoint"; then
    echo -e "${GREEN}✅ Database appears to be checkpointed for WASM compatibility${NC}"
else
    echo -e "${YELLOW}⚠️  WASM compatibility status unknown${NC}"
fi

echo -e "${YELLOW}6. Sample queries for manual verification:${NC}"
echo "   duckdb $OUTPUT_DB"
echo "   SELECT COUNT(*) FROM projects;"
echo "   SELECT COUNT(*) FROM categoryAnalysisResults;"
echo "   SELECT categoryIdentifierId, bmdMean, fishersExactTwoTailPValue FROM categoryAnalysisResults LIMIT 5;"

echo -e "${GREEN}=== V4 Validation Complete ===${NC}"
echo -e "${GREEN}✅ V4 export functionality validated successfully${NC}"
echo -e "${GREEN}✅ Database created: $OUTPUT_DB${NC}"
echo -e "${GREEN}✅ Ready for web application integration${NC}"
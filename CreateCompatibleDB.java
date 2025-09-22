import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateCompatibleDB {
    public static void main(String[] args) {
        try {
            Class.forName("org.duckdb.DuckDBDriver");

            String sourcePath = "dehp_correct_version.duckdb";
            String targetPath = "dehp_wasm_compatible_final.duckdb";

            System.out.println("Creating WASM-compatible database...");
            System.out.println("Source: " + sourcePath);
            System.out.println("Target: " + targetPath);

            try (Connection conn = DriverManager.getConnection("jdbc:duckdb:");
                 Statement stmt = conn.createStatement()) {

                // Attach source database
                System.out.println("1. Attaching source database...");
                stmt.execute("ATTACH '" + sourcePath + "' AS source");

                // Create new database with explicit storage version
                System.out.println("2. Creating target database with compatible storage version...");
                stmt.execute("ATTACH '" + targetPath + "' (STORAGE_VERSION 'v1.3.2') AS target");

                // Get list of tables from source
                System.out.println("3. Copying tables...");
                var rs = stmt.executeQuery("SHOW TABLES FROM source");
                while (rs.next()) {
                    String tableName = rs.getString(1);
                    System.out.println("   Copying table: " + tableName);
                    stmt.execute("CREATE TABLE target.main." + tableName + " AS SELECT * FROM source.main." + tableName);
                }
                rs.close();

                // Checkpoint to ensure everything is flushed
                System.out.println("4. Checkpointing...");
                stmt.execute("CHECKPOINT");

                System.out.println("✅ WASM-compatible database created successfully!");

            } catch (Exception e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            System.err.println("DuckDB JDBC driver not found: " + e.getMessage());
        }
    }
}
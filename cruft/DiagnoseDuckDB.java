import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DiagnoseDuckDB {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java DiagnoseDuckDB <database-file>");
            System.exit(1);
        }

        String dbPath = args[0];

        try {
            Class.forName("org.duckdb.DuckDBDriver");

            try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath);
                 Statement stmt = conn.createStatement()) {

                System.out.println("=== DuckDB JDBC Diagnostics ===");
                System.out.println("Database: " + dbPath);
                System.out.println();

                // 1. Engine version
                System.out.println("1. Engine version:");
                ResultSet rs = stmt.executeQuery("SELECT version()");
                if (rs.next()) {
                    System.out.println("   version(): " + rs.getString(1));
                }
                rs.close();

                rs = stmt.executeQuery("PRAGMA version");
                System.out.println("   PRAGMA version:");
                while (rs.next()) {
                    System.out.println("     library_version: " + rs.getString("library_version"));
                    System.out.println("     source_id: " + rs.getString("source_id"));
                }
                rs.close();

                // 2. Storage tags
                System.out.println();
                System.out.println("2. Storage tags:");
                try {
                    rs = stmt.executeQuery("SELECT database_name, tags FROM duckdb_databases()");
                    while (rs.next()) {
                        System.out.println("   Database: " + rs.getString("database_name"));
                        System.out.println("   Tags: " + rs.getString("tags"));
                    }
                    rs.close();
                } catch (Exception e) {
                    System.out.println("   Error querying duckdb_databases(): " + e.getMessage());
                }

                // 3. Show tables
                System.out.println();
                System.out.println("3. Available tables:");
                rs = stmt.executeQuery("SHOW TABLES");
                while (rs.next()) {
                    System.out.println("   - " + rs.getString(1));
                }
                rs.close();

            } catch (Exception e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            System.err.println("DuckDB JDBC driver not found: " + e.getMessage());
        }
    }
}
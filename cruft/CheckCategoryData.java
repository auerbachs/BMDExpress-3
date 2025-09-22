import java.sql.*;

public class CheckCategoryData {
    public static void main(String[] args) throws Exception {
        Class.forName("org.duckdb.DuckDBDriver");
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:dehp_webapp_schema.duckdb")) {
            System.out.println("Category Analysis Results - Sample data:");
            System.out.println("=====================================");
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT categoryIdentifierId, modelType, geneAllCount, percentage FROM categoryAnalysisResults LIMIT 10")) {
                
                while (rs.next()) {
                    String categoryId = rs.getString(1);
                    String modelType = rs.getString(2);
                    long geneCount = rs.getLong(3);
                    double percentage = rs.getDouble(4);
                    System.out.println("ID: " + categoryId + ", ModelType: " + modelType + ", Genes: " + geneCount + ", %: " + percentage);
                }
            }
            
            System.out.println("\nUnique categoryIdentifierId values:");
            System.out.println("=====================================");
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT categoryIdentifierId FROM categoryAnalysisResults LIMIT 20")) {
                
                while (rs.next()) {
                    System.out.println(rs.getString(1));
                }
            }
        }
    }
}

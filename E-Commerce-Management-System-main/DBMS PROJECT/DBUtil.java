import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBUtil — Centralized Database Utility Class
 * --------------------------------------------
 * Purpose:
 *  • Manages database connections for the entire application.
 *  • Loads the MySQL driver only once (thread-safe).
 *  • Provides clean exception handling and connection testing.
 *
 * Configuration:
 *  • Update DB_URL, USER, and PASSWORD as per your setup.
 */
public class DBUtil {

    // ✅ Database Configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/dbmspj?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    // ✅ Load driver only once
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver loaded successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Failed to load MySQL JDBC Driver.");
            e.printStackTrace();
        }
    }

    /**
     * Returns a connection to the database.
     *
     * @return Connection object if successful
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            if (conn != null && !conn.isClosed()) {
                // Optional: print for debugging
                // System.out.println("✅ Connected to Database: " + DB_URL);
            }
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed! Check DB URL, credentials, or MySQL service.");
            throw e;
        }
    }

    /**
     * Test method — run this class directly to verify your DB connection.
     */
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Database connection successful!");
            } else {
                System.out.println("❌ Failed to establish a database connection.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

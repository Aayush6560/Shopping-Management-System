import java.sql.*;

public class DBConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/dbmspj";
        String user = "root";
        String password = "12345";

        try {
            // Explicitly load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(url, user, password);
            if (conn != null) {
                System.out.println("✅ Database connection successful!");
                conn.close();
            } else {
                System.out.println("❌ Connection returned null!");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver not found. Please check your JAR file.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed!");
            e.printStackTrace();
        }
    }
}

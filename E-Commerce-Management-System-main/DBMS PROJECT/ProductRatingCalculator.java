import java.sql.*;

/**
 * Utility class to calculate the average rating for a given product.
 */
public class ProductRatingCalculator {

    // ✅ Better: Centralize DB credentials or use DBUtil.getConnection()
    private static final String DB_URL = "jdbc:mysql://localhost:3306/dbmspj";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345";

    /**
     * Retrieves the average rating for a specific product from the review table.
     *
     * @param productId The ID of the product to calculate the rating for.
     * @return The average rating (0.0 if no reviews are found).
     */
    public double getAverageRating(int productId) {
        String query = "SELECT AVG(RATING) AS avg_rating FROM review WHERE PRODUCT_ID = ?";
        double avgRating = 0.0;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                avgRating = rs.getDouble("avg_rating");
                if (rs.wasNull()) { // Handle null in case there are no ratings
                    avgRating = 0.0;
                }
            }

        } catch (SQLException e) {
            System.err.println("⚠️ Error fetching average rating for product ID " + productId + ": " + e.getMessage());
        }

        return avgRating;
    }
}

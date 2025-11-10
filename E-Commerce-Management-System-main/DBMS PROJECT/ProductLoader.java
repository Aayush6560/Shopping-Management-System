import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads products from the database along with any active discounts.
 * Uses DBUtil for database connection handling.
 */
public class ProductLoader {

    /**
     * Loads all products under a given category, applying active discounts.
     *
     * @param categoryId The category ID to filter products.
     * @return List of Product objects with adjusted final prices.
     */
    public List<Product> loadProducts(int categoryId) {
        List<Product> products = new ArrayList<>();

        String productQuery = """
            SELECT PRODUCT_ID, STYLE, STOCK_QUANTITY, PRICE, DISCOUNT_ID
            FROM PRODUCT
            WHERE CATEGORY_ID = ?
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(productQuery)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("PRODUCT_ID");
                    String style = rs.getString("STYLE");
                    int stock = rs.getInt("STOCK_QUANTITY");
                    double price = rs.getDouble("PRICE");

                    int discountId = rs.getInt("DISCOUNT_ID");
                    if (rs.wasNull()) discountId = 0; // handle NULL safely

                    double finalPrice = price;
                    if (discountId > 0) {
                        finalPrice = applyDiscount(conn, discountId, price);
                    }

                    Product product = new Product(id, style, stock, price, finalPrice);
                    products.add(product);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error loading products: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }

        return products;
    }

    /**
     * Applies discount to a product if it’s valid for today’s date.
     *
     * @param conn       Active DB connection.
     * @param discountId Discount ID linked to the product.
     * @param price      Original product price.
     * @return Final discounted price or original price if not applicable.
     */
    private double applyDiscount(Connection conn, int discountId, double price) {
        String discountQuery = """
            SELECT START_DATE, END_DATE, DISCOUNT_PERCENT
            FROM DISCOUNT
            WHERE DISCOUNT_ID = ?
        """;

        try (PreparedStatement dStmt = conn.prepareStatement(discountQuery)) {
            dStmt.setInt(1, discountId);
            try (ResultSet drs = dStmt.executeQuery()) {
                if (drs.next()) {
                    Date startDate = drs.getDate("START_DATE");
                    Date endDate = drs.getDate("END_DATE");
                    double percent = drs.getDouble("DISCOUNT_PERCENT");

                    LocalDate today = LocalDate.now();
                    if (startDate != null && endDate != null &&
                        !today.isBefore(startDate.toLocalDate()) &&
                        !today.isAfter(endDate.toLocalDate())) {
                        return price - (price * percent / 100.0);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error applying discount (ID: " + discountId + "): " + e.getMessage());
        }

        return price; // fallback to original price
    }
}

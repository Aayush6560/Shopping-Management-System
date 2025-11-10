import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * ShoppingHistoryPanel – Displays the customer's shopping history
 * and allows submitting reviews for purchased products.
 */
public class ShoppingHistoryPanel extends JPanel {

    // ✅ Database credentials (update if needed)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/dbmspj";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345";

    public ShoppingHistoryPanel(int customerId) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("🛒 Shopping History"));

        // 🧾 Table setup
        String[] columns = {"Shopping ID", "Date", "City", "State", "Country", "Payment ID", "Payment Method"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Load shopping history
        loadShoppingHistory(customerId, model);

        // ✍️ Review section
        JPanel reviewPanel = new JPanel(new BorderLayout(10, 10));
        reviewPanel.setBorder(BorderFactory.createTitledBorder("📝 Write Product Review"));

        JTextArea reviewArea = new JTextArea(4, 30);
        reviewArea.setLineWrap(true);
        reviewArea.setWrapStyleWord(true);
        JTextField ratingField = new JTextField();
        JButton submitReviewButton = new JButton("Submit Review");

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        formPanel.add(new JLabel("Rating (1–5):"));
        formPanel.add(ratingField);
        formPanel.add(new JLabel("Review:"));
        formPanel.add(new JScrollPane(reviewArea));

        reviewPanel.add(formPanel, BorderLayout.CENTER);
        reviewPanel.add(submitReviewButton, BorderLayout.SOUTH);

        // 🎯 Submit review button action
        submitReviewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a shopping record first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String feedback = reviewArea.getText().trim();
            if (feedback.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Review cannot be empty.", "Empty Review", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int rating;
            try {
                rating = Integer.parseInt(ratingField.getText().trim());
                if (rating < 1 || rating > 5) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid rating between 1 and 5.", "Invalid Rating", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int shoppingId = (int) model.getValueAt(selectedRow, 0);
            int productId = fetchProductIdFromShopping(shoppingId);

            if (productId == -1) {
                JOptionPane.showMessageDialog(this, "No product found for this shopping entry.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            submitReview(productId, feedback, rating, customerId);
            reviewArea.setText("");
            ratingField.setText("");
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(reviewPanel, BorderLayout.SOUTH);
    }

    /**
     * 🔍 Load shopping history for a specific customer.
     */
    private void loadShoppingHistory(int customerId, DefaultTableModel model) {
        String sql = """
            SELECT s.SHOPPING_ID, s.SHOPPING_DATE, s.CITY, s.STATE, s.COUNTRY,
                   s.PAYMENT_ID, pm.MethodName
            FROM shopping s
            LEFT JOIN payment p ON s.PAYMENT_ID = p.PaymentID
            LEFT JOIN paymentmethods pm ON p.PaymentMethodID = pm.PaymentMethodID
            WHERE s.CUSTOMER_ID = ?
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Object[] row = {
                        rs.getInt("SHOPPING_ID"),
                        rs.getDate("SHOPPING_DATE"),
                        rs.getString("CITY"),
                        rs.getString("STATE"),
                        rs.getString("COUNTRY"),
                        rs.getInt("PAYMENT_ID"),
                        rs.getString("MethodName")
                };
                model.addRow(row);
            }

            if (!hasData) {
                JOptionPane.showMessageDialog(this, "No shopping history found for this customer.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "❌ Database error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 🧮 Fetch product ID linked to a shopping ID.
     */
    private int fetchProductIdFromShopping(int shoppingId) {
        String sql = "SELECT PRODUCT_ID FROM shoppingdetails WHERE SHOPPING_ID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, shoppingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("PRODUCT_ID");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Not found
    }

    /**
     * 💬 Submit a review for a product.
     */
    private void submitReview(int productId, String feedback, int rating, int customerId) {
        String sql = "INSERT INTO review (PRODUCT_ID, FEEDBACK, RATING, CUSTOMER_ID) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setString(2, feedback);
            stmt.setInt(3, rating);
            stmt.setInt(4, customerId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Review submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "❌ Error submitting review: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

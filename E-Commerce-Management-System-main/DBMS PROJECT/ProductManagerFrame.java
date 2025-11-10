import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ProductManagerFrame extends JFrame {

    private JTable productTable;
    private DefaultTableModel tableModel;

    public ProductManagerFrame() {
        setTitle("🛍️ Product Management");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ===== HEADER =====
        JLabel header = new JLabel("Product Management Dashboard", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setOpaque(true);
        header.setBackground(new Color(60, 120, 220));
        header.setForeground(Color.WHITE);
        header.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // ===== TABLE =====
        String[] columns = {"ID", "Style", "Category ID", "Price", "Stock", "Discount ID"};
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.setRowHeight(28);
        productTable.setSelectionBackground(new Color(200, 230, 255));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        loadProducts();

        JScrollPane tableScroll = new JScrollPane(productTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(tableScroll, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton addBtn = createStyledButton("➕ Add Product", new Color(60, 180, 75));
        JButton updateBtn = createStyledButton("✏️ Update", new Color(250, 180, 50));
        JButton deleteBtn = createStyledButton("🗑️ Delete", new Color(230, 80, 80));
        JButton refreshBtn = createStyledButton("🔁 Refresh", new Color(60, 120, 220));

        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // ===== ACTIONS =====
        addBtn.addActionListener(e -> addProduct());
        updateBtn.addActionListener(e -> updateProduct());
        deleteBtn.addActionListener(e -> deleteProduct());
        refreshBtn.addActionListener(e -> loadProducts());

        setVisible(true);
    }

    // ===== BUTTON STYLING METHOD =====
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(150, 40));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // ===== LOAD PRODUCTS =====
    private void loadProducts() {
        tableModel.setRowCount(0);
        String query = "SELECT PRODUCT_ID, STYLE, CATEGORY_ID, PRICE, STOCK_QUANTITY, DISCOUNT_ID FROM product";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("PRODUCT_ID"),
                        rs.getString("STYLE"),
                        rs.getInt("CATEGORY_ID"),
                        rs.getDouble("PRICE"),
                        rs.getInt("STOCK_QUANTITY"),
                        rs.getObject("DISCOUNT_ID")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "⚠️ Error loading products:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== ADD PRODUCT =====
    private void addProduct() {
        JTextField styleField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField discountField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.add(new JLabel("Style:")); panel.add(styleField);
        panel.add(new JLabel("Category ID:")); panel.add(categoryField);
        panel.add(new JLabel("Price:")); panel.add(priceField);
        panel.add(new JLabel("Stock Quantity:")); panel.add(stockField);
        panel.add(new JLabel("Discount ID (optional):")); panel.add(discountField);

        int result = JOptionPane.showConfirmDialog(this, panel, "➕ Add Product",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String style = styleField.getText().trim();
            String catText = categoryField.getText().trim();
            String priceText = priceField.getText().trim();
            String stockText = stockField.getText().trim();
            String discountText = discountField.getText().trim();

            // ✅ Input validation
            if (style.isEmpty() || catText.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❗ Please fill all required fields!");
                return;
            }

            try {
                int category = Integer.parseInt(catText);
                double price = Double.parseDouble(priceText);
                int stock = Integer.parseInt(stockText);

                try (Connection conn = DBUtil.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "INSERT INTO product (STYLE, CATEGORY_ID, PRICE, STOCK_QUANTITY, DISCOUNT_ID) VALUES (?, ?, ?, ?, ?)")) {
                    stmt.setString(1, style);
                    stmt.setInt(2, category);
                    stmt.setDouble(3, price);
                    stmt.setInt(4, stock);

                    if (discountText.isEmpty())
                        stmt.setNull(5, java.sql.Types.INTEGER);
                    else
                        stmt.setInt(5, Integer.parseInt(discountText));

                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "✅ Product added successfully!");
                    loadProducts();

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "⚠️ Database Error:\n" + ex.getMessage());
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "❌ Please enter valid numeric values for Category, Price, and Stock!");
            }
        }
    }

    // ===== UPDATE PRODUCT =====
    private void updateProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "⚠️ Please select a product to update!");
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        String style = (String) tableModel.getValueAt(selectedRow, 1);
        double price = (double) tableModel.getValueAt(selectedRow, 3);
        int stock = (int) tableModel.getValueAt(selectedRow, 4);

        JTextField styleField = new JTextField(style);
        JTextField priceField = new JTextField(String.valueOf(price));
        JTextField stockField = new JTextField(String.valueOf(stock));

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Style:")); panel.add(styleField);
        panel.add(new JLabel("Price:")); panel.add(priceField);
        panel.add(new JLabel("Stock Quantity:")); panel.add(stockField);

        int result = JOptionPane.showConfirmDialog(this, panel, "✏️ Update Product",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double newPrice = Double.parseDouble(priceField.getText().trim());
                int newStock = Integer.parseInt(stockField.getText().trim());

                try (Connection conn = DBUtil.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "UPDATE product SET STYLE = ?, PRICE = ?, STOCK_QUANTITY = ? WHERE PRODUCT_ID = ?")) {
                    stmt.setString(1, styleField.getText().trim());
                    stmt.setDouble(2, newPrice);
                    stmt.setInt(3, newStock);
                    stmt.setInt(4, productId);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "✅ Product updated successfully!");
                    loadProducts();
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "❌ Invalid number input! Please check price and stock.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "⚠️ Error updating product:\n" + ex.getMessage());
            }
        }
    }

    // ===== DELETE PRODUCT =====
    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "⚠️ Please select a product to delete!");
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this product?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM product WHERE PRODUCT_ID = ?")) {
                stmt.setInt(1, productId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "🗑️ Product deleted successfully!");
                loadProducts();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "⚠️ Error deleting product:\n" + ex.getMessage());
            }
        }
    }

    // ===== MAIN METHOD =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProductManagerFrame::new);
    }
}

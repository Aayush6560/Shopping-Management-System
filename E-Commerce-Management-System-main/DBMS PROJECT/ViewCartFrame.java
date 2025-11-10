import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;

public class ViewCartFrame extends JFrame {

    private final HashMap<String, Integer> cart;
    private final int customerId;
    private JPanel cartPanel, itemsPanel;

    public ViewCartFrame(HashMap<String, Integer> cart, int customerId) {
        this.cart = cart;
        this.customerId = customerId;
        initComponents();
    }

    private void initComponents() {
        setTitle("🛒 Your Shopping Cart");
        setSize(700, 550);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Customer info (address + mobile)
        if (customerId != -1) {
            addCustomerInfoPanel();
        }

        // Items Section
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(BorderFactory.createTitledBorder("🧾 Items in Cart"));

        if (cart.isEmpty()) {
            JLabel emptyLabel = new JLabel("Your cart is empty.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 15));
            itemsPanel.add(emptyLabel);
        } else {
            populateCartItems();
        }

        cartPanel.add(itemsPanel);
        add(new JScrollPane(cartPanel), BorderLayout.CENTER);

        // Checkout button
        JButton checkoutButton = new JButton("Proceed to Checkout");
        checkoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        checkoutButton.setBackground(new Color(50, 150, 250));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFocusPainted(false);
        checkoutButton.addActionListener(e -> proceedToCheckout());
        add(checkoutButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addCustomerInfoPanel() {
        try (Connection conn = DBUtil.getConnection()) {

            JPanel addressPanel = new JPanel(new BorderLayout(5, 5));
            addressPanel.setBorder(BorderFactory.createTitledBorder("📦 Shipping Address"));

            String addressQuery = "SELECT CITY, STATE, COUNTRY FROM customer WHERE CUSTOMER_ID = ?";
            PreparedStatement addressStmt = conn.prepareStatement(addressQuery);
            addressStmt.setInt(1, customerId);
            ResultSet addressRs = addressStmt.executeQuery();

            JTextArea addressArea = new JTextArea(5, 30);
            addressArea.setEditable(false);
            addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            if (addressRs.next()) {
                addressArea.setText("City: " + addressRs.getString("CITY") +
                        "\nState: " + addressRs.getString("STATE") +
                        "\nCountry: " + addressRs.getString("COUNTRY"));
            }

            // Retrieve contact numbers
            ArrayList<String> mobileNumbers = new ArrayList<>();
            String mobileQuery = "SELECT MOBILE_NO FROM customer_mobile WHERE CUSTOMER_ID = ?";
            PreparedStatement mobileStmt = conn.prepareStatement(mobileQuery);
            mobileStmt.setInt(1, customerId);
            ResultSet mobRs = mobileStmt.executeQuery();

            while (mobRs.next()) {
                mobileNumbers.add(mobRs.getString("MOBILE_NO"));
            }

            if (!mobileNumbers.isEmpty()) {
                addressArea.append("\n\nContact Numbers:\n");
                for (String mobileNo : mobileNumbers) {
                    addressArea.append("- " + mobileNo + "\n");
                }
            }

            addressPanel.add(new JScrollPane(addressArea), BorderLayout.CENTER);
            cartPanel.add(addressPanel);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "⚠️ Error loading customer info:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateCartItems() {
        for (String item : new ArrayList<>(cart.keySet())) {
            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            itemPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

            JLabel iconLabel = new JLabel();
            setSmallProductImage(item, iconLabel);

            JLabel nameLabel = new JLabel(item);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLabel.setPreferredSize(new Dimension(150, 25));

            int productId = getProductIdByStyle(item);
            int quantity = cart.get(item);
            double originalPrice = getOriginalPrice(productId);
            double discountedPrice = getDiscountedPrice(productId);
            boolean hasDiscount = discountedPrice < originalPrice;

            JLabel qtyLabel = new JLabel("Qty: " + quantity);
            qtyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            JLabel priceLabel = new JLabel(hasDiscount
                    ? String.format("<html><strike>₹%.2f</strike> <font color='green'>₹%.2f</font></html>",
                    originalPrice, discountedPrice)
                    : String.format("₹%.2f", originalPrice));

            JButton increase = new JButton("+");
            JButton decrease = new JButton("-");
            JButton removeBtn = new JButton("Remove");

            increase.setFocusPainted(false);
            decrease.setFocusPainted(false);
            removeBtn.setFocusPainted(false);

            // Remove item
            removeBtn.addActionListener(e -> removeItem(item, itemPanel, productId));

            // Increase quantity
            increase.addActionListener(e -> updateQuantity(item, productId, qtyLabel, true));

            // Decrease quantity
            decrease.addActionListener(e -> updateQuantity(item, productId, qtyLabel, false));

            itemPanel.add(iconLabel);
            itemPanel.add(nameLabel);
            itemPanel.add(qtyLabel);
            itemPanel.add(priceLabel);
            itemPanel.add(decrease);
            itemPanel.add(increase);
            itemPanel.add(removeBtn);

            itemsPanel.add(itemPanel);
        }
    }

    private void removeItem(String item, JPanel itemPanel, int productId) {
        int currentQty = cart.get(item);
        int stock = getProductStock(productId);
        updateStock(productId, stock + currentQty);

        cart.remove(item);
        itemsPanel.remove(itemPanel);
        itemsPanel.revalidate();
        itemsPanel.repaint();

        if (cart.isEmpty()) {
            JLabel empty = new JLabel("Cart is empty.");
            empty.setFont(new Font("Arial", Font.ITALIC, 15));
            itemsPanel.add(empty);
        }
    }

    private void updateQuantity(String item, int productId, JLabel qtyLabel, boolean increase) {
        int currentQty = cart.get(item);
        int availableStock = getProductStock(productId);

        if (increase) {
            if (currentQty < availableStock) {
                cart.put(item, currentQty + 1);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cannot exceed available stock (" + availableStock + ") for " + item,
                        "Stock Limit", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else if (currentQty > 1) {
            cart.put(item, currentQty - 1);
        }

        qtyLabel.setText("Qty: " + cart.get(item));
    }

    private void setSmallProductImage(String style, JLabel imageLabel) {
        String imageName = style.toLowerCase().replace(" ", "_");
        String[] extensions = {".png", ".jpg", ".jpeg"};
        for (String ext : extensions) {
            File file = new File("images/" + imageName + ext);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                Image scaled = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                return;
            }
        }
        imageLabel.setIcon(new ImageIcon(
                new ImageIcon("images/noimage.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)
        ));
    }

    private int getProductIdByStyle(String style) {
        String query = "SELECT PRODUCT_ID FROM product WHERE STYLE = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, style);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("PRODUCT_ID") : -1;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    private int getProductStock(int productId) {
        String query = "SELECT STOCK_QUANTITY FROM product WHERE PRODUCT_ID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("STOCK_QUANTITY") : 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private void updateStock(int productId, int newQty) {
        String update = "UPDATE product SET STOCK_QUANTITY = ? WHERE PRODUCT_ID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setInt(1, newQty);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "⚠️ Error updating stock:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double getOriginalPrice(int productId) {
        String query = "SELECT PRICE FROM product WHERE PRODUCT_ID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("PRICE") : 0.0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }

    private double getDiscountedPrice(int productId) {
        double price = getOriginalPrice(productId);
        String discountQuery = """
            SELECT d.DISCOUNT_PERCENT 
            FROM product p 
            JOIN discount d ON p.discount_id = d.discount_id 
            WHERE p.product_id = ? 
            AND CURDATE() BETWEEN d.start_date AND d.end_date
        """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(discountQuery)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                price = price * (1 - rs.getDouble("DISCOUNT_PERCENT") / 100.0);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return price;
    }

    private void proceedToCheckout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "🛍️ Your cart is empty.", "Checkout", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (customerId == -1) {
            JOptionPane.showMessageDialog(this, "Please login to continue checkout.", "Checkout",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        new OrderSummaryFrame(cart, customerId);
        dispose();
    }

    public static void main(String[] args) {
        HashMap<String, Integer> sampleCart = new HashMap<>();
        sampleCart.put("Casual", 2);
        sampleCart.put("Formal", 1);
        SwingUtilities.invokeLater(() -> new ViewCartFrame(sampleCart, 1));
    }
}

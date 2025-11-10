import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.List;

public class ProductByCategoryApp extends JFrame {

    private JPanel categoryPanel;
    private JPanel productListPanel;
    private final HashMap<String, Integer> cart = new HashMap<>();
    private final int customerId;

    private final ProductLoader productLoader;
    private final ProductImageSetter productImageSetter;
    private final ProductRatingCalculator productRatingCalculator;

    // Database constants (reuse DBUtil for consistency)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/dbmspj";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345";

    public ProductByCategoryApp(int customerId) {
        this.customerId = customerId;
        this.productLoader = new ProductLoader();
        this.productImageSetter = new ProductImageSetter();
        this.productRatingCalculator = new ProductRatingCalculator();
        initComponents();
    }

    public ProductByCategoryApp() {
        this(-1);
    }

    // =============================== GUI SETUP ===============================
    private void initComponents() {
        setTitle("Products by Category");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        setContentPane(new JLabel(new ImageIcon("i.png")));
        getContentPane().setLayout(new BorderLayout());

        // Top Navigation Bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // ------------------ Categories ------------------
        categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        categoryPanel.setOpaque(false);

        String[] categories = {"Formal", "Casual", "Sports", "Denim"};
        int[] categoryIds = {1, 2, 3, 4};

        for (int i = 0; i < categories.length; i++) {
            JLabel categoryLabel = createCategoryLabel(categories[i], categoryIds[i]);
            categoryPanel.add(categoryLabel);
        }

        // ------------------ Right Side Buttons ------------------
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        rightPanel.setOpaque(false);

        JLabel profileIconLabel = createIconLabel("images/profile.png", 24, 24, "Profile", this::showProfileSection);
        JLabel cartLogo = createIconLabel("images/cart.png", 20, 20, "Cart", this::showCart);

        JButton viewCart = createButton("View Cart", e -> showCart());
        JButton profileButton = createButton("Profile", e -> showProfileSection());
        JButton supportButton = createButton("Support", e -> openSupportFrame());

        rightPanel.add(profileIconLabel);
        rightPanel.add(cartLogo);
        rightPanel.add(viewCart);
        rightPanel.add(profileButton);
        rightPanel.add(supportButton);

        topPanel.add(categoryPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ------------------ Product Display ------------------
        productListPanel = new JPanel(new GridLayout(0, 4, 15, 15));
        productListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(productListPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    // =============================== HELPERS ===============================
    private JLabel createCategoryLabel(String category, int catId) {
        JLabel label = new JLabel(category);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(Color.YELLOW);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                loadProducts(catId);
            }
        });
        return label;
    }

    private JLabel createIconLabel(String path, int width, int height, String tooltip, Runnable onClick) {
        ImageIcon icon = new ImageIcon(
                new ImageIcon(path).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        JLabel label = new JLabel(icon);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setToolTipText(tooltip);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
        return label;
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.addActionListener(listener);
        return button;
    }

    // =============================== MAIN LOGIC ===============================
    private void openSupportFrame() {
        if (customerId == -1) {
            JOptionPane.showMessageDialog(this, "Please login to use support.", "Support",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new SupportFrame(customerId).setVisible(true);
    }

    private void loadProducts(int categoryId) {
        productListPanel.removeAll();
        List<Product> products = productLoader.loadProducts(categoryId);

        for (Product product : products) {
            JPanel productPanel = createProductPanel(product);
            productListPanel.add(productPanel);
        }

        productListPanel.revalidate();
        productListPanel.repaint();
    }

    private JPanel createProductPanel(Product product) {
        int productId = product.getProductId();
        String style = product.getStyle();
        int stock = product.getStockQuantity();

        JPanel productPanel = new JPanel(new BorderLayout());
        productPanel.setPreferredSize(new Dimension(50, 50));
        productPanel.setBackground(new Color(255, 255, 255, 190));
        productPanel.setBorder(BorderFactory.createTitledBorder(style));

        JLabel imageLabel = new JLabel();
        productImageSetter.setProductImage(style, imageLabel);

        JLabel stockLabel = new JLabel("Stock: " + stock, SwingConstants.CENTER);
        stockLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel priceLabel = createPriceLabel(product);

        JButton actionButton = new JButton(cart.containsKey(style) ? "Remove" : "Add");
        actionButton.setFont(new Font("Arial", Font.PLAIN, 11));
        actionButton.setPreferredSize(new Dimension(70, 20));

        actionButton.addActionListener(e -> handleCartAction(product, stockLabel, actionButton));

        // Rating
        double avgRating = productRatingCalculator.getAverageRating(productId);
        JPanel ratingPanel = createRatingPanel(avgRating);

        JPanel centerPanel = new JPanel(new GridLayout(4, 1));
        centerPanel.setOpaque(false);
        centerPanel.add(priceLabel);
        centerPanel.add(stockLabel);
        centerPanel.add(ratingPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(actionButton);
        centerPanel.add(buttonPanel);

        productPanel.add(imageLabel, BorderLayout.NORTH);
        productPanel.add(centerPanel, BorderLayout.CENTER);
        return productPanel;
    }

    private JLabel createPriceLabel(Product product) {
        double original = product.getOriginalPrice();
        double discounted = product.getDiscountedPrice();
        JLabel priceLabel;
        if (discounted < original) {
            priceLabel = new JLabel(
                    "<html><span style='text-decoration: line-through;'>₹" +
                            String.format("%.2f", original) +
                            "</span> ₹" + String.format("%.2f", discounted) + "</html>",
                    SwingConstants.CENTER);
        } else {
            priceLabel = new JLabel("₹" + String.format("%.2f", original), SwingConstants.CENTER);
        }
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        return priceLabel;
    }

    private JPanel createRatingPanel(double avgRating) {
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        ratingPanel.setOpaque(false);

        ImageIcon starIcon = new ImageIcon(
                new ImageIcon("images/star.png").getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        JLabel starLabel = new JLabel(starIcon);

        JLabel ratingLabel = new JLabel(String.format("%.1f / 5", avgRating));
        ratingLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        ratingPanel.add(starLabel);
        ratingPanel.add(ratingLabel);
        return ratingPanel;
    }

    private void handleCartAction(Product product, JLabel stockLabel, JButton actionButton) {
        String style = product.getStyle();
        int productId = product.getProductId();
        int currentStock = product.getStockQuantity();

        if (cart.containsKey(style)) {
            int currentQty = cart.remove(style);
            updateStock(productId, currentStock + currentQty);
            stockLabel.setText("Stock: " + (currentStock + currentQty));
            actionButton.setText("Add");
            JOptionPane.showMessageDialog(this, "Removed from cart!");
        } else {
            cart.put(style, 1);
            updateStock(productId, currentStock - 1);
            stockLabel.setText("Stock: " + (currentStock - 1));
            actionButton.setText("Remove");
            JOptionPane.showMessageDialog(this, "Added to cart!");
        }
    }

    private void updateStock(int productId, int newQty) {
        String update = "UPDATE product SET STOCK_QUANTITY = ? WHERE PRODUCT_ID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setInt(1, newQty);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showCart() {
        new ViewCartFrame(cart, customerId).setVisible(true);
    }

    private void showProfileSection() {
        if (customerId == -1) {
            JOptionPane.showMessageDialog(this, "Please login to access your profile", "Profile",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new ProfileFrame(customerId).setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductByCategoryApp().setVisible(true));
    }
}

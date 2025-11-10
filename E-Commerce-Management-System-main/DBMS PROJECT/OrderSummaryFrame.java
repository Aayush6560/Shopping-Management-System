import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class OrderSummaryFrame extends JFrame {

    private final HashMap<String, Integer> cart;
    private final int customerId;
    private JComboBox<String> paymentMethodCombo;
    private double totalAmount;

    private String customerCity;
    private String customerState;
    private String customerCountry;

    public OrderSummaryFrame(HashMap<String, Integer> cart, int customerId) {
        this.cart = cart;
        this.customerId = customerId;

        setTitle("🛒 Order Summary");
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        initUI();

        setVisible(true);
    }
 
    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));

        // Left: user icon
        JLabel userIconLabel = new JLabel(new ImageIcon("user.png"));
        topPanel.add(userIconLabel, BorderLayout.WEST);

        // Right: logo
        JLabel logoLabel = new JLabel();
        ImageIcon icon = new ImageIcon("n1.png");
        Image scaled = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        logoLabel.setIcon(new ImageIcon(scaled));
        topPanel.add(logoLabel, BorderLayout.EAST);

        // Center: customer info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        JLabel dateLabel = new JLabel("Date: " + timestamp);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JTextArea customerArea = new JTextArea(5, 40);
        customerArea.setEditable(false);
        loadCustomerInfo(customerArea);

        centerPanel.add(dateLabel);
        centerPanel.add(new JScrollPane(customerArea));
        topPanel.add(centerPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

       
        String[] columnNames = {"Product Name", "Quantity", "Price (each)", "Subtotal", "Vendor Info"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable cartTable = new JTable(tableModel);
        totalAmount = 0.0;

        for (String style : cart.keySet()) {
            int quantity = cart.get(style);
            double price = getProductPriceByStyle(style);
            double subtotal = price * quantity;
            totalAmount += subtotal;
            String vendorInfo = getVendorInfoByStyle(style);

            Object[] row = {
                    style,
                    quantity,
                    String.format("₹ %.2f", price),
                    String.format("₹ %.2f", subtotal),
                    vendorInfo
            };
            tableModel.addRow(row);
        }

        JScrollPane tableScroll = new JScrollPane(cartTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Items Summary"));
        add(tableScroll, BorderLayout.CENTER);

       
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JLabel totalLabel = new JLabel("Total: ₹ " + String.format("%.2f", totalAmount));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(totalLabel, BorderLayout.WEST);

        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paymentPanel.add(new JLabel("Select Payment Method:"));
        String[] paymentOptions = {"Credit Card", "UPI", "Net Banking", "Cash"};
        paymentMethodCombo = new JComboBox<>(paymentOptions);
        paymentPanel.add(paymentMethodCombo);

        JButton confirmButton = new JButton("Confirm Order");
        confirmButton.setBackground(new Color(144, 238, 144));
        confirmButton.addActionListener(e -> confirmOrder());
        paymentPanel.add(confirmButton);

        JButton printPdfButton = new JButton("🖨 Print / Save PDF");
        printPdfButton.setBackground(new Color(255, 218, 185));
        printPdfButton.addActionListener(e -> printFullFrameAsPDF());
        paymentPanel.add(printPdfButton);

        bottomPanel.add(paymentPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

   
    private void loadCustomerInfo(JTextArea area) {
        String query = """
                SELECT FIRST_NAME, MIDDLE_NAME, LAST_NAME, COUNTRY, STATE, CITY, EMAIL, LOYALTY_POINTS 
                FROM customer WHERE CUSTOMER_ID = ?
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("FIRST_NAME") + " " +
                        rs.getString("MIDDLE_NAME") + " " +
                        rs.getString("LAST_NAME");

                customerCity = rs.getString("CITY");
                customerState = rs.getString("STATE");
                customerCountry = rs.getString("COUNTRY");

                String email = rs.getString("EMAIL");
                int loyaltyPoints = rs.getInt("LOYALTY_POINTS");

                area.setText("Customer Name: " + fullName + "\n" +
                        "Location: " + customerCity + ", " + customerState + ", " + customerCountry + "\n" +
                        "Email: " + email + "\n" +
                        "Loyalty Points: " + loyaltyPoints);
            } else {
                area.setText("Customer not found for ID: " + customerId);
            }
        } catch (SQLException e) {
            area.setText("Error loading customer data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    private double getProductPriceByStyle(String style) {
        double price = 0.0;
        String query = "SELECT PRICE FROM product WHERE STYLE = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, style);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) price = rs.getDouble("PRICE");

        } catch (SQLException e) {
            System.err.println("Error fetching price for style: " + style);
            e.printStackTrace();
        }
        return price;
    }

 
    private String getVendorInfoByStyle(String style) {
        String vendorInfo = "Unknown Vendor";
        String query = """
                SELECT v.FIRST_NAME, v.MIDDLE_NAME, v.LAST_NAME, vm.MOBILE_NO 
                FROM product p
                JOIN vendor v ON p.VENDOR_ID = v.VENDOR_ID
                JOIN vendor_mobile vm ON v.VENDOR_ID = vm.VENDOR_ID
                WHERE p.STYLE = ? LIMIT 1
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, style);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("FIRST_NAME") + " " +
                        rs.getString("MIDDLE_NAME") + " " +
                        rs.getString("LAST_NAME");
                String mobile = rs.getString("MOBILE_NO");
                vendorInfo = name + " (📱 " + mobile + ")";
            }
        } catch (SQLException e) {
            System.err.println("Error fetching vendor info for style: " + style);
            e.printStackTrace();
        }

        return vendorInfo;
    }

  
     private void confirmOrder() {
    int paymentMethodId = paymentMethodCombo.getSelectedIndex() + 1;

    // ✅ Removed CUSTOMER_ID from payment (not needed in payment table)
    String insertPayment = "INSERT INTO payment (Status, Amount, PaymentDate, PaymentMethodID, CustomerID) VALUES (?, ?, CURDATE(), ?, ?)";

    String getPaymentId = "SELECT PaymentID FROM payment ORDER BY PaymentID DESC LIMIT 1";

    String getProductId = "SELECT PRODUCT_ID FROM product WHERE STYLE = ?";
    String insertOrder = "INSERT INTO orders (PRODUCT_ID, QUANTITY, CUSTOMER_ID, PAYMENT_ID) VALUES (?, ?, ?, ?)";

    try (Connection conn = DBUtil.getConnection()) {
        conn.setAutoCommit(false);

        // ✅ Insert payment record
        try (PreparedStatement ps = conn.prepareStatement(insertPayment)) {
            ps.setString(1, "Completed");
            ps.setDouble(2, totalAmount);
            ps.setInt(3, paymentMethodId);
            ps.setInt(4, customerId);  // ✅ add this line
            ps.executeUpdate();

        }

        // ✅ Retrieve the latest PaymentID
        int paymentId;
        try (PreparedStatement ps = conn.prepareStatement(getPaymentId)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                paymentId = rs.getInt("PaymentID");
            } else {
                throw new SQLException("Payment ID retrieval failed.");
            }
        }

        // ✅ Insert order details (linked to customer and payment)
        try (PreparedStatement prodStmt = conn.prepareStatement(getProductId);
             PreparedStatement orderStmt = conn.prepareStatement(insertOrder)) {

            for (String style : cart.keySet()) {
                int quantity = cart.get(style);
                prodStmt.setString(1, style);
                ResultSet rs = prodStmt.executeQuery();
                if (rs.next()) {
                    int productId = rs.getInt("PRODUCT_ID");
                    orderStmt.setInt(1, productId);
                    orderStmt.setInt(2, quantity);
                    orderStmt.setInt(3, customerId);
                    orderStmt.setInt(4, paymentId);
                    orderStmt.executeUpdate();
                } else {
                    throw new SQLException("Product not found for style: " + style);
                }
            }
        }

        // ✅ Call stored procedure for shopping details (with customerId)
        callAddShoppingAfterPayment(conn, paymentId);
        conn.commit();

        JOptionPane.showMessageDialog(this, "✅ Order confirmed and payment recorded successfully!");
        dispose();

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "❌ Database error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void callAddShoppingAfterPayment(Connection conn, int paymentId) throws SQLException {
    String procedure = "{CALL AddShoppingAfterPayment(?, ?, ?, ?, ?)}"; // 5 params
    try (CallableStatement call = conn.prepareCall(procedure)) {
        call.setInt(1, paymentId);
        call.setInt(2, customerId); // ✅ add this
        call.setString(3, customerCity);
        call.setString(4, customerState);
        call.setString(5, customerCountry);
        call.executeUpdate();
    }
}



    private void printFullFrameAsPDF() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Order Summary");

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2d.scale(0.8, 0.8);
            getContentPane().printAll(g2d);
            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Print failed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

   
    public static void main(String[] args) {
        HashMap<String, Integer> sampleCart = new HashMap<>();
        sampleCart.put("Casual", 2);
        sampleCart.put("Formal", 1);
        SwingUtilities.invokeLater(() -> new OrderSummaryFrame(sampleCart, 1));
    }
}


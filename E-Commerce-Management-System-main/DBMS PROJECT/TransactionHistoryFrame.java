import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class TransactionHistoryFrame extends JFrame {

    public TransactionHistoryFrame(int customerId) {
        setTitle("Transaction History");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // --- Create main panel ---
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Your Transaction History", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        // --- Table setup ---
        String[] columns = {"Payment ID", "Status", "Amount", "Payment Date", "Payment Method"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(200, 220, 255));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(230, 230, 250));

        // --- Load transaction data ---
        loadTransactionData(customerId, tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // --- Close Button ---
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private void loadTransactionData(int customerId, DefaultTableModel tableModel) {
        String query = """
            SELECT p.PaymentID, p.Status, p.Amount, p.PaymentDate, pm.MethodName
            FROM payment p
            JOIN paymentmethods pm ON p.PaymentMethodID = pm.PaymentMethodID
            WHERE p.CustomerID = ?

            ORDER BY p.PaymentDate DESC
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            boolean hasResults = false;
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("PaymentID"),
                        rs.getString("Status"),
                        rs.getDouble("Amount"),
                        rs.getDate("PaymentDate"),
                        rs.getString("MethodName")
                };
                tableModel.addRow(row);
                hasResults = true;
            }

            if (!hasResults) {
                JOptionPane.showMessageDialog(this,
                        "No transaction history found for this customer.",
                        "No Records", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading transaction history:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


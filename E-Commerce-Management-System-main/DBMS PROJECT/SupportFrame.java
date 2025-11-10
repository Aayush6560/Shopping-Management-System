import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SupportFrame extends JFrame {

    private final int customerId;
    private JTextArea queryTextArea;
    private JTextField searchField;
    private JTextArea resultArea;

    public SupportFrame(int customerId) {
        this.customerId = customerId;
        initComponents();
    }

    private void initComponents() {
        setTitle("Customer Support");
        setSize(650, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel("Customer Support Center", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(heading, BorderLayout.NORTH);

        // --- Query Submission Section ---
        queryTextArea = new JTextArea(5, 40);
        queryTextArea.setLineWrap(true);
        queryTextArea.setWrapStyleWord(true);
        JScrollPane queryScroll = new JScrollPane(queryTextArea);

        JButton submitButton = new JButton("Submit Query");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.addActionListener(e -> submitQuery());

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Submit a Support Query"));
        inputPanel.add(queryScroll, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.SOUTH);

        // --- Search Section ---
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Query by ID"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(new JLabel("Support ID:"), gbc);

        gbc.gridx = 1;
        searchField = new JTextField(15);
        searchPanel.add(searchField, gbc);

        gbc.gridx = 2;
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchQuery());
        searchPanel.add(searchButton, gbc);

        // --- View All Queries Section ---
        JButton viewAllButton = new JButton("View My Queries");
        viewAllButton.addActionListener(e -> viewMyQueries());
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        searchPanel.add(viewAllButton, gbc);

        // --- Result Area ---
        resultArea = new JTextArea(12, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane resultScroll = new JScrollPane(resultArea);

        // --- Center Panel Layout ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(inputPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(searchPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(resultScroll);

        panel.add(centerPanel, BorderLayout.CENTER);
        setContentPane(panel);
        pack();
    }

    // --- Submit Query ---
    private void submitQuery() {
        String queryText = queryTextArea.getText().trim();
        if (queryText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your query before submitting.", "Missing Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO support (CUSTOMER_ID, QUERY_STATUS, DESCRIPTION) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, customerId);
            stmt.setString(2, "Open"); // default status
            stmt.setString(3, queryText);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int supportId = generatedKeys.getInt(1);
                    JOptionPane.showMessageDialog(this,
                            "Support query submitted successfully.\nYour Support ID is: " + supportId,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                queryTextArea.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit query.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Search Specific Query ---
    private void searchQuery() {
        String input = searchField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Support ID to search.", "Missing Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int supportId;
        try {
            supportId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Support ID must be a number.", "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "SELECT * FROM support WHERE SUPPORT_ID = ? AND CUSTOMER_ID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supportId);
            stmt.setInt(2, customerId);

            ResultSet rs = stmt.executeQuery();
            StringBuilder results = new StringBuilder();

            if (rs.next()) {
                results.append("Support ID: ").append(rs.getInt("SUPPORT_ID")).append("\n")
                        .append("Status     : ").append(rs.getString("QUERY_STATUS")).append("\n")
                        .append("Description: ").append(rs.getString("DESCRIPTION")).append("\n")
                        .append("-----------------------------\n");
            } else {
                results.append("No support query found for this ID under your account.");
            }

            resultArea.setText(results.toString());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- View All Queries by Customer ---
    private void viewMyQueries() {
        String sql = "SELECT SUPPORT_ID, QUERY_STATUS, DESCRIPTION FROM support WHERE CUSTOMER_ID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("=== Your Support Queries ===\n");
            while (rs.next()) {
                sb.append("Support ID: ").append(rs.getInt("SUPPORT_ID")).append("\n")
                        .append("Status     : ").append(rs.getString("QUERY_STATUS")).append("\n")
                        .append("Description: ").append(rs.getString("DESCRIPTION")).append("\n")
                        .append("-----------------------------\n");
            }

            resultArea.setText(sb.toString());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

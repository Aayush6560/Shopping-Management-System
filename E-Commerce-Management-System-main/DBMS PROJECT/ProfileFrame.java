import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * ProfileFrame - Displays and manages a user's profile information.
 * Allows editing of email, address, and mobile numbers.
 */
public class ProfileFrame extends JFrame {
    private final int customerId;

    public ProfileFrame(int customerId) {
        this.customerId = customerId;
        initProfileFrame();
    }

    private void initProfileFrame() {
        setTitle("My Profile");
        setSize(500, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Header section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel topRightIconLabel = new JLabel(new ImageIcon(
                new ImageIcon("images/user.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
        headerPanel.add(topRightIconLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));

        // Load user details
        loadCustomerDetails(profilePanel);

        add(new JScrollPane(profilePanel), BorderLayout.CENTER);
        setVisible(true);
    }

    /** Loads all profile sections */
    private void loadCustomerDetails(JPanel profilePanel) {
        try (Connection conn = DBUtil.getConnection()) {

            // Fetch user info
            String userQuery = "SELECT CUSTOMER_ID, FIRST_NAME, LAST_NAME, EMAIL, LOYALTY_POINTS, CITY, STATE, COUNTRY " +
                               "FROM customer WHERE CUSTOMER_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
                stmt.setInt(1, customerId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Customer not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    dispose();
                    return;
                }

                // Profile photo + details
                JPanel userInfoPanel = new JPanel(new BorderLayout(10, 10));
                userInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JLabel profileImage = new JLabel(new ImageIcon(
                        new ImageIcon("images/profile.png").getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
                userInfoPanel.add(profileImage, BorderLayout.WEST);

                JPanel userDetailsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                String fullName = rs.getString("FIRST_NAME") + " " + rs.getString("LAST_NAME");
                String email = rs.getString("EMAIL");
                int loyaltyPoints = rs.getInt("LOYALTY_POINTS");

                JLabel nameLabel = new JLabel("Name: " + fullName);
                nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
                JLabel pointsLabel = new JLabel("Loyalty Points: " + loyaltyPoints);
                JLabel emailLabel = new JLabel("Email: " + email);

                // Edit email
                JLabel editEmailIcon = new JLabel(new ImageIcon(
                        new ImageIcon("images/edit.png").getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
                editEmailIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
                editEmailIcon.setToolTipText("Edit Email");
                editEmailIcon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        String newEmail = JOptionPane.showInputDialog(ProfileFrame.this, "Enter new email:", email);
                        if (newEmail != null && !newEmail.trim().isEmpty()) {
                            updateCustomerEmail(newEmail);
                            emailLabel.setText("Email: " + newEmail);
                        }
                    }
                });

                JPanel emailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                emailPanel.add(emailLabel);
                emailPanel.add(editEmailIcon);

                userDetailsPanel.add(nameLabel);
                userDetailsPanel.add(pointsLabel);
                userDetailsPanel.add(emailPanel);
                userInfoPanel.add(userDetailsPanel, BorderLayout.CENTER);
                profilePanel.add(userInfoPanel);

                // Contact numbers
                loadMobileNumbers(profilePanel);

                // Address section
                loadAddressSection(profilePanel, rs.getString("CITY"), rs.getString("STATE"), rs.getString("COUNTRY"));

                // Transaction and shopping history
                JButton transactionButton = new JButton("View Transaction History");
                transactionButton.addActionListener(e -> new TransactionHistoryFrame(customerId));
                profilePanel.add(transactionButton);

                profilePanel.add(new ShoppingHistoryPanel(customerId));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading profile: " + e.getMessage());
        }
    }

    /** Loads customer mobile numbers */
    private void loadMobileNumbers(JPanel profilePanel) {
        JPanel mobilePanel = new JPanel(new BorderLayout());
        mobilePanel.setBorder(BorderFactory.createTitledBorder("Contact Numbers"));

        JPanel numbersPanel = new JPanel();
        numbersPanel.setLayout(new BoxLayout(numbersPanel, BoxLayout.Y_AXIS));

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT MOBILE_NO FROM customer_mobile WHERE CUSTOMER_ID = ?")) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String mobileNo = rs.getString("MOBILE_NO");
                numbersPanel.add(createMobileNumberRow(numbersPanel, mobileNo));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        JButton addNewNumberBtn = new JButton("Add New Number");
        addNewNumberBtn.addActionListener(e -> {
            String newNumber = JOptionPane.showInputDialog(ProfileFrame.this, "Enter new mobile number:");
            if (newNumber != null && !newNumber.trim().isEmpty()) {
                addMobileNumber(newNumber);
                numbersPanel.add(createMobileNumberRow(numbersPanel, newNumber));
                numbersPanel.revalidate();
                numbersPanel.repaint();
            }
        });

        mobilePanel.add(new JScrollPane(numbersPanel), BorderLayout.CENTER);
        mobilePanel.add(addNewNumberBtn, BorderLayout.SOUTH);
        profilePanel.add(mobilePanel);
    }

    /** Creates a single mobile number row with edit and delete options */
    private JPanel createMobileNumberRow(JPanel parentPanel, String mobileNo) {
        JPanel numberRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel mobileLabel = new JLabel(mobileNo);

        JLabel editIcon = new JLabel(new ImageIcon(
                new ImageIcon("images/edit.png").getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        JLabel deleteIcon = new JLabel(new ImageIcon(
                new ImageIcon("images/delete.png").getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));

        editIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        editIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String newNumber = JOptionPane.showInputDialog(ProfileFrame.this, "Enter new number:", mobileNo);
                if (newNumber != null && !newNumber.trim().isEmpty()) {
                    updateMobileNumber(mobileNo, newNumber);
                    mobileLabel.setText(newNumber);
                }
            }
        });

        deleteIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int confirm = JOptionPane.showConfirmDialog(ProfileFrame.this,
                        "Delete this number?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteMobileNumber(mobileNo);
                    parentPanel.remove(numberRow);
                    parentPanel.revalidate();
                    parentPanel.repaint();
                }
            }
        });

        numberRow.add(mobileLabel);
        numberRow.add(editIcon);
        numberRow.add(deleteIcon);
        return numberRow;
    }

    /** Loads editable address section */
    private void loadAddressSection(JPanel profilePanel, String city, String state, String country) {
        JPanel addressPanel = new JPanel(new BorderLayout());
        addressPanel.setBorder(BorderFactory.createTitledBorder("Address"));

        JPanel addressFields = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField cityField = new JTextField(city);
        JTextField stateField = new JTextField(state);
        JTextField countryField = new JTextField(country);

        addressFields.add(new JLabel("City:"));
        addressFields.add(cityField);
        addressFields.add(new JLabel("State:"));
        addressFields.add(stateField);
        addressFields.add(new JLabel("Country:"));
        addressFields.add(countryField);

        JButton updateAddressBtn = new JButton("Update Address");
        updateAddressBtn.addActionListener(e -> {
            updateCustomerAddress(cityField.getText(), stateField.getText(), countryField.getText());
            JOptionPane.showMessageDialog(this, "Address updated successfully!");
        });

        addressPanel.add(addressFields, BorderLayout.CENTER);
        addressPanel.add(updateAddressBtn, BorderLayout.SOUTH);
        profilePanel.add(addressPanel);
    }

    // === DATABASE ACTIONS ===
    private void updateCustomerEmail(String newEmail) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE customer SET EMAIL = ? WHERE CUSTOMER_ID = ?")) {
            stmt.setString(1, newEmail);
            stmt.setInt(2, customerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating email: " + e.getMessage());
        }
    }

    private void updateMobileNumber(String oldNumber, String newNumber) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE customer_mobile SET MOBILE_NO = ? WHERE CUSTOMER_ID = ? AND MOBILE_NO = ?")) {
            stmt.setString(1, newNumber);
            stmt.setInt(2, customerId);
            stmt.setString(3, oldNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating number: " + e.getMessage());
        }
    }

    private void deleteMobileNumber(String number) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM customer_mobile WHERE CUSTOMER_ID = ? AND MOBILE_NO = ?")) {
            stmt.setInt(1, customerId);
            stmt.setString(2, number);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting number: " + e.getMessage());
        }
    }

    private void addMobileNumber(String number) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO customer_mobile (CUSTOMER_ID, MOBILE_NO) VALUES (?, ?)")) {
            stmt.setInt(1, customerId);
            stmt.setString(2, number);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding number: " + e.getMessage());
        }
    }

    private void updateCustomerAddress(String city, String state, String country) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE customer SET CITY = ?, STATE = ?, COUNTRY = ? WHERE CUSTOMER_ID = ?")) {
            stmt.setString(1, city);
            stmt.setString(2, state);
            stmt.setString(3, country);
            stmt.setInt(4, customerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating address: " + e.getMessage());
        }
    }
}


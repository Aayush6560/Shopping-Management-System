import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {

    private static final String AUTH_QUERY = "SELECT * FROM security_confi WHERE email = ? AND pass = ?";
    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginForm() {
        setTitle("🛒 Login");
        setSize(450, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center window
        setLayout(null);
        getContentPane().setBackground(new Color(245, 245, 245)); // light background

        // =======================
        // Logo / Header
        // =======================
        JLabel header = new JLabel("WELCOME", SwingConstants.CENTER);
        header.setBounds(50, 20, 350, 50);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(new Color(50, 50, 200));
        add(header);

        JLabel loginLabel = new JLabel("Login to your account", SwingConstants.CENTER);
        loginLabel.setBounds(50, 70, 350, 30);
        loginLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        loginLabel.setForeground(Color.DARK_GRAY);
        add(loginLabel);

        // =======================
        // Input Fields
        // =======================
        emailField = new JTextField();
        emailField.setBounds(50, 130, 350, 35);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        emailField.setBorder(BorderFactory.createTitledBorder("Email"));
        add(emailField);

        passwordField = new JPasswordField();
        passwordField.setBounds(50, 190, 350, 35);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));
        add(passwordField);

        // =======================
        // Buttons
        // =======================
        JButton loginButton = new JButton("Login");
        loginButton.setBounds(50, 260, 350, 40);
        loginButton.setBackground(new Color(50, 150, 250));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(50, 320, 350, 35);
        registerButton.setBackground(new Color(100, 200, 150));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setFocusPainted(false);
        add(registerButton);

        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(50, 370, 350, 35);
        exitButton.setBackground(new Color(200, 50, 50));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitButton.setFocusPainted(false);
        add(exitButton);

        // ENTER key triggers login
        getRootPane().setDefaultButton(loginButton);

        // =======================
        // Button Actions
        // =======================
        loginButton.addActionListener(e -> handleLogin());

        exitButton.addActionListener(e -> System.exit(0));

        registerButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Registration feature coming soon!",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        setVisible(true);
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both Email and Password.",
                    "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(AUTH_QUERY)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int customerId = rs.getInt("customer_id");
                JOptionPane.showMessageDialog(this, "✅ Login Successful! Welcome!");
                dispose(); // close login window
                new DashboardFrame(customerId); // open dashboard with button
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Invalid Email or Password. Try again.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Database Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginForm::new);
    }
}

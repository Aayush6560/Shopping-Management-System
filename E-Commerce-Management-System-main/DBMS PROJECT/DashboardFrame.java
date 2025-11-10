import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {

    private int customerId;

    public DashboardFrame(int customerId) {
        this.customerId = customerId;

        setTitle("🛒 Dashboard");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));

        JLabel welcomeLabel = new JLabel("Welcome, User ID: " + customerId);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(welcomeLabel);

        JButton productBtn = new JButton("Manage Products");
        productBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        productBtn.setBackground(new Color(50, 150, 250));
        productBtn.setForeground(Color.WHITE);
        productBtn.setFocusPainted(false);
        productBtn.addActionListener(e -> new ProductManagerFrame());

        add(productBtn);

        setVisible(true);
    }
}

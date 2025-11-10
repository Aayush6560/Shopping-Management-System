import javax.swing.*;
import java.awt.*;

public class RpIdleSection extends JFrame {

    public RpIdleSection() {
        setTitle("Rp Idle Section");
        setSize(450, 300);
        setLocationRelativeTo(null); // Center window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header section
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(60, 130, 200));
        headerPanel.setPreferredSize(new Dimension(450, 60));

        JLabel headerLabel = new JLabel("RP Idle Section");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Center content
        JLabel contentLabel = new JLabel("Welcome to RpIdleSection", SwingConstants.CENTER);
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        contentLabel.setForeground(Color.DARK_GRAY);

        // Footer (optional)
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(Color.LIGHT_GRAY);
        footerPanel.add(new JLabel("Status: Idle | Ready"));

        add(headerPanel, BorderLayout.NORTH);
        add(contentLabel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    // For standalone testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RpIdleSection().setVisible(true));
    }
}

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

class CustomerDisplay extends JPanel {
    private MainGUI mainGUI;
    private JLabel userLabel;
    
    public CustomerDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        setLayout(new BorderLayout());
        
        // Header with user info
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel header = new JLabel("Customer Dashboard", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // User info panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userLabel = new JLabel("Welcome, " + mainGUI.getCurrentUsername());
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Logout button panel
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        
        userPanel.add(userLabel);
        logoutPanel.add(logoutButton);
        
        headerPanel.add(userPanel, BorderLayout.WEST);
        headerPanel.add(header, BorderLayout.CENTER);
        headerPanel.add(logoutPanel, BorderLayout.EAST);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        String[] buttons = {
            "View Screenings", "My Tickets", 
            "Book Tickets", "Logout"
        };
        
        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 16));
            button.setPreferredSize(new Dimension(200, 80));
            button.addActionListener(new MenuButtonListener());
            buttonPanel.add(button);
        }
        
        logoutButton.addActionListener(e -> mainGUI.logout());
        
        add(headerPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }
    
    public void refreshUserInfo() {
        if (userLabel != null) {
            userLabel.setText("Welcome, " + mainGUI.getCurrentUsername());
        }
    }
    
    private class MenuButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = ((JButton) e.getSource()).getText();
            switch (command) {
                case "View Screenings":
                    mainGUI.showScreen("SCREENING_MANAGEMENT");
                    break;
                case "My Tickets":
                case "Book Tickets":
                    mainGUI.showScreen("TICKET_MANAGEMENT");
                    break;
                case "Logout":
                    mainGUI.logout();
                    break;
                default:
                    JOptionPane.showMessageDialog(CustomerDisplay.this, 
                        command + " functionality coming soon!", 
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
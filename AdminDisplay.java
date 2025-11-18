import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

class AdminDisplay extends JPanel {
    private MainGUI mainGUI;
    
    public AdminDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        setLayout(new BorderLayout());
        
        // Header
        JLabel header = new JLabel("Admin Dashboard", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        String[] buttons = {
            "Manage Movies", "Manage Venues", "Manage Rooms",
            "Manage Seats", "View Bookings", "Reports"
        };
        
        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 16));
            button.setPreferredSize(new Dimension(200, 80));
            button.addActionListener(new MenuButtonListener());
            buttonPanel.add(button);
        }
        
        add(header, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }
    
    private class MenuButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = ((JButton) e.getSource()).getText();
            switch (command) {
                case "Manage Movies":
                    mainGUI.showScreen("MOVIE_MANAGEMENT");
                    break;
                case "Manage Venues":
                    mainGUI.showScreen("VENUE_MANAGEMENT");
                    break;
                // Add other cases...
            }
        }
    }
}
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

class CustomerDisplay extends JPanel {
    private MainGUI mainGUI;
    
    public CustomerDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        setLayout(new BorderLayout());
        
        // Header
        JLabel header = new JLabel("Customer Dashboard", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        String[] buttons = {
            "Ticket Bookings", "View Venues", 
            "View Screenings", "View Movies"
        };
        
        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 16));
            button.addActionListener(new CustomerButtonListener());
            buttonPanel.add(button);
        }
        
        // Logout button
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> mainGUI.showScreen("LOGIN"));
        logoutPanel.add(logoutButton);
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(header, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(logoutPanel, BorderLayout.SOUTH);
    }
    
    private class CustomerButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = ((JButton) e.getSource()).getText();
            JOptionPane.showMessageDialog(CustomerDisplay.this, 
                command + " clicked - This would open the " + command + " screen");
            
            // You can add screen navigation here later
            // switch(command) {
            //     case "Ticket Bookings":
            //         mainGUI.showScreen("BOOKING");
            //         break;
            //     // etc...
            // }
        }
    }
}
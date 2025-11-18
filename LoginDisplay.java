import java.awt.*;
import javax.swing.*;

class LoginDisplay extends JPanel {
    private MainGUI mainGUI;
    private CustomersDao customersDao;
    
    public LoginDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        this.customersDao = new CustomersDao();
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel(""));
        formPanel.add(loginButton);
        
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            
            // Authenticate user
            Customers loggedInUser = customersDao.getCustomerByUsername(username);
            
            if (loggedInUser != null && loggedInUser.getPassword().equals(password)) {
                // Determine which dashboard to show based on account type
                if (loggedInUser.getAccountType().equals("Admin")) {
                    mainGUI.showScreen("ADMIN PANEL");
                } else {
                    // Create customer panel with user info
                    mainGUI.showScreen("CUSTOMER PANEL");
                }
                JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + loggedInUser.getName());
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!", 
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        add(formPanel, BorderLayout.CENTER);
    }
}
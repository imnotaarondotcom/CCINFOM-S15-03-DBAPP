import java.awt.*;
import javax.swing.*;

class LoginDisplay extends JPanel {
    private MainGUI mainGUI;
    private CustomersDao customersDao;
    
    public LoginDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        this.customersDao = new CustomersDao();
        setLayout(new BorderLayout());
        
        // Header
        JLabel header = new JLabel("Movie Ticket Management System", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField usernameField = new JTextField(20);
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JPasswordField passwordField = new JPasswordField(20);
        
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));
        
        // Layout components
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(loginButton, gbc);
        
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password!", 
                    "Login Failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Authenticate user
            Customers loggedInUser = customersDao.getCustomerByUsername(username);
            
            if (loggedInUser != null && loggedInUser.getPassword().equals(password)) {
                mainGUI.loginSuccess(loggedInUser);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!", 
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
                // Clear fields
                passwordField.setText("");
                usernameField.requestFocus();
            }
        });
        
        // Add enter key listener for convenience
        passwordField.addActionListener(e -> loginButton.doClick());
        
        add(header, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }
}
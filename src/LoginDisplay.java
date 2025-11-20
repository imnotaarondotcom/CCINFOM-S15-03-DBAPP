import java.awt.*;
import javax.swing.*;

class LoginDisplay extends JPanel {
    private MainGUI mainGUI;
    private CustomersDao customersDao;
    
    public LoginDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        this.customersDao = new CustomersDao();
        setLayout(new BorderLayout());
        
        JLabel header = new JLabel("Movie Ticket Management System", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        
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
        JTextField passwordField = new JTextField(20);
        
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));

        JButton createAccountButton = new JButton("Create Account");
        createAccountButton.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridy = 3;
        formPanel.add(createAccountButton, gbc);
        createAccountButton.addActionListener(e -> createAccdisplay());
        
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
            String password = passwordField.getText().trim();
            
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
                passwordField.setText("");
                usernameField.requestFocus();
            }
        });
        
        passwordField.addActionListener(e -> loginButton.doClick());
        
        add(header, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }
    
    private void createAccdisplay() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Customer Account", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JTextField phoneField = new JTextField();
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JPasswordField();

        dialog.add(new JLabel("Phone Number: "));
        dialog.add(phoneField);
        dialog.add(new JLabel("Username: "));
        dialog.add(usernameField);
        dialog.add(new JLabel("Password: "));
        dialog.add(passwordField);

        JButton saveButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");

        dialog.add(saveButton);
        dialog.add(cancelButton);

        saveButton.addActionListener(e -> {
            String phone = phoneField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            CustomersDao dao = new CustomersDao();
            if (dao.getCustomerByUsername(username) != null) {
                JOptionPane.showMessageDialog(dialog, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Customers newCustomer = new Customers(0, phone, username, "Customer", password);
            boolean success = dao.addCustomer(newCustomer);

            if (success) {
                JOptionPane.showMessageDialog(dialog, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create account.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
}

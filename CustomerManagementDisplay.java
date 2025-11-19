import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Scanner;

public class CustomerManagementDisplay extends JPanel {
    private MainGUI mainGUI;
    private CustomersManagement customersManagement;
    private CustomersDao customersDao;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    
    public CustomerManagementDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        this.customersManagement = new CustomersManagement(new Scanner(System.in));
        this.customersDao = new CustomersDao();
        setLayout(new BorderLayout());
        initializeComponents();
        loadCustomerData();
    }
    
    private void initializeComponents() {
        // Header
        JLabel header = new JLabel("Customer Management", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Table for displaying customers
        String[] columnNames = {"ID", "Phone Number", "Username", "Account Type", "Password"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        customerTable = new JTable(tableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        customerTable.setFont(new Font("Arial", Font.PLAIN, 12));
        customerTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Customers List"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton addButton = new JButton("Add Customer");
        JButton editButton = new JButton("Edit Customer");
        JButton deleteButton = new JButton("Delete Customer");
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back to Admin Panel");
        
        // Style buttons
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        refreshButton.setFont(buttonFont);
        backButton.setFont(buttonFont);
        
        // Add action listeners
        addButton.addActionListener(e -> addCustomer());
        editButton.addActionListener(e -> editCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        refreshButton.addActionListener(e -> loadCustomerData());
        backButton.addActionListener(e -> mainGUI.showScreen("ADMIN PANEL"));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        
        // Add components to panel
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadCustomerData() {
        tableModel.setRowCount(0); // Clear existing data
        
        ArrayList<Customers> customers = customersDao.getAllCustomers();
        for (Customers customer : customers) {
            tableModel.addRow(new Object[]{
                customer.getID(),
                customer.getNumber(),
                customer.getName(),
                customer.getAccountType(),
                "••••••••" // Hide actual password
            });
        }
    }
    
    private void addCustomer() {
        JDialog addDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Customer", true);
        addDialog.setLayout(new GridLayout(6, 2, 10, 10));
        addDialog.setSize(400, 300);
        addDialog.setLocationRelativeTo(this);
        
        JTextField phoneField = new JTextField();
        JTextField usernameField = new JTextField();
        JComboBox<String> accountTypeCombo = new JComboBox<>(new String[]{"Admin", "Customer"});
        JPasswordField passwordField = new JPasswordField();
        
        addDialog.add(new JLabel("Phone Number:"));
        addDialog.add(phoneField);
        addDialog.add(new JLabel("Username:"));
        addDialog.add(usernameField);
        addDialog.add(new JLabel("Account Type:"));
        addDialog.add(accountTypeCombo);
        addDialog.add(new JLabel("Password:"));
        addDialog.add(passwordField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            if (validateInput(phoneField.getText(), usernameField.getText(), new String(passwordField.getPassword()))) {
                Customers newCustomer = new Customers(
                    0, // ID will be auto-generated
                    phoneField.getText(),
                    usernameField.getText(),
                    (String) accountTypeCombo.getSelectedItem(),
                    new String(passwordField.getPassword())
                );
                
                boolean added = customersDao.addCustomer(newCustomer);
                if (added) {
                    JOptionPane.showMessageDialog(addDialog, "Customer added successfully!");
                    loadCustomerData();
                    addDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(addDialog, "Error adding customer!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> addDialog.dispose());
        
        addDialog.add(saveButton);
        addDialog.add(cancelButton);
        
        addDialog.setVisible(true);
    }
    
    private void editCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        Customers customer = customersDao.getCustomerById(customerId);
        
        if (customer == null) {
            JOptionPane.showMessageDialog(this, "Customer not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Customer", true);
        editDialog.setLayout(new GridLayout(6, 2, 10, 10));
        editDialog.setSize(400, 350);
        editDialog.setLocationRelativeTo(this);
        
        JTextField phoneField = new JTextField(customer.getNumber());
        JTextField usernameField = new JTextField(customer.getName());
        JComboBox<String> accountTypeCombo = new JComboBox<>(new String[]{"Admin", "Customer"});
        accountTypeCombo.setSelectedItem(customer.getAccountType());
        JPasswordField passwordField = new JPasswordField();
        
        editDialog.add(new JLabel("Phone Number:"));
        editDialog.add(phoneField);
        editDialog.add(new JLabel("Username:"));
        editDialog.add(usernameField);
        editDialog.add(new JLabel("Account Type:"));
        editDialog.add(accountTypeCombo);
        editDialog.add(new JLabel("New Password (optional):"));
        editDialog.add(passwordField);
        editDialog.add(new JLabel("")); // empty cell for layout
        editDialog.add(new JLabel("Leave blank to keep current password"));
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            // Use a different validation method for editing that doesn't require password
            if (validateEditInput(phoneField.getText(), usernameField.getText())) {
                customer.setNumber(phoneField.getText());
                customer.setName(usernameField.getText());
                customer.setAccountType((String) accountTypeCombo.getSelectedItem());
                
                String newPassword = new String(passwordField.getPassword());
                if (!newPassword.isEmpty()) {
                    customer.setPassword(newPassword);
                }
                
                boolean updated = customersDao.updateCustomer(customer);
                if (updated) {
                    JOptionPane.showMessageDialog(editDialog, "Customer updated successfully!");
                    loadCustomerData();
                    editDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Error updating customer!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> editDialog.dispose());
        
        editDialog.add(saveButton);
        editDialog.add(cancelButton);
        
        editDialog.setVisible(true);
    }

    // Separate validation method for editing that doesn't require password
    private boolean validateEditInput(String phone, String username) {
        if (phone.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone number cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to delete customer: " + username + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = customersDao.deleteCustomer(customerId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
                loadCustomerData();
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting customer!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean validateInput(String phone, String username, String password) {
        if (phone.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone number cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
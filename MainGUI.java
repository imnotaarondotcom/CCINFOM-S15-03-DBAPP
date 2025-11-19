import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainGUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private String currentUserType = "";
    private int currentUserId = -1;
    private String currentUsername = "";
    
    public MainGUI() {
        setTitle("Movie Ticket Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Add login screen only initially
        mainPanel.add(new LoginDisplay(this), "LOGIN");
        
        // Admin and Customer displays will be created dynamically after login
        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }
    
    public void showScreen(String screenName) {
        cardLayout.show(mainPanel, screenName);
    }
    
    public void loginSuccess(Customers user) {
        this.currentUserId = user.getID();
        this.currentUserType = user.getAccountType();
        this.currentUsername = user.getName();
        
        // Remove existing admin/customer panels if they exist
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            String name = ((JPanel)comp).getName();
            if (name != null && (name.equals("ADMIN PANEL") || name.equals("CUSTOMER PANEL"))) {
                mainPanel.remove(comp);
            }
        }
        
        // Create and add the appropriate dashboard
        if ("Admin".equals(currentUserType)) {
            mainPanel.add(new AdminDisplay(this), "ADMIN PANEL");
        } else {
            mainPanel.add(new CustomerDisplay(this), "CUSTOMER PANEL");
        }
        
        // Create management screens with user context
        createManagementScreens();
        
        // Show appropriate dashboard
        if ("Admin".equals(currentUserType)) {
            showScreen("ADMIN PANEL");
        } else {
            showScreen("CUSTOMER PANEL");
        }
        
        JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + currentUsername);
    }
    
    private void createManagementScreens() {
        // Remove existing management screens if any
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            String name = ((JPanel)comp).getName();
            if (name != null && (name.equals("CUSTOMER_MANAGEMENT") || name.equals("MOVIE_MANAGEMENT") || 
                name.equals("VENUE_MANAGEMENT") || name.equals("ROOM_MANAGEMENT") || 
                name.equals("SEAT_MANAGEMENT") || name.equals("SCREENING_MANAGEMENT") || 
                name.equals("TICKET_MANAGEMENT") || name.equals("REPORTS"))) {
                mainPanel.remove(comp);
            }
        }
        
        // Add management screens with user context
        if ("Admin".equals(currentUserType)) {
            mainPanel.add(new CustomerManagementDisplay(this), "CUSTOMER_MANAGEMENT");
            mainPanel.add(new MovieManagementDisplay(this), "MOVIE_MANAGEMENT");
            mainPanel.add(new VenueManagementDisplay(this), "VENUE_MANAGEMENT");
            mainPanel.add(new RoomManagementDisplay(this), "ROOM_MANAGEMENT");
            mainPanel.add(new SeatManagementDisplay(this), "SEAT_MANAGEMENT");
            mainPanel.add(new ScreeningManagementDisplay(this, currentUserType), "SCREENING_MANAGEMENT");
            mainPanel.add(new TicketManagementDisplay(this, currentUserId, currentUserType), "TICKET_MANAGEMENT");
            mainPanel.add(new ReportsDisplay(this), "REPORTS");
        } else {
            // Customer only needs ticket management and screening view
            mainPanel.add(new TicketManagementDisplay(this, currentUserId, currentUserType), "TICKET_MANAGEMENT");
            mainPanel.add(new ScreeningManagementDisplay(this, currentUserType), "SCREENING_MANAGEMENT");
        }
    }
    
    // Getters for user information
    public int getCurrentUserId() {
        return currentUserId;
    }
    
    public String getCurrentUserType() {
        return currentUserType;
    }
    
    public String getCurrentUsername() {
        return currentUsername;
    }
    
    public void logout() {
        this.currentUserId = -1;
        this.currentUserType = "";
        this.currentUsername = "";
        
        // Remove all panels except login
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            String name = ((JPanel)comp).getName();
            if (name != null && !name.equals("LOGIN")) {
                mainPanel.remove(comp);
            }
        }
        
        showScreen("LOGIN");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainGUI().setVisible(true);
        });
    }
}
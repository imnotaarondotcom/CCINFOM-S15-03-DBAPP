import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ReportsDisplay extends JPanel {
    private MainGUI mainGUI;
    private JTextArea reportTextArea;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    
    // DAO instances
    private VenuesDao venuesDao;
    private CustomersDao customersDao;
    private MovieDao movieDao;
    private RoomDao roomDao;
    private ScreeningDao screeningDao;
    private TicketDao ticketDao;
    
    public ReportsDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        initializeDAOs();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initializeComponents();
    }
    
    private void initializeDAOs() {
        venuesDao = new VenuesDao();
        customersDao = new CustomersDao();
        movieDao = new MovieDao();
        roomDao = new RoomDao();
        screeningDao = new ScreeningDao();
        ticketDao = new TicketDao();
    }
    
    private void initializeComponents() {
        // Header
        JLabel header = new JLabel("Reports Dashboard", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 28));
        header.setForeground(new Color(0, 51, 102));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        // Report selection panel with buttons
        JPanel buttonPanel = createButtonPanel();
        
        // Report display area
        JPanel reportDisplayPanel = createReportDisplayPanel();
        
        // Add components to main panel
        add(header, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(reportDisplayPanel, BorderLayout.SOUTH);
        
        // Show initial welcome message
        showWelcomeMessage();
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 15, 15));
        panel.setBorder(BorderFactory.createTitledBorder("Select Report Type"));
        panel.setBackground(new Color(240, 245, 255));
        
        // Create report buttons
        JButton ticketRevenueBtn = createStyledButton("Ticket Revenue", "Daily/Weekly Revenue by Branch");
        JButton customerActivityBtn = createStyledButton("Customer Activity", "Weekly Customer Transactions");
        JButton moviePerformanceBtn = createStyledButton("Movie Performance", "Weekly/Monthly Movie Rankings");
        JButton venueUtilizationBtn = createStyledButton("Venue Utilization", "Monthly Seat Occupancy");
        JButton clearBtn = createStyledButton("Clear Report", "Clear Current Report");
        JButton backBtn = createStyledButton("Back to Admin", "Return to Admin Panel");
        
        // Add action listeners
        ticketRevenueBtn.addActionListener(e -> generateTicketRevenueReport());
        customerActivityBtn.addActionListener(e -> generateCustomerActivityReport());
        moviePerformanceBtn.addActionListener(e -> generateMoviePerformanceReport());
        venueUtilizationBtn.addActionListener(e -> generateVenueUtilizationReport());
        clearBtn.addActionListener(e -> clearReport());
        backBtn.addActionListener(e -> mainGUI.showScreen("ADMIN PANEL"));
        
        // Add buttons to panel
        panel.add(ticketRevenueBtn);
        panel.add(customerActivityBtn);
        panel.add(moviePerformanceBtn);
        panel.add(venueUtilizationBtn);
        panel.add(clearBtn);
        panel.add(backBtn);
        
        return panel;
    }
    
    private JButton createStyledButton(String title, String tooltip) {
        JButton button = new JButton("<html><center>" + title + "</center></html>");
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(150, 80));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
        
        return button;
    }
    
    private JPanel createReportDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Table for summary data
        String[] columnNames = {"Metric", "Value"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable = new JTable(tableModel);
        reportTable.setFont(new Font("Arial", Font.PLAIN, 12));
        reportTable.setRowHeight(25);
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        reportTable.setSelectionBackground(new Color(220, 240, 255));
        
        JScrollPane tableScrollPane = new JScrollPane(reportTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Report Summary"));
        tableScrollPane.setPreferredSize(new Dimension(400, 150));
        
        // Text area for detailed report
        reportTextArea = new JTextArea(20, 70);
        reportTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        reportTextArea.setEditable(false);
        reportTextArea.setBackground(new Color(248, 248, 248));
        reportTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JScrollPane textScrollPane = new JScrollPane(reportTextArea);
        textScrollPane.setBorder(BorderFactory.createTitledBorder("Detailed Report"));
        
        // Add both components to split pane for resizing
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, textScrollPane);
        splitPane.setResizeWeight(0.3);
        splitPane.setDividerLocation(150);
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }
    
    private void showWelcomeMessage() {
        clearReport();
        reportTextArea.setText("WELCOME TO REPORTS DASHBOARD\n");
        reportTextArea.append("=" .repeat(50) + "\n\n");
        reportTextArea.append("Select a report type from the buttons above to generate detailed analytics.\n\n");
        
        reportTextArea.append("AVAILABLE REPORTS:\n");
        reportTextArea.append("• Ticket Revenue Report - Shows total tickets sold and revenue for each branch (Daily/Weekly)\n");
        reportTextArea.append("• Customer Activity Report - Transaction frequency per customer (Weekly)\n");
        reportTextArea.append("• Movie Performance Report - Tracks ticket sales and attendance by movie (Weekly/Monthly)\n");
        reportTextArea.append("• Venue Utilization Report - Shows seat occupancy and room capacity usage (Monthly)\n\n");
        
        reportTextArea.append("TIP: Use the summary table for quick insights and the detailed area for comprehensive data.");
    }
    
    private void generateTicketRevenueReport() {
        clearReport();
        appendReportHeader("TICKET REVENUE REPORT", "Daily/Weekly Revenue by Branch");
        
        try {
            // Use RoomDao's existing method for revenue by room
            ArrayList<String> revenueData = roomDao.getRevenueByRoom();
            
            reportTextArea.append(String.format("%-25s %-15s %s\n", 
                "ROOM", "TICKETS SOLD", "TOTAL REVENUE"));
            reportTextArea.append("-".repeat(60) + "\n");
            
            double grandTotalRevenue = 0;
            int grandTotalTickets = 0;
            int venueCount = 0;
            
            // Parse the revenue data from RoomDao
            for (String revenueInfo : revenueData) {
                reportTextArea.append(revenueInfo + "\n");
                
                // Extract numbers from the formatted string for summary calculations
                String[] parts = revenueInfo.split("\\|");
                if (parts.length >= 3) {
                    try {
                        // Extract tickets sold
                        String ticketsPart = parts[1].trim();
                        int tickets = Integer.parseInt(ticketsPart.replaceAll("[^0-9]", ""));
                        
                        // Extract revenue
                        String revenuePart = parts[2].trim();
                        double revenue = Double.parseDouble(revenuePart.replaceAll("[^0-9.]", ""));
                        
                        grandTotalTickets += tickets;
                        grandTotalRevenue += revenue;
                        venueCount++;
                    } catch (NumberFormatException e) {
                        // Skip if parsing fails
                    }
                }
            }
            
            reportTextArea.append("-".repeat(60) + "\n");
            reportTextArea.append(String.format("GRAND TOTAL: %d tickets, $%.2f revenue\n", 
                grandTotalTickets, grandTotalRevenue));
            
            // Update summary table
            tableModel.addRow(new Object[]{"Total Venues/Rooms", venueCount});
            tableModel.addRow(new Object[]{"Total Tickets Sold", grandTotalTickets});
            tableModel.addRow(new Object[]{"Total Revenue", String.format("$%.2f", grandTotalRevenue)});
            tableModel.addRow(new Object[]{"Average Revenue per Room", 
                String.format("$%.2f", venueCount > 0 ? grandTotalRevenue / venueCount : 0)});
            
        } catch (Exception e) {
            showError("Error generating ticket revenue report: " + e.getMessage());
        }
    }
    
    private void generateCustomerActivityReport() {
        clearReport();
        appendReportHeader("CUSTOMER ACTIVITY REPORT", "Weekly Customer Transactions");
        
        try {
            // Get all customers and their activity using existing DAO methods
            ArrayList<Customers> allCustomers = customersDao.getAllCustomers();
            
            reportTextArea.append(String.format("%-20s %-12s %-15s %s\n", 
                "CUSTOMER", "ACCOUNT TYPE", "PHONE", "USERNAME"));
            reportTextArea.append("-".repeat(65) + "\n");
            
            int totalCustomers = 0;
            int adminCount = 0;
            int customerCount = 0;
            
            for (Customers customer : allCustomers) {
                reportTextArea.append(String.format("%-20s %-12s %-15s %s\n", 
                    customer.getName(), 
                    customer.getAccountType(),
                    customer.getNumber(),
                    customer.getName()));
                
                totalCustomers++;
                if ("Admin".equals(customer.getAccountType())) {
                    adminCount++;
                } else {
                    customerCount++;
                }
            }
            
            reportTextArea.append("-".repeat(65) + "\n");
            reportTextArea.append(String.format("TOTAL CUSTOMERS: %d (Admins: %d, Customers: %d)\n", 
                totalCustomers, adminCount, customerCount));
            
            // Update summary table
            tableModel.addRow(new Object[]{"Total Users", totalCustomers});
            tableModel.addRow(new Object[]{"Admin Accounts", adminCount});
            tableModel.addRow(new Object[]{"Customer Accounts", customerCount});
            tableModel.addRow(new Object[]{"Admin Percentage", 
                String.format("%.1f%%", totalCustomers > 0 ? (adminCount * 100.0 / totalCustomers) : 0)});
            
        } catch (Exception e) {
            showError("Error generating customer activity report: " + e.getMessage());
        }
    }
    
    private void generateMoviePerformanceReport() {
        clearReport();
        appendReportHeader("MOVIE PERFORMANCE REPORT", "Weekly/Monthly Movie Rankings");
        
        try {
            // Use MovieDao to get all movies
            ArrayList<Movie> allMovies = movieDao.getAllMovies();
            
            reportTextArea.append(String.format("%-25s %-12s %-8s %s\n", 
                "MOVIE", "GENRE", "RATING", "DURATION"));
            reportTextArea.append("-".repeat(60) + "\n");
            
            int totalMovies = 0;
            int totalDuration = 0;
            int pgCount = 0, pg13Count = 0, rCount = 0, gCount = 0;
            
            for (Movie movie : allMovies) {
                String movieName = movie.getMovieName();
                if (movieName.length() > 24) {
                    movieName = movieName.substring(0, 24) + "...";
                }
                
                reportTextArea.append(String.format("%-25s %-12s %-8s %d mins\n", 
                    movieName, 
                    movie.getGenre(),
                    movie.getAgeRating(),
                    movie.getDuration()));
                
                totalMovies++;
                totalDuration += movie.getDuration();
                
                // Count by rating
                switch (movie.getAgeRating()) {
                    case "PG": pgCount++; break;
                    case "PG-13": pg13Count++; break;
                    case "R": rCount++; break;
                    case "G": gCount++; break;
                }
            }
            
            reportTextArea.append("-".repeat(60) + "\n");
            reportTextArea.append(String.format("TOTAL MOVIES: %d | AVERAGE DURATION: %.1f mins\n", 
                totalMovies, totalMovies > 0 ? (double)totalDuration / totalMovies : 0));
            
            // Update summary table
            tableModel.addRow(new Object[]{"Total Movies", totalMovies});
            tableModel.addRow(new Object[]{"Average Duration", 
                String.format("%.1f mins", totalMovies > 0 ? (double)totalDuration / totalMovies : 0)});
            tableModel.addRow(new Object[]{"PG Movies", pgCount});
            tableModel.addRow(new Object[]{"PG-13 Movies", pg13Count});
            tableModel.addRow(new Object[]{"R Rated Movies", rCount});
            tableModel.addRow(new Object[]{"G Rated Movies", gCount});
            
        } catch (Exception e) {
            showError("Error generating movie performance report: " + e.getMessage());
        }
    }
    
    private void generateVenueUtilizationReport() {
        clearReport();
        appendReportHeader("VENUE UTILIZATION REPORT", "Monthly Seat Occupancy & Capacity Usage");
        
        try {
            // Use VenuesDao and RoomDao methods
            ArrayList<Venues> allVenues = venuesDao.getAllVenues();
            ArrayList<String> capacityData = roomDao.getVenueCapacityVsAttendance();
            ArrayList<String> screeningData = roomDao.getTotalScreeningsByVenue();
            
            reportTextArea.append(String.format("%-20s %-12s %-15s %s\n", 
                "VENUE", "ADDRESS", "SCREENINGS", "TICKETS SOLD"));
            reportTextArea.append("-".repeat(70) + "\n");
            
            int totalVenues = 0;
            int totalScreenings = 0;
            int totalTickets = 0;
            
            // Display venue basic info
            for (Venues venue : allVenues) {
                int ticketsSold = venuesDao.getTicketsSoldByVenue(venue.getVenue_id());
                ArrayList<String> screenings = venuesDao.getScreeningsByVenue(venue.getVenue_id());
                
                reportTextArea.append(String.format("%-20s %-12s %-15d %d\n", 
                    venue.getVenue_name(),
                    venue.getAddress().length() > 11 ? venue.getAddress().substring(0, 11) + "..." : venue.getAddress(),
                    screenings.size(),
                    ticketsSold));
                
                totalVenues++;
                totalScreenings += screenings.size();
                totalTickets += ticketsSold;
            }
            
            reportTextArea.append("-".repeat(70) + "\n");
            reportTextArea.append(String.format("TOTAL: %d venues, %d screenings, %d tickets\n", 
                totalVenues, totalScreenings, totalTickets));
            
            // Add capacity and occupancy data
            reportTextArea.append("\nCAPACITY AND OCCUPANCY ANALYSIS:\n");
            reportTextArea.append("-".repeat(70) + "\n");
            for (String capacityInfo : capacityData) {
                reportTextArea.append(capacityInfo + "\n");
            }
            
            // Update summary table
            tableModel.addRow(new Object[]{"Total Venues", totalVenues});
            tableModel.addRow(new Object[]{"Total Screenings", totalScreenings});
            tableModel.addRow(new Object[]{"Total Tickets Sold", totalTickets});
            tableModel.addRow(new Object[]{"Average Screenings per Venue", 
                String.format("%.1f", totalVenues > 0 ? (double)totalScreenings / totalVenues : 0)});
            tableModel.addRow(new Object[]{"Average Tickets per Venue", 
                String.format("%.1f", totalVenues > 0 ? (double)totalTickets / totalVenues : 0)});
            
        } catch (Exception e) {
            showError("Error generating venue utilization report: " + e.getMessage());
        }
    }
    
    private void appendReportHeader(String title, String description) {
        reportTextArea.append(title + "\n");
        reportTextArea.append("Generated: " + LocalDate.now() + " | " + description + "\n");
        reportTextArea.append("=" .repeat(60) + "\n\n");
    }
    
    private void clearReport() {
        tableModel.setRowCount(0);
        reportTextArea.setText("");
    }
    
    private void showError(String message) {
        reportTextArea.append("\nERROR: " + message + "\n");
        JOptionPane.showMessageDialog(this, message, "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
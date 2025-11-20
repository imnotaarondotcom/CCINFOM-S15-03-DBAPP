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
        reportTextArea.append("- Ticket Revenue Report - Shows total tickets sold and revenue for each branch\n");
        reportTextArea.append("- Customer Activity Report - Transaction frequency per customer\n");
        reportTextArea.append("- Movie Performance Report - Tracks ticket sales and attendance by movie\n");
        reportTextArea.append("- Venue Utilization Report - Shows seat occupancy and room capacity usage\n\n");
        
        reportTextArea.append("TIP: Use the summary table for quick insights and the detailed area for comprehensive data.");
    }
    
    private void generateTicketRevenueReport() {
        clearReport();
        appendReportHeader("TICKET REVENUE REPORT", "Shows total tickets sold and revenue for each branch");
        
        String sql = """
            SELECT 
                v.venue_name AS branch,
                COUNT(tb.ticket_no) AS tickets_sold,
                SUM(s.price) AS total_revenue,
                AVG(s.price) AS avg_ticket_price
            FROM TicketBookings tb
            JOIN Screenings s ON tb.screening_id = s.screening_id
            JOIN Venues v ON s.venue_id = v.venue_id
            WHERE tb.ticket_status = 'Booked'
            GROUP BY v.venue_id, v.venue_name
            ORDER BY total_revenue DESC
        """;
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            reportTextArea.append(String.format("%-25s %-15s %-15s %s\n", 
                "BRANCH", "TICKETS SOLD", "TOTAL REVENUE", "AVG PRICE"));
            reportTextArea.append("-".repeat(70) + "\n");
            
            double grandTotalRevenue = 0;
            int grandTotalTickets = 0;
            int branchCount = 0;
            
            while (rs.next()) {
                String branch = rs.getString("branch");
                int ticketsSold = rs.getInt("tickets_sold");
                double totalRevenue = rs.getDouble("total_revenue");
                double avgPrice = rs.getDouble("avg_ticket_price");
                
                reportTextArea.append(String.format("%-25s %-15d PHP %-14.2f PHP %-13.2f\n", 
                    branch, ticketsSold, totalRevenue, avgPrice));
                
                grandTotalRevenue += totalRevenue;
                grandTotalTickets += ticketsSold;
                branchCount++;
            }
            
            reportTextArea.append("-".repeat(70) + "\n");
            reportTextArea.append(String.format("%-25s %-15d PHP %-14.2f\n", 
                "GRAND TOTAL", grandTotalTickets, grandTotalRevenue));
            
            // Update summary table
            tableModel.addRow(new Object[]{"Total Branches", branchCount});
            tableModel.addRow(new Object[]{"Total Tickets Sold", grandTotalTickets});
            tableModel.addRow(new Object[]{"Total Revenue", String.format("PHP %.2f", grandTotalRevenue)});
            tableModel.addRow(new Object[]{"Average Ticket Price", 
                String.format("PHP %.2f", grandTotalTickets > 0 ? grandTotalRevenue / grandTotalTickets : 0)});
            
        } catch (SQLException e) {
            showError("Error generating ticket revenue report: " + e.getMessage());
        }
    }
    
    private void generateCustomerActivityReport() {
        clearReport();
        appendReportHeader("CUSTOMER ACTIVITY REPORT", "Transaction frequency per customer");
        
        String sql = """
            SELECT 
                c.username AS customer_name,
                COUNT(tb.ticket_no) AS total_tickets,
                SUM(s.price) AS total_spent,
                COUNT(DISTINCT DATE(tb.date_booked)) AS transaction_days
            FROM TicketBookings tb
            JOIN Customers c ON tb.customer_id = c.customer_id
            JOIN Screenings s ON tb.screening_id = s.screening_id
            WHERE tb.ticket_status = 'Booked'
            GROUP BY c.customer_id, c.username
            HAVING COUNT(tb.ticket_no) >= 1
            ORDER BY total_tickets DESC, total_spent DESC
            LIMIT 50
        """;
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            reportTextArea.append(String.format("%-25s %-15s %-15s %s\n", 
                "CUSTOMER", "TICKETS", "TOTAL SPENT", "TRANSACTION DAYS"));
            reportTextArea.append("-".repeat(70) + "\n");
            
            int totalCustomers = 0;
            int totalTickets = 0;
            double totalRevenue = 0;
            int totalTransactionDays = 0;
            
            while (rs.next()) {
                String customer = rs.getString("customer_name");
                int tickets = rs.getInt("total_tickets");
                double spent = rs.getDouble("total_spent");
                int transactionDays = rs.getInt("transaction_days");
                
                reportTextArea.append(String.format("%-25s %-15d PHP %-14.2f %-15d\n", 
                    customer, tickets, spent, transactionDays));
                
                totalCustomers++;
                totalTickets += tickets;
                totalRevenue += spent;
                totalTransactionDays += transactionDays;
            }
            
            reportTextArea.append("-".repeat(70) + "\n");
            reportTextArea.append(String.format("%-25s %-15d PHP %-14.2f\n", 
                "TOTAL (" + totalCustomers + " customers)", totalTickets, totalRevenue));
            
            // Update summary table
            tableModel.addRow(new Object[]{"Active Customers", totalCustomers});
            tableModel.addRow(new Object[]{"Total Tickets Purchased", totalTickets});
            tableModel.addRow(new Object[]{"Total Customer Revenue", String.format("PHP %.2f", totalRevenue)});
            tableModel.addRow(new Object[]{"Avg Tickets per Customer", 
                String.format("%.1f", totalCustomers > 0 ? (double)totalTickets / totalCustomers : 0)});
            tableModel.addRow(new Object[]{"Avg Transaction Days", 
                String.format("%.1f", totalCustomers > 0 ? (double)totalTransactionDays / totalCustomers : 0)});
            
        } catch (SQLException e) {
            showError("Error generating customer activity report: " + e.getMessage());
        }
    }
    
    private void generateMoviePerformanceReport() {
        clearReport();
        appendReportHeader("MOVIE PERFORMANCE REPORT", "Tracks ticket sales and attendance by movie");
        
        String sql = """
            SELECT 
                m.movie_name,
                m.genre,
                m.age_rating,
                COUNT(tb.ticket_no) AS tickets_sold,
                SUM(s.price) AS total_revenue,
                COUNT(DISTINCT s.screening_id) AS total_screenings
            FROM Movies m
            JOIN Screenings s ON m.movie_id = s.movie_id
            LEFT JOIN TicketBookings tb ON s.screening_id = tb.screening_id AND tb.ticket_status = 'Booked'
            GROUP BY m.movie_id, m.movie_name, m.genre, m.age_rating
            ORDER BY total_revenue DESC
        """;
        
        try (Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            // Header with proper spacing
            reportTextArea.append(String.format("%-30s %-15s %-8s %-8s %-12s %s\n", 
                "MOVIE", "GENRE", "RATING", "TICKETS", "REVENUE", "SCREENINGS"));
            reportTextArea.append("-".repeat(95) + "\n");
            
            int totalMovies = 0;
            int totalTickets = 0;
            double totalRevenue = 0;
            int totalScreenings = 0;
            
            while (rs.next()) {
                String movie = rs.getString("movie_name");
                String genre = rs.getString("genre");
                String rating = rs.getString("age_rating");
                int tickets = rs.getInt("tickets_sold");
                double revenue = rs.getDouble("total_revenue");
                int screenings = rs.getInt("total_screenings");
                
                // Truncate long movie names but keep them readable
                if (movie.length() > 27) {
                    movie = movie.substring(0, 27) + "...";
                }
                if (genre.length() > 14) {
                    genre = genre.substring(0, 14);
                }
                
                reportTextArea.append(String.format("%-30s %-15s %-8s %-8d PHP %-11.2f %-12d\n", 
                    movie, genre, rating, tickets, revenue, screenings));
                
                totalMovies++;
                totalTickets += tickets;
                totalRevenue += revenue;
                totalScreenings += screenings;
            }
            
            reportTextArea.append("-".repeat(95) + "\n");
            reportTextArea.append(String.format("%-30s %-15s %-8s %-8d PHP %-11.2f %-12d\n", 
                "TOTAL (" + totalMovies + " movies)", "", "", totalTickets, totalRevenue, totalScreenings));
            
            // Update summary table
            tableModel.addRow(new Object[]{"Total Movies", totalMovies});
            tableModel.addRow(new Object[]{"Total Tickets Sold", totalTickets});
            tableModel.addRow(new Object[]{"Total Revenue", String.format("PHP %.2f", totalRevenue)});
            tableModel.addRow(new Object[]{"Total Screenings", totalScreenings});
            tableModel.addRow(new Object[]{"Average Revenue per Movie", 
                String.format("PHP %.2f", totalMovies > 0 ? totalRevenue / totalMovies : 0)});
            tableModel.addRow(new Object[]{"Average Tickets per Screening", 
                String.format("%.1f", totalScreenings > 0 ? (double)totalTickets / totalScreenings : 0)});
            
        } catch (SQLException e) {
            showError("Error generating movie performance report: " + e.getMessage());
        }
    }
    
    private void generateVenueUtilizationReport() {
        clearReport();
        appendReportHeader("VENUE UTILIZATION REPORT", "Shows seat occupancy and room capacity usage");
        
        String sql = """
            SELECT 
                v.venue_name,
                COUNT(DISTINCT r.room_id) AS total_rooms,
                COUNT(DISTINCT s.seat_id) AS total_seats,
                COUNT(DISTINCT tb.ticket_no) AS occupied_seats,
                COUNT(DISTINCT sc.screening_id) AS total_screenings,
                ROUND((COUNT(DISTINCT tb.ticket_no) * 100.0 / COUNT(DISTINCT s.seat_id)), 2) AS occupancy_rate
            FROM Venues v
            LEFT JOIN Rooms r ON v.venue_id = r.venue_id
            LEFT JOIN Seats s ON r.room_id = s.room_id
            LEFT JOIN Screenings sc ON r.room_id = sc.room_id
            LEFT JOIN TicketBookings tb ON sc.screening_id = tb.screening_id AND tb.ticket_status = 'Booked'
            GROUP BY v.venue_id, v.venue_name
            ORDER BY occupancy_rate DESC
        """;
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            reportTextArea.append(String.format("%-20s %-8s %-12s %-15s %-12s %s\n", 
                "VENUE", "ROOMS", "TOTAL SEATS", "OCCUPIED", "OCCUPANCY %", "SCREENINGS"));
            reportTextArea.append("-".repeat(80) + "\n");
            
            int totalVenues = 0;
            int totalRooms = 0;
            int totalSeats = 0;
            int totalOccupied = 0;
            int totalScreenings = 0;
            
            while (rs.next()) {
                String venue = rs.getString("venue_name");
                int rooms = rs.getInt("total_rooms");
                int seats = rs.getInt("total_seats");
                int occupied = rs.getInt("occupied_seats");
                int screenings = rs.getInt("total_screenings");
                double occupancy = rs.getDouble("occupancy_rate");
                
                reportTextArea.append(String.format("%-20s %-8d %-12d %-15d %-11.1f%% %-12d\n", 
                    venue, rooms, seats, occupied, occupancy, screenings));
                
                totalVenues++;
                totalRooms += rooms;
                totalSeats += seats;
                totalOccupied += occupied;
                totalScreenings += screenings;
            }
            
            reportTextArea.append("-".repeat(80) + "\n");
            double overallOccupancy = totalSeats > 0 ? (double)totalOccupied / totalSeats * 100 : 0;
            reportTextArea.append(String.format("%-20s %-8d %-12d %-15d %-11.1f%% %-12d\n", 
                "TOTAL (" + totalVenues + " venues)", totalRooms, totalSeats, totalOccupied, overallOccupancy, totalScreenings));
            
            // Update summary table
            tableModel.addRow(new Object[]{"Total Venues", totalVenues});
            tableModel.addRow(new Object[]{"Total Rooms", totalRooms});
            tableModel.addRow(new Object[]{"Total Seats", totalSeats});
            tableModel.addRow(new Object[]{"Occupied Seats", totalOccupied});
            tableModel.addRow(new Object[]{"Overall Occupancy", String.format("%.1f%%", overallOccupancy)});
            tableModel.addRow(new Object[]{"Total Screenings", totalScreenings});
            
        } catch (SQLException e) {
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
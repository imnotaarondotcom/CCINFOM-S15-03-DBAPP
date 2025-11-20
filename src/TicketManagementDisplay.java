import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TicketManagementDisplay extends JPanel {
    private MainGUI mainGUI;
    private TicketDao ticketDao;
    private ScreeningDao screeningDao;
    private SeatDao seatDao;
    private int loggedCustomerId;
    private String accountType;
    private JTable ticketTable;
    private DefaultTableModel tableModel;
    
    public TicketManagementDisplay(MainGUI mainGUI, int customerId, String accountType) {
        this.mainGUI = mainGUI;
        this.loggedCustomerId = customerId;
        this.accountType = accountType;
        this.ticketDao = new TicketDao();
        this.screeningDao = new ScreeningDao();
        this.seatDao = new SeatDao();
        setLayout(new BorderLayout());
        initializeComponents();
        loadTicketData();
    }
    
    private void initializeComponents() {
        // Header
        JLabel header = new JLabel("Ticket Management", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Table for displaying tickets
        String[] columnNames = {"Ticket No", "Movie", "Venue", "Room", "Seat", "Date", "Time", "Price", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        ticketTable = new JTable(tableModel);
        ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        ticketTable.setFont(new Font("Arial", Font.PLAIN, 11));
        ticketTable.setRowHeight(20);
        
        // Set column widths
        ticketTable.getColumnModel().getColumn(0).setPreferredWidth(70);  // Ticket No
        ticketTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Movie
        ticketTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Venue
        ticketTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Room
        ticketTable.getColumnModel().getColumn(4).setPreferredWidth(50);  // Seat
        ticketTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Date
        ticketTable.getColumnModel().getColumn(6).setPreferredWidth(70);  // Time
        ticketTable.getColumnModel().getColumn(7).setPreferredWidth(60);  // Price
        ticketTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // Status
        
        JScrollPane scrollPane = new JScrollPane(ticketTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Tickets"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton buyButton = new JButton("Buy Ticket");
        JButton refundButton = new JButton("Refund Ticket");
        JButton blockButton = new JButton("Block Screening");
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back");
        
        // Style buttons
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        buyButton.setFont(buttonFont);
        refundButton.setFont(buttonFont);
        blockButton.setFont(buttonFont);
        refreshButton.setFont(buttonFont);
        backButton.setFont(buttonFont);
        
        // Add action listeners
        buyButton.addActionListener(e -> buyTicket());
        refundButton.addActionListener(e -> refundTicket());
        blockButton.addActionListener(e -> blockScreening());
        refreshButton.addActionListener(e -> loadTicketData());
        backButton.addActionListener(e -> {
            if ("Admin".equals(accountType)) {
                mainGUI.showScreen("ADMIN PANEL");
            } else {
                mainGUI.showScreen("CUSTOMER PANEL");
            }
        });
        
        buttonPanel.add(buyButton);
        buttonPanel.add(refundButton);
        buttonPanel.add(blockButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        
        // Add components to panel
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadTicketData() {
        tableModel.setRowCount(0); // Clear existing data
        
        String sql = """
            SELECT t.ticket_no, m.movie_name, v.venue_name, r.room_name, 
                s.seat_no, sc.screening_date, sc.screening_start_time, 
                sc.price, t.ticket_status, t.customer_id, c.username
            FROM TicketBookings t
            JOIN Screenings sc ON t.screening_id = sc.screening_id
            JOIN Movies m ON sc.movie_id = m.movie_id
            JOIN Venues v ON sc.venue_id = v.venue_id
            JOIN Rooms r ON sc.room_id = r.room_id
            JOIN Seats s ON t.seat_id = s.seat_id
            JOIN Customers c ON t.customer_id = c.customer_id
            """;

        if (!"Admin".equals(accountType)) {
            sql += " WHERE t.customer_id = ?";
        }
        
        sql += " ORDER BY sc.screening_date DESC, sc.screening_start_time DESC";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // parameter for customer view
            if (!"Admin".equals(accountType)) {
                pst.setInt(1, loggedCustomerId);
            }
            
            ResultSet rs = pst.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String status = rs.getString("ticket_status");
                String customerName = rs.getString("username");
                
                // for admins, show customer name in the movie column
                String displayMovie = "Admin".equals(accountType) ? 
                    rs.getString("movie_name") + " (" + customerName + ")" : 
                    rs.getString("movie_name");
                
                tableModel.addRow(new Object[]{
                    rs.getInt("ticket_no"),
                    displayMovie,
                    rs.getString("venue_name"),
                    rs.getString("room_name"),
                    rs.getInt("seat_no"),
                    rs.getDate("screening_date"),
                    rs.getTime("screening_start_time"),
                    String.format("PHP %.2f", rs.getDouble("price")),
                    status
                });
            }
            
            if (!hasData) {
                JOptionPane.showMessageDialog(this, 
                    "No tickets found.", 
                    "No Tickets", 
                    JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading tickets: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void buyTicket() {
        // First show available screenings
        JDialog screeningsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Available Screenings", true);
        screeningsDialog.setLayout(new BorderLayout());
        screeningsDialog.setSize(800, 400);
        screeningsDialog.setLocationRelativeTo(this);
        
        String[] screeningColumns = {"Screening ID", "Movie", "Venue", "Room", "Date", "Time", "Price"};
        DefaultTableModel screeningsModel = new DefaultTableModel(screeningColumns, 0);
        JTable screeningsTable = new JTable(screeningsModel);
        
        // Load active screenings
        String sql = """
            SELECT s.screening_id, m.movie_name, v.venue_name, r.room_name, 
                   s.screening_date, s.screening_start_time, s.price
            FROM Screenings s
            JOIN Movies m ON s.movie_id = m.movie_id
            JOIN Venues v ON s.venue_id = v.venue_id
            JOIN Rooms r ON s.room_id = r.room_id
            WHERE s.screening_status = 'Active'
            ORDER BY s.screening_date, s.screening_start_time
        """;
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                screeningsModel.addRow(new Object[]{
                    rs.getInt("screening_id"),
                    rs.getString("movie_name"),
                    rs.getString("venue_name"),
                    rs.getString("room_name"),
                    rs.getDate("screening_date"),
                    rs.getTime("screening_start_time"),
                    String.format("PHP %.2f", rs.getDouble("price"))
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading screenings: " + e.getMessage());
            return;
        }
        
        JButton selectButton = new JButton("Select Screening");
        selectButton.addActionListener(e -> {
            int selectedRow = screeningsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(screeningsDialog, "Please select a screening!");
                return;
            }
            int screeningId = (int) screeningsModel.getValueAt(selectedRow, 0);
            screeningsDialog.dispose();
            showSeatSelection(screeningId);
        });
        
        screeningsDialog.add(new JScrollPane(screeningsTable), BorderLayout.CENTER);
        screeningsDialog.add(selectButton, BorderLayout.SOUTH);
        screeningsDialog.setVisible(true);
    }
    
    private void showSeatSelection(int screeningId) {
        JDialog seatDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Select Seat", true);
        seatDialog.setLayout(new BorderLayout());
        seatDialog.setSize(500, 400);
        seatDialog.setLocationRelativeTo(this);
        
        // Get available seats
        Map<Integer, Integer> seatMap = new HashMap<>();
        String sql = """
            SELECT s.seat_id, s.seat_no 
            FROM Seats s
            WHERE s.room_id = (SELECT room_id FROM Screenings WHERE screening_id = ?)
            AND s.seat_id NOT IN (
                SELECT seat_id FROM TicketBookings 
                WHERE screening_id = ? AND ticket_status = 'Booked'
            )
            ORDER BY s.seat_no
        """;
        
        DefaultListModel<String> seatModel = new DefaultListModel<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, screeningId);
            pst.setInt(2, screeningId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int seatId = rs.getInt("seat_id");
                int seatNo = rs.getInt("seat_no");
                seatMap.put(seatNo, seatId);
                seatModel.addElement("Seat " + seatNo);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading seats: " + e.getMessage());
            return;
        }
        
        if (seatMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No seats available for this screening!");
            return;
        }
        
        JList<String> seatList = new JList<>(seatModel);
        JButton bookButton = new JButton("Book Selected Seat");
        
        bookButton.addActionListener(e -> {
            int selectedIndex = seatList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(seatDialog, "Please select a seat!");
                return;
            }
            
            String selectedSeat = seatList.getSelectedValue();
            int seatNo = Integer.parseInt(selectedSeat.replace("Seat ", ""));
            int seatId = seatMap.get(seatNo);
            
            String insertSQL = """
                INSERT INTO TicketBookings (ticket_status, seat_id, date_booked, customer_id, screening_id)
                VALUES ('Booked', ?, NOW(), ?, ?)
            """;
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement(insertSQL)) {
                
                pst.setInt(1, seatId);
                pst.setInt(2, loggedCustomerId);
                pst.setInt(3, screeningId);
                pst.executeUpdate();
                
                JOptionPane.showMessageDialog(seatDialog, 
                    "Ticket booked successfully! Seat: " + seatNo);
                seatDialog.dispose();
                loadTicketData(); // Refresh the table
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(seatDialog, "Error booking ticket: " + ex.getMessage());
            }
        });
        
        seatDialog.add(new JScrollPane(seatList), BorderLayout.CENTER);
        seatDialog.add(bookButton, BorderLayout.SOUTH);
        seatDialog.setVisible(true);
    }
    
    private void refundTicket() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a ticket to refund!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int ticketNo = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 8);
        String movie = (String) tableModel.getValueAt(selectedRow, 1);
        
        if ("Refunded".equals(status)) {
            JOptionPane.showMessageDialog(this, "This ticket has already been refunded!");
            return;
        }
        if ("Cancelled".equals(status)) {
            JOptionPane.showMessageDialog(this, "This ticket was cancelled with the screening!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to refund ticket for: " + movie + "?",
            "Confirm Refund",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            ticketDao.refundTicket(ticketNo);
            JOptionPane.showMessageDialog(this, "Ticket refunded successfully!");
            loadTicketData();
        }
    }
    
    private void blockScreening() {
        JDialog blockDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Block Screening", true);
        blockDialog.setLayout(new BorderLayout());
        blockDialog.setSize(600, 400);
        blockDialog.setLocationRelativeTo(this);
        
        String[] screeningColumns = {"Screening ID", "Movie", "Venue", "Date", "Time", "Status", "Available Seats", "Total Seats"};
        DefaultTableModel screeningsModel = new DefaultTableModel(screeningColumns, 0);
        JTable screeningsTable = new JTable(screeningsModel);
        
        // Load all screenings with available seat count and total seats
        String sql = """
            SELECT 
                s.screening_id, 
                m.movie_name, 
                v.venue_name, 
                s.screening_date, 
                s.screening_start_time, 
                s.screening_status,
                s.price,
                (SELECT COUNT(*) FROM Seats se 
                 WHERE se.room_id = s.room_id 
                 AND se.seat_id NOT IN (
                     SELECT seat_id FROM TicketBookings 
                     WHERE screening_id = s.screening_id AND ticket_status = 'Booked'
                 )) AS available_seats,
                (SELECT COUNT(*) FROM Seats se 
                 WHERE se.room_id = s.room_id) AS total_seats
            FROM Screenings s
            JOIN Movies m ON s.movie_id = m.movie_id
            JOIN Venues v ON s.venue_id = v.venue_id
            WHERE s.screening_status = 'Active'
            ORDER BY s.screening_date, s.screening_start_time
        """;
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                screeningsModel.addRow(new Object[]{
                    rs.getInt("screening_id"),
                    rs.getString("movie_name"),
                    rs.getString("venue_name"),
                    rs.getDate("screening_date"),
                    rs.getTime("screening_start_time"),
                    rs.getString("screening_status"),
                    rs.getInt("available_seats"),
                    rs.getInt("total_seats")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading screenings: " + e.getMessage());
            return;
        }
        
        JButton blockButton = new JButton("Block Entire Screening");
        blockButton.addActionListener(e -> {
            int selectedRow = screeningsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(blockDialog, "Please select a screening!");
                return;
            }
            
            int screeningId = (int) screeningsModel.getValueAt(selectedRow, 0);
            String movie = (String) screeningsModel.getValueAt(selectedRow, 1);
            int availableSeats = (int) screeningsModel.getValueAt(selectedRow, 6);
            int totalSeats = (int) screeningsModel.getValueAt(selectedRow, 7);
            double price = getScreeningPrice(screeningId);
            
            // Check if ALL seats are available
            if (availableSeats != totalSeats) {
                JOptionPane.showMessageDialog(blockDialog, 
                    "Cannot block this screening!\n\n" +
                    "Only screenings with ALL seats available can be blocked.\n" +
                    "Available seats: " + availableSeats + "/" + totalSeats + "\n" +
                    "Some seats are already booked by other customers.",
                    "Cannot Block Screening", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double totalCost = price * totalSeats;
            
            int confirm = JOptionPane.showConfirmDialog(
                blockDialog,
                "BLOCK ENTIRE SCREENING: " + movie + "\n\n" +
                "Total Seats: " + totalSeats + "\n" +
                "Price per seat: PHP " + String.format("%.2f", price) + "\n" +
                "TOTAL COST: PHP " + String.format("%.2f", totalCost) + "\n\n" +
                "This will book ALL " + totalSeats + " seats under your account.\n" +
                "No other customers will be able to book this screening.\n\n" +
                "Are you sure you want to proceed?",
                "Confirm Block Entire Screening",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                // block the screening by creating bookings for all seats
                boolean success = blockAllSeats(screeningId, totalSeats);
                if (success) {
                    JOptionPane.showMessageDialog(blockDialog, 
                        "Screening blocked successfully!\n" +
                        "All " + totalSeats + " seats have been booked under your account.\n" +
                        "Total charged: PHP " + String.format("%.2f", totalCost));
                    blockDialog.dispose();
                    loadTicketData();
                } else {
                    JOptionPane.showMessageDialog(blockDialog, 
                        "Failed to block screening!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        blockDialog.add(new JScrollPane(screeningsTable), BorderLayout.CENTER);
        blockDialog.add(blockButton, BorderLayout.SOUTH);
        blockDialog.setVisible(true);
    }

    private boolean blockAllSeats(int screeningId, int totalSeats) {
        String getAllSeatsSQL = """
            SELECT s.seat_id 
            FROM Seats s
            WHERE s.room_id = (SELECT room_id FROM Screenings WHERE screening_id = ?)
            ORDER BY s.seat_no
        """;
        
        String insertBookedSeatSQL = """
            INSERT INTO TicketBookings (ticket_status, seat_id, date_booked, customer_id, screening_id)
            VALUES ('Booked', ?, NOW(), ?, ?)
        """;
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Get all seats in the room
                ArrayList<Integer> allSeatIds = new ArrayList<>();
                try (PreparedStatement pst = conn.prepareStatement(getAllSeatsSQL)) {
                    pst.setInt(1, screeningId);
                    ResultSet rs = pst.executeQuery();
                    
                    while (rs.next()) {
                        allSeatIds.add(rs.getInt("seat_id"));
                    }
                }
                
                if (allSeatIds.size() != totalSeats) {
                    throw new SQLException("Seat count mismatch!");
                }
                
                // Create booked tickets for all seats using current user account
                try (PreparedStatement pst = conn.prepareStatement(insertBookedSeatSQL)) {
                    for (int seatId : allSeatIds) {
                        pst.setInt(1, seatId);
                        pst.setInt(2, loggedCustomerId); // use current user ID
                        pst.setInt(3, screeningId);
                        pst.addBatch();
                    }
                    pst.executeBatch();
                }
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error blocking seats: " + e.getMessage());
            return false;
        }
    }
    
    private double getScreeningPrice(int screeningId) {
        String sql = "SELECT price FROM Screenings WHERE screening_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, screeningId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("price");
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error getting screening price: " + e.getMessage());
        }
        
        return 0.0;
    }
}
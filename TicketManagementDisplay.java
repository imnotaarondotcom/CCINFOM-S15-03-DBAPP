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
        
        if (!"Admin".equals(accountType)) {
            blockButton.setVisible(false);
        }
        
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
                sc.price, t.ticket_status, t.customer_id
            FROM TicketBookings t
            JOIN Screenings sc ON t.screening_id = sc.screening_id
            JOIN Movies m ON sc.movie_id = m.movie_id
            JOIN Venues v ON sc.venue_id = v.venue_id
            JOIN Rooms r ON sc.room_id = r.room_id
            JOIN Seats s ON t.seat_id = s.seat_id
            WHERE (t.customer_id = ? OR ? = 'Admin')
            AND (t.ticket_status != 'Blocked' OR ? = 'Admin')
            ORDER BY sc.screening_date DESC, sc.screening_start_time DESC
        """;

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, loggedCustomerId);
            pst.setString(2, accountType);
            pst.setString(3, accountType); // For the blocked tickets condition
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("ticket_status");
                    int customerId = rs.getInt("customer_id");
                    
                    // For blocked tickets, show special message
                    String displayStatus = status;
                    if ("Blocked".equals(status) && customerId == -1) {
                        displayStatus = "Blocked (Admin)";
                    }
                    
                    tableModel.addRow(new Object[]{
                        rs.getInt("ticket_no"),
                        rs.getString("movie_name"),
                        rs.getString("venue_name"),
                        rs.getString("room_name"),
                        rs.getInt("seat_no"),
                        rs.getDate("screening_date"),
                        rs.getTime("screening_start_time"),
                        String.format("PHP %.2f", rs.getDouble("price")),
                        displayStatus
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading tickets: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void buyTicket() {
        // First, show available screenings
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
                    String.format("$%.2f", rs.getDouble("price"))
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
            
            // Book the ticket
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
    if (!"Admin".equals(accountType)) {
        JOptionPane.showMessageDialog(this, "Admin access required!");
        return;
    }
    
    JDialog blockDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
        "Block Screening", true);
    blockDialog.setLayout(new BorderLayout());
    blockDialog.setSize(600, 400);
    blockDialog.setLocationRelativeTo(this);
    
    String[] screeningColumns = {"Screening ID", "Movie", "Venue", "Date", "Time", "Status", "Available Seats"};
    DefaultTableModel screeningsModel = new DefaultTableModel(screeningColumns, 0);
    JTable screeningsTable = new JTable(screeningsModel);
    
    // Load all screenings with available seat count
    String sql = """
        SELECT 
            s.screening_id, 
            m.movie_name, 
            v.venue_name, 
            s.screening_date, 
            s.screening_start_time, 
            s.screening_status,
            (SELECT COUNT(*) FROM Seats se 
             WHERE se.room_id = s.room_id 
             AND se.seat_id NOT IN (
                 SELECT seat_id FROM TicketBookings 
                 WHERE screening_id = s.screening_id AND ticket_status = 'Booked'
             )) AS available_seats
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
                rs.getInt("available_seats")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading screenings: " + e.getMessage());
        return;
    }
    
    JButton blockButton = new JButton("Block Screening (Make All Seats Unavailable)");
    blockButton.addActionListener(e -> {
        int selectedRow = screeningsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(blockDialog, "Please select a screening!");
            return;
        }
        
        int screeningId = (int) screeningsModel.getValueAt(selectedRow, 0);
        String movie = (String) screeningsModel.getValueAt(selectedRow, 1);
        int availableSeats = (int) screeningsModel.getValueAt(selectedRow, 6);
        
        if (availableSeats == 0) {
            JOptionPane.showMessageDialog(blockDialog, 
                "This screening already has no available seats!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            blockDialog,
            "BLOCK screening for: " + movie + "?\n\n" +
            "This will make ALL " + availableSeats + " available seats unavailable for booking.\n" +
            "Existing booked tickets will NOT be affected.\n\n" +
            "This action cannot be undone!",
            "Confirm Block Screening",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Block the screening by creating dummy bookings for all available seats
            boolean success = blockAllAvailableSeats(screeningId);
            if (success) {
                JOptionPane.showMessageDialog(blockDialog, 
                    "Screening blocked successfully!\n" +
                    "All " + availableSeats + " seats are now unavailable for booking.");
                blockDialog.dispose();
                loadTicketData();
            } else {
                JOptionPane.showMessageDialog(blockDialog, 
                    "Failed to block screening!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });
    
    // Add a cancel screening button as well for clarity
    JButton cancelButton = new JButton("Cancel Screening");
    cancelButton.addActionListener(e -> {
        int selectedRow = screeningsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(blockDialog, "Please select a screening!");
            return;
        }
        
        int screeningId = (int) screeningsModel.getValueAt(selectedRow, 0);
        String status = (String) screeningsModel.getValueAt(selectedRow, 5);
        String movie = (String) screeningsModel.getValueAt(selectedRow, 1);
        
        if ("Cancelled".equals(status)) {
            JOptionPane.showMessageDialog(blockDialog, "This screening is already cancelled!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            blockDialog,
            "CANCEL screening for: " + movie + "?\n\n" +
            "This will cancel the screening AND refund/cancel all associated tickets!",
            "Confirm Cancel Screening",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            cancelScreeningAndTickets(screeningId);
            JOptionPane.showMessageDialog(blockDialog, "Screening and associated tickets cancelled!");
            blockDialog.dispose();
            loadTicketData();
        }
    });
    
    JPanel buttonPanel = new JPanel(new FlowLayout());
    buttonPanel.add(blockButton);
    buttonPanel.add(cancelButton);
    
    blockDialog.add(new JScrollPane(screeningsTable), BorderLayout.CENTER);
    blockDialog.add(buttonPanel, BorderLayout.SOUTH);
    blockDialog.setVisible(true);
}

    private boolean blockAllAvailableSeats(int screeningId) {
        String getAvailableSeatsSQL = """
            SELECT s.seat_id 
            FROM Seats s
            WHERE s.room_id = (SELECT room_id FROM Screenings WHERE screening_id = ?)
            AND s.seat_id NOT IN (
                SELECT seat_id FROM TicketBookings 
                WHERE screening_id = ? AND ticket_status = 'Booked'
            )
        """;
        
        String insertBlockedSeatSQL = """
            INSERT INTO TicketBookings (ticket_status, seat_id, date_booked, customer_id, screening_id)
            VALUES ('Blocked', ?, NOW(), -1, ?)
        """;
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Get all available seats
                ArrayList<Integer> availableSeatIds = new ArrayList<>();
                try (PreparedStatement pst = conn.prepareStatement(getAvailableSeatsSQL)) {
                    pst.setInt(1, screeningId);
                    pst.setInt(2, screeningId);
                    ResultSet rs = pst.executeQuery();
                    
                    while (rs.next()) {
                        availableSeatIds.add(rs.getInt("seat_id"));
                    }
                }
                
                if (availableSeatIds.isEmpty()) {
                    return true; // No seats to block
                }
                
                // Create blocked bookings for all available seats
                try (PreparedStatement pst = conn.prepareStatement(insertBlockedSeatSQL)) {
                    for (int seatId : availableSeatIds) {
                        pst.setInt(1, seatId);
                        pst.setInt(2, screeningId);
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
    
    private void cancelScreeningAndTickets(int screeningId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // 1. Cancel all tickets for this screening
                String updateTicketsSQL = """
                    UPDATE TicketBookings 
                    SET ticket_status = 'Cancelled' 
                    WHERE screening_id = ? AND ticket_status = 'Booked'
                """;
                
                try (PreparedStatement pst = conn.prepareStatement(updateTicketsSQL)) {
                    pst.setInt(1, screeningId);
                    pst.executeUpdate();
                }
                
                // 2. Cancel the screening
                String updateScreeningSQL = """
                    UPDATE Screenings 
                    SET screening_status = 'Cancelled' 
                    WHERE screening_id = ?
                """;
                
                try (PreparedStatement pst = conn.prepareStatement(updateScreeningSQL)) {
                    pst.setInt(1, screeningId);
                    pst.executeUpdate();
                }
                
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error cancelling screening: " + e.getMessage());
        }
    }
}
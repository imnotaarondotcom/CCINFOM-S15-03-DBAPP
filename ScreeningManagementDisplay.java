import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ScreeningManagementDisplay extends JPanel {
    private MainGUI mainGUI;
    private ScreeningDao screeningDao;
    private MovieDao movieDao;
    private VenuesDao venuesDao;
    private JTable screeningTable;
    private DefaultTableModel tableModel;
    private String accountType;
    
    public ScreeningManagementDisplay(MainGUI mainGUI, String accountType) {
        this.mainGUI = mainGUI;
        this.accountType = accountType;
        this.screeningDao = new ScreeningDao();
        this.movieDao = new MovieDao();
        this.venuesDao = new VenuesDao();
        setLayout(new BorderLayout());
        initializeComponents();
        loadScreeningData();
    }
    
    private void initializeComponents() {
        // Header
        JLabel header = new JLabel("Screenings Management", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");

        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                searchScreeningsByMovie(searchTerm);
            } else {
                loadScreeningData(); // Reload all if search is empty
            }
        });

        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadScreeningData();
        });

        searchPanel.add(new JLabel("Search by Movie:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        
        // Table for displaying screenings
        String[] columnNames = {"Screening ID", "Movie", "Venue", "Room", "Date", "Start Time", "End Time", "Price", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        screeningTable = new JTable(tableModel);
        screeningTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        screeningTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        screeningTable.setFont(new Font("Arial", Font.PLAIN, 11));
        screeningTable.setRowHeight(20);
        
        // Set column widths
        screeningTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Screening ID
        screeningTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Movie
        screeningTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Venue
        screeningTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Room
        screeningTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Date
        screeningTable.getColumnModel().getColumn(5).setPreferredWidth(70);  // Start Time
        screeningTable.getColumnModel().getColumn(6).setPreferredWidth(70);  // End Time
        screeningTable.getColumnModel().getColumn(7).setPreferredWidth(60);  // Price
        screeningTable.getColumnModel().getColumn(8).setPreferredWidth(70);  // Status
        
        JScrollPane scrollPane = new JScrollPane(screeningTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("All Screenings"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton addButton = new JButton("Add Screening");
        JButton cancelButton = new JButton("Cancel Screening");
        JButton completeButton = new JButton("Complete Screening");
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back");
        
        // Style buttons
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        addButton.setFont(buttonFont);
        cancelButton.setFont(buttonFont);
        completeButton.setFont(buttonFont);
        refreshButton.setFont(buttonFont);
        backButton.setFont(buttonFont);
        
        // Add action listeners
        addButton.addActionListener(e -> addScreening());
        cancelButton.addActionListener(e -> cancelScreening());
        completeButton.addActionListener(e -> completeScreening()); 
        refreshButton.addActionListener(e -> loadScreeningData());
        backButton.addActionListener(e -> {
            if ("Admin".equals(accountType)) {
                mainGUI.showScreen("ADMIN PANEL");
            } else {
                mainGUI.showScreen("CUSTOMER PANEL");
            }
        });
        
        // Show/hide buttons based on account type
        if (!"Admin".equals(accountType)) {
            addButton.setVisible(false);
            cancelButton.setVisible(false);
            completeButton.setVisible(false);
        }
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        
        // Add components to panel
        add(header, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.SOUTH);
        
        // Create center panel to hold both table and buttons
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }
    
    private void loadScreeningData() {
        tableModel.setRowCount(0); // Clear existing data
        
        String sql = """
            SELECT s.screening_id, v.venue_name, r.room_name, m.movie_name,
                   m.genre, m.age_rating, s.screening_date,
                   s.screening_start_time, s.screening_end_time, s.price, s.screening_status
            FROM Screenings s
            JOIN Venues v ON s.venue_id = v.venue_id
            JOIN Rooms r ON s.room_id = r.room_id
            JOIN Movies m ON s.movie_id = m.movie_id
            ORDER BY s.screening_date, s.screening_start_time
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("screening_id"),
                    rs.getString("movie_name"),
                    rs.getString("venue_name"),
                    rs.getString("room_name"),
                    rs.getDate("screening_date"),
                    rs.getTime("screening_start_time"),
                    rs.getTime("screening_end_time"),
                    String.format("PHP %.2f", rs.getDouble("price")),
                    rs.getString("screening_status")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading screenings: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchScreeningsByMovie(String searchTerm) {
        tableModel.setRowCount(0); // Clear existing data
        
        String sql = """
            SELECT s.screening_id, v.venue_name, r.room_name, m.movie_name,
                   m.genre, m.age_rating, s.screening_date,
                   s.screening_start_time, s.screening_end_time, s.price, s.screening_status
            FROM Screenings s
            JOIN Venues v ON s.venue_id = v.venue_id
            JOIN Rooms r ON s.room_id = r.room_id
            JOIN Movies m ON s.movie_id = m.movie_id
            WHERE m.movie_name LIKE ?
            ORDER BY s.screening_date, s.screening_start_time
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pst.executeQuery();

            boolean foundResults = false;
            while (rs.next()) {
                foundResults = true;
                tableModel.addRow(new Object[]{
                    rs.getInt("screening_id"),
                    rs.getString("movie_name"),
                    rs.getString("venue_name"),
                    rs.getString("room_name"),
                    rs.getDate("screening_date"),
                    rs.getTime("screening_start_time"),
                    rs.getTime("screening_end_time"),
                    String.format("PHP %.2f", rs.getDouble("price")),
                    rs.getString("screening_status")
                });
            }

            if (!foundResults) {
                JOptionPane.showMessageDialog(this, "No screenings found for movie: " + searchTerm, 
                    "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching screenings: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addScreening() {
        JDialog addDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Screening", true);
        addDialog.setLayout(new BorderLayout());
        addDialog.setSize(500, 600);
        addDialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Movie selection
        JComboBox<String> movieCombo = new JComboBox<>();
        loadMoviesIntoCombo(movieCombo);
        
        // Venue selection
        JComboBox<String> venueCombo = new JComboBox<>();
        loadVenuesIntoCombo(venueCombo);
        
        // Room selection (will be populated based on venue)
        JComboBox<String> roomCombo = new JComboBox<>();
        
        JTextField dateField = new JTextField();
        JTextField startTimeField = new JTextField();
        JTextField priceField = new JTextField();
        
        // Update rooms when venue changes
        venueCombo.addActionListener(e -> {
            if (venueCombo.getSelectedItem() != null) {
                String venueInfo = (String) venueCombo.getSelectedItem();
                int venueId = extractIdFromCombo(venueInfo);
                loadRoomsIntoCombo(roomCombo, venueId);
            }
        });
        
        formPanel.add(new JLabel("Movie:"));
        formPanel.add(movieCombo);
        formPanel.add(new JLabel("Venue:"));
        formPanel.add(venueCombo);
        formPanel.add(new JLabel("Room:"));
        formPanel.add(roomCombo);
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Start Time (HH:MM):"));
        formPanel.add(startTimeField);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceField);
        
        // Help labels
        JLabel helpLabel = new JLabel("Format: YYYY-MM-DD (e.g., 2024-01-15)");
        helpLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        formPanel.add(helpLabel);
        
        JLabel timeHelpLabel = new JLabel("Format: HH:MM (e.g., 14:30)");
        timeHelpLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        formPanel.add(timeHelpLabel);
        
        JButton saveButton = new JButton("Save Screening");
        JButton cancelButton = new JButton("Cancel");
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        saveButton.addActionListener(e -> {
            // Validation
            if (movieCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(addDialog, "Please select a movie!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (venueCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(addDialog, "Please select a venue!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (roomCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(addDialog, "Please select a room!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (dateField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "Please enter a date!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (startTimeField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "Please enter a start time!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (priceField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "Please enter a price!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Extract IDs from comboboxes
                int movieId = extractIdFromCombo((String) movieCombo.getSelectedItem());
                int venueId = extractIdFromCombo((String) venueCombo.getSelectedItem());
                int roomId = extractIdFromCombo((String) roomCombo.getSelectedItem());
                double price = Double.parseDouble(priceField.getText().trim());
                
                // Parse date and time
                java.time.LocalDate screeningDate = java.time.LocalDate.parse(dateField.getText().trim());
                java.time.LocalTime startTime = java.time.LocalTime.parse(startTimeField.getText().trim());
                
                // Get movie duration to calculate end time
                Movie movie = movieDao.getMovieById(movieId);
                if (movie == null) {
                    JOptionPane.showMessageDialog(addDialog, "Movie not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                java.time.LocalTime endTime = startTime.plusMinutes(movie.getDuration());
                
                // Check if room is available
                if (!screeningDao.isRoomAvailable(roomId, screeningDate, startTime, endTime, null)) {
                    JOptionPane.showMessageDialog(addDialog, 
                        "Room is not available at this time!\nPlease choose a different time or room.", 
                        "Room Conflict", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Add the screening
                boolean success = screeningDao.addScreening(movieId, venueId, roomId, price, screeningDate, startTime, endTime);
                
                if (success) {
                    JOptionPane.showMessageDialog(addDialog, "Screening added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    addDialog.dispose();
                    loadScreeningData(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(addDialog, "Failed to add screening!", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(addDialog, 
                    "Invalid date or time format!\nDate: YYYY-MM-DD (e.g., 2024-01-15)\nTime: HH:MM (e.g., 14:30)", 
                    "Format Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addDialog, "Price must be a valid number!", "Format Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(addDialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> addDialog.dispose());
        
        addDialog.add(formPanel, BorderLayout.CENTER);
        addDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        addDialog.setVisible(true);
    }
    
    private void cancelScreening() {
        int selectedRow = screeningTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a screening to cancel!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int screeningId = (int) tableModel.getValueAt(selectedRow, 0);
        String movieName = (String) tableModel.getValueAt(selectedRow, 1);
        String status = (String) tableModel.getValueAt(selectedRow, 8);
        
        if ("Cancelled".equals(status)) {
            JOptionPane.showMessageDialog(this, "This screening is already cancelled!", 
                "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "WARNING: This will cancel screening for: " + movieName + "\n\n" +
            "This action will also cancel ALL ticket bookings for this screening.\n" +
            "Customers will need to request refunds for cancelled tickets.\n\n" +
            "Are you sure you want to proceed?",
            "Confirm Cancellation - WARNING",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean cancelled = screeningDao.cancelScreening(screeningId);
            if (cancelled) {
                JOptionPane.showMessageDialog(this, 
                    "Screening and all associated tickets cancelled successfully!",
                    "Cancellation Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadScreeningData();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error cancelling screening!",
                    "Cancellation Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void completeScreening() {
        int selectedRow = screeningTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a screening to mark as complete!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int screeningId = (int) tableModel.getValueAt(selectedRow, 0);
        String movieName = (String) tableModel.getValueAt(selectedRow, 1);
        String status = (String) tableModel.getValueAt(selectedRow, 8);
        
        if ("Completed".equals(status)) {
            JOptionPane.showMessageDialog(this, "This screening is already completed!", 
                "Already Completed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        if ("Cancelled".equals(status)) {
            JOptionPane.showMessageDialog(this, "Cannot complete a cancelled screening!", 
                "Invalid Operation", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Mark screening as completed: " + movieName + "?\n\n" +
            "This will update the screening status to 'Completed'.\n" +
            "Ticket bookings will remain unchanged.",
            "Confirm Complete Screening",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            screeningDao.updateScreeningStatus(screeningId, "Completed");
            JOptionPane.showMessageDialog(this, "Screening marked as completed successfully!");
            loadScreeningData();
        }
    }
    
    // Helper methods for combo boxes
    private void loadMoviesIntoCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        ArrayList<Movie> movies = movieDao.getAllMovies();
        for (Movie movie : movies) {
            combo.addItem(movie.getMovieId() + ": " + movie.getMovieName());
        }
    }
    
    private void loadVenuesIntoCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        ArrayList<Venues> venues = venuesDao.getAllVenues();
        for (Venues venue : venues) {
            combo.addItem(venue.getVenue_id() + ": " + venue.getVenue_name());
        }
    }
    
    private void loadRoomsIntoCombo(JComboBox<String> combo, int venueId) {
        combo.removeAllItems();
        ArrayList<Room> rooms = venuesDao.getRoomsByVenue(venueId);
        for (Room room : rooms) {
            combo.addItem(room.getRoomId() + ": " + room.getRoomName() + " (" + room.getRoomType() + ")");
        }
    }
    
    private int extractIdFromCombo(String comboText) {
        try {
            return Integer.parseInt(comboText.split(":")[0].trim());
        } catch (Exception e) {
            return -1;
        }
    }
}
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
        JButton editButton = new JButton("Edit Screening");
        JButton cancelButton = new JButton("Cancel Screening");
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back to Admin Panel");
        
        // Style buttons
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        cancelButton.setFont(buttonFont);
        refreshButton.setFont(buttonFont);
        backButton.setFont(buttonFont);
        
        // Add action listeners
        addButton.addActionListener(e -> addScreening());
        editButton.addActionListener(e -> editScreening());
        cancelButton.addActionListener(e -> cancelScreening());
        refreshButton.addActionListener(e -> loadScreeningData());
        backButton.addActionListener(e -> mainGUI.showScreen("ADMIN PANEL"));
        
        // Show/hide buttons based on account type
        if (!"Admin".equals(accountType)) {
            addButton.setVisible(false);
            editButton.setVisible(false);
            cancelButton.setVisible(false);
        }
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        
        // Add components to panel
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
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
            // Validation and saving logic would go here
            JOptionPane.showMessageDialog(addDialog, "Add screening functionality to be implemented");
        });
        
        cancelButton.addActionListener(e -> addDialog.dispose());
        
        addDialog.add(formPanel, BorderLayout.CENTER);
        addDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        addDialog.setVisible(true);
    }
    
    private void editScreening() {
        int selectedRow = screeningTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a screening to edit!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int screeningId = (int) tableModel.getValueAt(selectedRow, 0);
        Screening screening = screeningDao.getScreeningById(screeningId);
        
        if (screening == null) {
            JOptionPane.showMessageDialog(this, "Screening not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Similar to addScreening but with pre-filled values
        JOptionPane.showMessageDialog(this, "Edit screening functionality to be implemented");
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
            "Are you sure you want to cancel screening for: " + movieName + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            screeningDao.updateScreeningStatus(screeningId, "Cancelled");
            JOptionPane.showMessageDialog(this, "Screening cancelled successfully!");
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
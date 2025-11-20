import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VenueManagementDisplay extends JPanel {
    private MainGUI mainGUI;
    private VenuesDao venuesDao;
    private JTable venueTable;
    private DefaultTableModel tableModel;
    
    public VenueManagementDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        this.venuesDao = new VenuesDao();
        setLayout(new BorderLayout());
        initializeComponents();
        loadVenueData();
    }
    
    private void initializeComponents() {
        // Header
        JLabel header = new JLabel("Venue Management", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Table for displaying venues
        String[] columnNames = {"ID", "Venue Name", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        venueTable = new JTable(tableModel);
        venueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        venueTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        venueTable.setFont(new Font("Arial", Font.PLAIN, 12));
        venueTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(venueTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Venues List"));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                searchVenues(searchTerm);
            } else {
                loadVenueData();
            }
        });
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton addButton = new JButton("Add Venue");
        JButton editButton = new JButton("Edit Venue");
        JButton detailsButton = new JButton("View Details");
        JButton refreshButton = new JButton("Refresh All");
        JButton backButton = new JButton("Back to Admin Panel");
        
        // Style buttons
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        detailsButton.setFont(buttonFont);
        refreshButton.setFont(buttonFont);
        backButton.setFont(buttonFont);
        
        addButton.addActionListener(e -> addVenue());
        editButton.addActionListener(e -> editVenue());
        detailsButton.addActionListener(e -> viewVenueDetails());
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadVenueData();
        });
        backButton.addActionListener(e -> mainGUI.showScreen("ADMIN PANEL"));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(detailsButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.SOUTH);
    }
    
    private void loadVenueData() {
        tableModel.setRowCount(0);
        
        ArrayList<Venues> venues = venuesDao.getAllVenues();
        for (Venues venue : venues) {
            tableModel.addRow(new Object[]{
                venue.getVenue_id(),
                venue.getVenue_name(),
                venue.getAddress()
            });
        }
    }
    
    private void searchVenues(String searchTerm) {
        tableModel.setRowCount(0);
        
        ArrayList<Venues> venues = venuesDao.searchVenueName(searchTerm);
        if (venues.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No venues found matching: " + searchTerm, 
                "Search Results", JOptionPane.INFORMATION_MESSAGE);
        }
        
        for (Venues venue : venues) {
            tableModel.addRow(new Object[]{
                venue.getVenue_id(),
                venue.getVenue_name(),
                venue.getAddress()
            });
        }
    }
    
    private void addVenue() {
        JDialog addDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Venue", true);
        addDialog.setLayout(new GridLayout(4, 2, 10, 10));
        addDialog.setSize(400, 200);
        addDialog.setLocationRelativeTo(this);
        
        JTextField nameField = new JTextField();
        JTextField addressField = new JTextField();
        
        addDialog.add(new JLabel("Venue Name:"));
        addDialog.add(nameField);
        addDialog.add(new JLabel("Address:"));
        addDialog.add(addressField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            if (validateVenueInput(nameField.getText(), addressField.getText())) {
                Venues newVenue = new Venues(
                    0,
                    nameField.getText(),
                    addressField.getText()
                );
                
                boolean added = venuesDao.addVenue(newVenue);
                if (added) {
                    JOptionPane.showMessageDialog(addDialog, "Venue added successfully!");
                    loadVenueData();
                    addDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(addDialog, "Error adding venue!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> addDialog.dispose());
        
        addDialog.add(saveButton);
        addDialog.add(cancelButton);
        
        addDialog.setVisible(true);
    }
    
    private void editVenue() {
        int selectedRow = venueTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a venue to edit!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int venueId = (int) tableModel.getValueAt(selectedRow, 0);
        Venues venue = venuesDao.getVenueById(venueId);
        
        if (venue == null) {
            JOptionPane.showMessageDialog(this, "Venue not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Venue", true);
        editDialog.setLayout(new GridLayout(4, 2, 10, 10));
        editDialog.setSize(400, 200);
        editDialog.setLocationRelativeTo(this);
        
        JTextField nameField = new JTextField(venue.getVenue_name());
        JTextField addressField = new JTextField(venue.getAddress());
        
        editDialog.add(new JLabel("Venue Name:"));
        editDialog.add(nameField);
        editDialog.add(new JLabel("Address:"));
        editDialog.add(addressField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            if (validateVenueInput(nameField.getText(), addressField.getText())) {
                if (nameField.getText().equals(venue.getVenue_name())) {
                    JOptionPane.showMessageDialog(editDialog, 
                        "The new name cannot be the same as the old name!", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                ArrayList<Venues> existing = venuesDao.searchVenueName(nameField.getText());
                boolean sameName = false;
                for (Venues v : existing) {
                    if (v.getVenue_id() != venueId) {
                        sameName = true;
                        break;
                    }
                }
                
                if (sameName) {
                    JOptionPane.showMessageDialog(editDialog, 
                        "Another venue already has this name!", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                venue.setVenue_name(nameField.getText());
                venue.setAddress(addressField.getText());
                
                boolean updated = venuesDao.updateVenue(venue);
                if (updated) {
                    JOptionPane.showMessageDialog(editDialog, "Venue updated successfully!");
                    loadVenueData();
                    editDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Error updating venue!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> editDialog.dispose());
        
        editDialog.add(saveButton);
        editDialog.add(cancelButton);
        
        editDialog.setVisible(true);
    }
    
    private void viewVenueDetails() {
        int selectedRow = venueTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a venue to view details!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int venueId = (int) tableModel.getValueAt(selectedRow, 0);
        String venueName = (String) tableModel.getValueAt(selectedRow, 1);
        
        JDialog detailsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Venue Details: " + venueName, true);
        detailsDialog.setLayout(new BorderLayout());
        detailsDialog.setSize(500, 400);
        detailsDialog.setLocationRelativeTo(this);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Movies tab
        JPanel moviesPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> moviesModel = new DefaultListModel<>();
        JList<String> moviesList = new JList<>(moviesModel);
        loadMoviesAtVenue(venueId, moviesModel);
        moviesPanel.add(new JLabel("Movies Screening Here:"), BorderLayout.NORTH);
        moviesPanel.add(new JScrollPane(moviesList), BorderLayout.CENTER);
        tabbedPane.addTab("Movies", moviesPanel);
        
        // Rooms tab
        JPanel roomsPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> roomsModel = new DefaultListModel<>();
        JList<String> roomsList = new JList<>(roomsModel);
        loadRoomsAtVenue(venueId, roomsModel);
        roomsPanel.add(new JLabel("Rooms:"), BorderLayout.NORTH);
        roomsPanel.add(new JScrollPane(roomsList), BorderLayout.CENTER);
        tabbedPane.addTab("Rooms", roomsPanel);
        
        // Screenings tab
        JPanel screeningsPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> screeningsModel = new DefaultListModel<>();
        JList<String> screeningsList = new JList<>(screeningsModel);
        loadScreeningsAtVenue(venueId, screeningsModel);
        screeningsPanel.add(new JLabel("Screening Schedule:"), BorderLayout.NORTH);
        screeningsPanel.add(new JScrollPane(screeningsList), BorderLayout.CENTER);
        tabbedPane.addTab("Screenings", screeningsPanel);
        
        // Ticket Sales tab
        JPanel salesPanel = new JPanel(new BorderLayout());
        int ticketsSold = venuesDao.getTicketsSoldByVenue(venueId);
        JLabel salesLabel = new JLabel("Total Tickets Sold: " + ticketsSold, JLabel.CENTER);
        salesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        salesPanel.add(salesLabel, BorderLayout.CENTER);
        tabbedPane.addTab("Ticket Sales", salesPanel);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> detailsDialog.dispose());
        
        detailsDialog.add(tabbedPane, BorderLayout.CENTER);
        detailsDialog.add(closeButton, BorderLayout.SOUTH);
        
        detailsDialog.setVisible(true);
    }
    
    private void loadMoviesAtVenue(int venueId, DefaultListModel<String> model) {
        model.clear();
        ArrayList<String> movies = venuesDao.getMoviesByVenue(venueId);
        if (movies.isEmpty()) {
            model.addElement("No movies scheduled at this venue.");
        } else {
            for (String movie : movies) {
                model.addElement(movie);
            }
        }
    }
    
    private void loadRoomsAtVenue(int venueId, DefaultListModel<String> model) {
        model.clear();
        ArrayList<Room> rooms = venuesDao.getRoomsByVenue(venueId);
        if (rooms.isEmpty()) {
            model.addElement("No rooms found in this venue.");
        } else {
            for (Room room : rooms) {
                model.addElement(room.toString());
            }
        }
    }
    
    private void loadScreeningsAtVenue(int venueId, DefaultListModel<String> model) {
        model.clear();
        ArrayList<String> screenings = venuesDao.getScreeningsByVenue(venueId);
        if (screenings.isEmpty()) {
            model.addElement("No screenings scheduled at this venue.");
        } else {
            for (String screening : screenings) {
                model.addElement(screening);
            }
        }
    }
    
    private boolean validateVenueInput(String name, String address) {
        if (name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Venue name cannot be empty!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (address.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Address cannot be empty!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VenueManagementDisplay extends JPanel {
    private MainGUI mainGUI;
    private VenuesDao venuesDao;
    private JTable venueTable;
    private DefaultTableModel tableModel;
    
    public VenueManagementDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        this.venuesDao = new VenuesDao();
        setLayout(new BorderLayout());
        initializeComponents();
        loadVenueData();
    }
    
    private void initializeComponents() {
        // Header
        JLabel header = new JLabel("Venue Management", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Table for displaying venues
        String[] columnNames = {"ID", "Venue Name", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        venueTable = new JTable(tableModel);
        venueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        venueTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        venueTable.setFont(new Font("Arial", Font.PLAIN, 12));
        venueTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(venueTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Venues List"));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                searchVenues(searchTerm);
            } else {
                loadVenueData();
            }
        });
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton addButton = new JButton("Add Venue");
        JButton editButton = new JButton("Edit Venue");
        JButton detailsButton = new JButton("View Details");
        JButton refreshButton = new JButton("Refresh All");
        JButton backButton = new JButton("Back to Admin Panel");
        
        // Style buttons
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        detailsButton.setFont(buttonFont);
        refreshButton.setFont(buttonFont);
        backButton.setFont(buttonFont);
        
        addButton.addActionListener(e -> addVenue());
        editButton.addActionListener(e -> editVenue());
        detailsButton.addActionListener(e -> viewVenueDetails());
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadVenueData();
        });
        backButton.addActionListener(e -> mainGUI.showScreen("ADMIN PANEL"));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(detailsButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.SOUTH);
    }
    
    private void loadVenueData() {
        tableModel.setRowCount(0);
        
        ArrayList<Venues> venues = venuesDao.getAllVenues();
        for (Venues venue : venues) {
            tableModel.addRow(new Object[]{
                venue.getVenue_id(),
                venue.getVenue_name(),
                venue.getAddress()
            });
        }
    }
    
    private void searchVenues(String searchTerm) {
        tableModel.setRowCount(0);
        
        ArrayList<Venues> venues = venuesDao.searchVenueName(searchTerm);
        if (venues.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No venues found matching: " + searchTerm, 
                "Search Results", JOptionPane.INFORMATION_MESSAGE);
        }
        
        for (Venues venue : venues) {
            tableModel.addRow(new Object[]{
                venue.getVenue_id(),
                venue.getVenue_name(),
                venue.getAddress()
            });
        }
    }
    
    private void addVenue() {
        JDialog addDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Venue", true);
        addDialog.setLayout(new GridLayout(4, 2, 10, 10));
        addDialog.setSize(400, 200);
        addDialog.setLocationRelativeTo(this);
        
        JTextField nameField = new JTextField();
        JTextField addressField = new JTextField();
        
        addDialog.add(new JLabel("Venue Name:"));
        addDialog.add(nameField);
        addDialog.add(new JLabel("Address:"));
        addDialog.add(addressField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            if (validateVenueInput(nameField.getText(), addressField.getText())) {
                Venues newVenue = new Venues(
                    0,
                    nameField.getText(),
                    addressField.getText()
                );
                
                boolean added = venuesDao.addVenue(newVenue);
                if (added) {
                    JOptionPane.showMessageDialog(addDialog, "Venue added successfully!");
                    loadVenueData();
                    addDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(addDialog, "Error adding venue!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> addDialog.dispose());
        
        addDialog.add(saveButton);
        addDialog.add(cancelButton);
        
        addDialog.setVisible(true);
    }
    
    private void editVenue() {
        int selectedRow = venueTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a venue to edit!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int venueId = (int) tableModel.getValueAt(selectedRow, 0);
        Venues venue = venuesDao.getVenueById(venueId);
        
        if (venue == null) {
            JOptionPane.showMessageDialog(this, "Venue not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Venue", true);
        editDialog.setLayout(new GridLayout(4, 2, 10, 10));
        editDialog.setSize(400, 200);
        editDialog.setLocationRelativeTo(this);
        
        JTextField nameField = new JTextField(venue.getVenue_name());
        JTextField addressField = new JTextField(venue.getAddress());
        
        editDialog.add(new JLabel("Venue Name:"));
        editDialog.add(nameField);
        editDialog.add(new JLabel("Address:"));
        editDialog.add(addressField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            if (validateVenueInput(nameField.getText(), addressField.getText())) {
                if (nameField.getText().equals(venue.getVenue_name())) {
                    JOptionPane.showMessageDialog(editDialog, 
                        "The new name cannot be the same as the old name!", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                ArrayList<Venues> existing = venuesDao.searchVenueName(nameField.getText());
                boolean sameName = false;
                for (Venues v : existing) {
                    if (v.getVenue_id() != venueId) {
                        sameName = true;
                        break;
                    }
                }
                
                if (sameName) {
                    JOptionPane.showMessageDialog(editDialog, 
                        "Another venue already has this name!", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                venue.setVenue_name(nameField.getText());
                venue.setAddress(addressField.getText());
                
                boolean updated = venuesDao.updateVenue(venue);
                if (updated) {
                    JOptionPane.showMessageDialog(editDialog, "Venue updated successfully!");
                    loadVenueData();
                    editDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Error updating venue!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> editDialog.dispose());
        
        editDialog.add(saveButton);
        editDialog.add(cancelButton);
        
        editDialog.setVisible(true);
    }
    
    private void viewVenueDetails() {
        int selectedRow = venueTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a venue to view details!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int venueId = (int) tableModel.getValueAt(selectedRow, 0);
        String venueName = (String) tableModel.getValueAt(selectedRow, 1);
        
        JDialog detailsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Venue Details: " + venueName, true);
        detailsDialog.setLayout(new BorderLayout());
        detailsDialog.setSize(500, 400);
        detailsDialog.setLocationRelativeTo(this);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Movies tab
        JPanel moviesPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> moviesModel = new DefaultListModel<>();
        JList<String> moviesList = new JList<>(moviesModel);
        loadMoviesAtVenue(venueId, moviesModel);
        moviesPanel.add(new JLabel("Movies Screening Here:"), BorderLayout.NORTH);
        moviesPanel.add(new JScrollPane(moviesList), BorderLayout.CENTER);
        tabbedPane.addTab("Movies", moviesPanel);
        
        // Rooms tab
        JPanel roomsPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> roomsModel = new DefaultListModel<>();
        JList<String> roomsList = new JList<>(roomsModel);
        loadRoomsAtVenue(venueId, roomsModel);
        roomsPanel.add(new JLabel("Rooms:"), BorderLayout.NORTH);
        roomsPanel.add(new JScrollPane(roomsList), BorderLayout.CENTER);
        tabbedPane.addTab("Rooms", roomsPanel);
        
        // Screenings tab
        JPanel screeningsPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> screeningsModel = new DefaultListModel<>();
        JList<String> screeningsList = new JList<>(screeningsModel);
        loadScreeningsAtVenue(venueId, screeningsModel);
        screeningsPanel.add(new JLabel("Screening Schedule:"), BorderLayout.NORTH);
        screeningsPanel.add(new JScrollPane(screeningsList), BorderLayout.CENTER);
        tabbedPane.addTab("Screenings", screeningsPanel);
        
        // Ticket Sales tab
        JPanel salesPanel = new JPanel(new BorderLayout());
        int ticketsSold = venuesDao.getTicketsSoldByVenue(venueId);
        JLabel salesLabel = new JLabel("Total Tickets Sold: " + ticketsSold, JLabel.CENTER);
        salesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        salesPanel.add(salesLabel, BorderLayout.CENTER);
        tabbedPane.addTab("Ticket Sales", salesPanel);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> detailsDialog.dispose());
        
        detailsDialog.add(tabbedPane, BorderLayout.CENTER);
        detailsDialog.add(closeButton, BorderLayout.SOUTH);
        
        detailsDialog.setVisible(true);
    }
    
    private void loadMoviesAtVenue(int venueId, DefaultListModel<String> model) {
        model.clear();
        ArrayList<String> movies = venuesDao.getMoviesByVenue(venueId);
        if (movies.isEmpty()) {
            model.addElement("No movies scheduled at this venue.");
        } else {
            for (String movie : movies) {
                model.addElement(movie);
            }
        }
    }
    
    private void loadRoomsAtVenue(int venueId, DefaultListModel<String> model) {
        model.clear();
        ArrayList<Room> rooms = venuesDao.getRoomsByVenue(venueId);
        if (rooms.isEmpty()) {
            model.addElement("No rooms found in this venue.");
        } else {
            for (Room room : rooms) {
                model.addElement(room.toString());
            }
        }
    }
    
    private void loadScreeningsAtVenue(int venueId, DefaultListModel<String> model) {
        model.clear();
        ArrayList<String> screenings = venuesDao.getScreeningsByVenue(venueId);
        if (screenings.isEmpty()) {
            model.addElement("No screenings scheduled at this venue.");
        } else {
            for (String screening : screenings) {
                model.addElement(screening);
            }
        }
    }
    
    private boolean validateVenueInput(String name, String address) {
        if (name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Venue name cannot be empty!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (address.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Address cannot be empty!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}

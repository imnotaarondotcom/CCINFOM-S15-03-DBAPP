import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class RoomManagementDisplay extends JPanel {
    private MainGUI mainGUI;
    private RoomDao roomDao;
    private JTable roomTable;
    private DefaultTableModel tableModel;
    
    public RoomManagementDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        this.roomDao = new RoomDao();
        setLayout(new BorderLayout());
        initializeComponents();
        loadRoomData();
    }
    
    private void initializeComponents() {
        // Header
        JLabel header = new JLabel("Room Management", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Table for displaying rooms
        String[] columnNames = {"Room ID", "Room Name", "Room Type", "Venue ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        roomTable = new JTable(tableModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        roomTable.setFont(new Font("Arial", Font.PLAIN, 12));
        roomTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Rooms List"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton addButton = new JButton("Add Room");
        JButton editButton = new JButton("Edit Room");
        JButton deleteButton = new JButton("Delete Room");
        JButton reportsButton = new JButton("View Reports");
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back to Admin Panel");
        
        // Style buttons
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        reportsButton.setFont(buttonFont);
        refreshButton.setFont(buttonFont);
        backButton.setFont(buttonFont);
        
        // Add action listeners
        addButton.addActionListener(e -> addRoom());
        editButton.addActionListener(e -> editRoom());
        deleteButton.addActionListener(e -> deleteRoom());
        reportsButton.addActionListener(e -> showReportsDialog());
        refreshButton.addActionListener(e -> loadRoomData());
        backButton.addActionListener(e -> mainGUI.showScreen("ADMIN PANEL"));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(reportsButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        
        // Add components to panel
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadRoomData() {
        tableModel.setRowCount(0);
        
        ArrayList<Room> rooms = roomDao.getAllRooms();
        for (Room room : rooms) {
            tableModel.addRow(new Object[]{
                room.getRoomId(),
                room.getRoomName(),
                room.getRoomType(),
                room.getVenueId()
            });
        }
    }
    
    private void addRoom() {
        JDialog addDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Room", true);
        addDialog.setLayout(new GridLayout(5, 2, 10, 10));
        addDialog.setSize(400, 250);
        addDialog.setLocationRelativeTo(this);
        
        JTextField nameField = new JTextField();
        JTextField typeField = new JTextField();
        JTextField venueIdField = new JTextField();
        
        addDialog.add(new JLabel("Room Name:"));
        addDialog.add(nameField);
        addDialog.add(new JLabel("Room Type (IMAX, 3D, Standard):"));
        addDialog.add(typeField);
        addDialog.add(new JLabel("Venue ID:"));
        addDialog.add(venueIdField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            if (validateRoomInput(nameField.getText(), typeField.getText(), venueIdField.getText())) {
                try {
                    roomDao.addRoom(
                        nameField.getText(),
                        typeField.getText(),
                        Integer.parseInt(venueIdField.getText())
                    );
                    JOptionPane.showMessageDialog(addDialog, "Room added successfully!");
                    loadRoomData();
                    addDialog.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(addDialog, "Venue ID must be a number!", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> addDialog.dispose());
        
        addDialog.add(saveButton);
        addDialog.add(cancelButton);
        
        addDialog.setVisible(true);
    }
    
    private void editRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room to edit!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int roomId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentType = (String) tableModel.getValueAt(selectedRow, 2);
        
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Room", true);
        editDialog.setLayout(new GridLayout(4, 2, 10, 10));
        editDialog.setSize(400, 200);
        editDialog.setLocationRelativeTo(this);
        
        JTextField nameField = new JTextField(currentName);
        JTextField typeField = new JTextField(currentType);
        
        editDialog.add(new JLabel("Room Name:"));
        editDialog.add(nameField);
        editDialog.add(new JLabel("Room Type:"));
        editDialog.add(typeField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            if (validateRoomInput(nameField.getText(), typeField.getText(), "1")) { // Venue ID not needed for edit
                roomDao.updateRoom(roomId, nameField.getText(), typeField.getText());
                JOptionPane.showMessageDialog(editDialog, "Room updated successfully!");
                loadRoomData();
                editDialog.dispose();
            }
        });
        
        cancelButton.addActionListener(e -> editDialog.dispose());
        
        editDialog.add(saveButton);
        editDialog.add(cancelButton);
        
        editDialog.setVisible(true);
    }
    
    private void deleteRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room to delete!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int roomId = (int) tableModel.getValueAt(selectedRow, 0);
        String roomName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to delete room: " + roomName + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            roomDao.deleteRoom(roomId);
            JOptionPane.showMessageDialog(this, "Room deleted successfully!");
            loadRoomData();
        }
    }
    
    private void showReportsDialog() {
        JDialog reportsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Room Management Reports", true);
        reportsDialog.setLayout(new BorderLayout());
        reportsDialog.setSize(700, 500);
        reportsDialog.setLocationRelativeTo(this);
        
        // Create tabbed pane for different reports
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Ticket Count Report
        JPanel ticketCountPanel = createReportPanel("Rooms with Ticket Count", 
            roomDao.getRoomsWithTicketCount());
        tabbedPane.addTab("Ticket Count", ticketCountPanel);
        
        // Movie Info Report
        JPanel movieInfoPanel = createReportPanel("Rooms with Movie Schedules", 
            roomDao.getRoomsWithMovieInfo());
        tabbedPane.addTab("Movie Schedules", movieInfoPanel);
        
        // Movie Details Report
        JPanel movieDetailsPanel = createReportPanel("Movie Details & Ratings", 
            roomDao.getMovieDetailsForScreenings());
        tabbedPane.addTab("Movie Details", movieDetailsPanel);
        
        // Revenue Report
        JPanel revenuePanel = createReportPanel("Revenue by Room", 
            roomDao.getRevenueByRoom());
        tabbedPane.addTab("Revenue", revenuePanel);
        
        // Capacity vs Attendance Report
        JPanel capacityPanel = createReportPanel("Venue Capacity vs Attendance", 
            roomDao.getVenueCapacityVsAttendance());
        tabbedPane.addTab("Capacity", capacityPanel);
        
        // Screenings Report
        JPanel screeningsPanel = createReportPanel("Total Screenings by Venue", 
            roomDao.getTotalScreeningsByVenue());
        tabbedPane.addTab("Screenings", screeningsPanel);
        
        // Vacant Seats Panel (special case - needs screening ID input)
        JPanel vacantSeatsPanel = createVacantSeatsPanel();
        tabbedPane.addTab("Vacant Seats", vacantSeatsPanel);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> reportsDialog.dispose());
        
        reportsDialog.add(tabbedPane, BorderLayout.CENTER);
        reportsDialog.add(closeButton, BorderLayout.SOUTH);
        
        reportsDialog.setVisible(true);
    }
    
    private JPanel createReportPanel(String title, ArrayList<String> data) {
        JPanel panel = new JPanel(new BorderLayout());
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> reportList = new JList<>(listModel);
        reportList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        if (data.isEmpty()) {
            listModel.addElement("No data available.");
        } else {
            for (String item : data) {
                listModel.addElement(item);
            }
        }
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportList), BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createVacantSeatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel inputPanel = new JPanel(new FlowLayout());
        JTextField screeningIdField = new JTextField(10);
        JButton searchButton = new JButton("Search Vacant Seats");
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> seatsList = new JList<>(listModel);
        seatsList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        searchButton.addActionListener(e -> {
            try {
                int screeningId = Integer.parseInt(screeningIdField.getText().trim());
                ArrayList<String> vacantSeats = roomDao.getVacantSeatsForScreening(screeningId);
                
                listModel.clear();
                if (vacantSeats.isEmpty()) {
                    listModel.addElement("No vacant seats found or screening doesn't exist.");
                } else {
                    listModel.addElement("Vacant Seats for Screening ID: " + screeningId);
                    listModel.addElement("");
                    for (String seat : vacantSeats) {
                        listModel.addElement(seat);
                    }
                    listModel.addElement("");
                    listModel.addElement("Total vacant seats: " + vacantSeats.size());
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid Screening ID!", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        inputPanel.add(new JLabel("Screening ID:"));
        inputPanel.add(screeningIdField);
        inputPanel.add(searchButton);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(seatsList), BorderLayout.CENTER);
        
        return panel;
    }
    
    private boolean validateRoomInput(String name, String type, String venueId) {
        if (name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room name cannot be empty!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (type.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room type cannot be empty!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (venueId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Venue ID cannot be empty!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            Integer.parseInt(venueId);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Venue ID must be a number!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SeatManagementDisplay extends JPanel {
    private MainGUI mainGUI;
    private SeatDao seatDao;
    private JTable seatTable;
    private DefaultTableModel tableModel;
    private JComboBox<Integer> roomComboBox;
    
    public SeatManagementDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        this.seatDao = new SeatDao();
        setLayout(new BorderLayout());
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Header
        JLabel header = new JLabel("Seat Management", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Room selection panel
        JPanel roomSelectionPanel = new JPanel(new FlowLayout());
        roomSelectionPanel.add(new JLabel("Select Room ID:"));
        
        roomComboBox = new JComboBox<>();
        roomComboBox.setPreferredSize(new Dimension(100, 25));
        loadRoomIds(); // Populate with available room IDs
        
        JButton loadSeatsButton = new JButton("Load Seats");
        loadSeatsButton.addActionListener(e -> loadSeatsForRoom());
        
        roomSelectionPanel.add(roomComboBox);
        roomSelectionPanel.add(loadSeatsButton);
        
        // Table for displaying seats
        String[] columnNames = {"Seat ID", "Seat Number", "Room ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        seatTable = new JTable(tableModel);
        seatTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        seatTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        seatTable.setFont(new Font("Arial", Font.PLAIN, 12));
        seatTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(seatTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Seats List"));
        scrollPane.setPreferredSize(new Dimension(500, 300));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton addSeatsButton = new JButton("Add Seats");
        JButton deleteSeatButton = new JButton("Delete Selected Seat");
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back to Admin Panel");
        
        // Style buttons
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        addSeatsButton.setFont(buttonFont);
        deleteSeatButton.setFont(buttonFont);
        refreshButton.setFont(buttonFont);
        backButton.setFont(buttonFont);
        
        // Add action listeners
        addSeatsButton.addActionListener(e -> addSeats());
        deleteSeatButton.addActionListener(e -> deleteSeat());
        refreshButton.addActionListener(e -> loadSeatsForRoom());
        backButton.addActionListener(e -> mainGUI.showScreen("ADMIN PANEL"));
        
        buttonPanel.add(addSeatsButton);
        buttonPanel.add(deleteSeatButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        
        // Add components to panel
        add(header, BorderLayout.NORTH);
        add(roomSelectionPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadRoomIds() {
        // This method would need to get available room IDs from the database
        // For now, we'll use a placeholder. You might need to add a method to RoomDao to get all room IDs
        roomComboBox.removeAllItems();
        
        // Placeholder - you'll need to implement this based on your database structure
        // roomComboBox.addItem(1);
        // roomComboBox.addItem(2);
        // etc.
        
        // Temporary: Add some example room IDs
        for (int i = 1; i <= 10; i++) {
            roomComboBox.addItem(i);
        }
    }
    
    private void loadSeatsForRoom() {
        if (roomComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a room first!", 
                "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int roomId = (Integer) roomComboBox.getSelectedItem();
        tableModel.setRowCount(0); // Clear existing data
        
        ArrayList<Seat> seats = seatDao.getSeatsByRoom(roomId);
        if (seats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No seats found for Room ID: " + roomId, 
                "No Seats", JOptionPane.INFORMATION_MESSAGE);
        }
        
        for (Seat seat : seats) {
            tableModel.addRow(new Object[]{
                seat.getSeatId(),
                seat.getSeatNo(),
                seat.getRoomId()
            });
        }
    }
    
    private void addSeats() {
        if (roomComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a room first!", 
                "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int roomId = (Integer) roomComboBox.getSelectedItem();
        
        JDialog addDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Seats", true);
        addDialog.setLayout(new GridLayout(3, 2, 10, 10));
        addDialog.setSize(300, 150);
        addDialog.setLocationRelativeTo(this);
        
        JLabel roomLabel = new JLabel("Room ID: " + roomId);
        JTextField countField = new JTextField();
        
        addDialog.add(new JLabel("Target Room:"));
        addDialog.add(roomLabel);
        addDialog.add(new JLabel("Number of Seats to Add:"));
        addDialog.add(countField);
        
        JButton saveButton = new JButton("Add Seats");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                int count = Integer.parseInt(countField.getText().trim());
                if (count <= 0) {
                    JOptionPane.showMessageDialog(addDialog, "Please enter a positive number!", 
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success = seatDao.addSeats(roomId, count);
                if (success) {
                    JOptionPane.showMessageDialog(addDialog, 
                        count + " seats added successfully to Room " + roomId + "!");
                    loadSeatsForRoom(); // Refresh the table
                    addDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(addDialog, "Failed to add seats!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addDialog, "Please enter a valid number!", 
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> addDialog.dispose());
        
        addDialog.add(saveButton);
        addDialog.add(cancelButton);
        
        addDialog.setVisible(true);
    }
    
    private void deleteSeat() {
        int selectedRow = seatTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a seat to delete!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int seatId = (int) tableModel.getValueAt(selectedRow, 0);
        String seatNumber = (String) tableModel.getValueAt(selectedRow, 1);
        int roomId = (int) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to delete Seat " + seatNumber + " from Room " + roomId + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            seatDao.deleteSeat(seatId);
            JOptionPane.showMessageDialog(this, "Seat deleted successfully!");
            loadSeatsForRoom(); // Refresh the table
        }
    }
}
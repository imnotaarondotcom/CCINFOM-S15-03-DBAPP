import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MovieManagementDisplay extends JPanel {
    private MainGUI mainGUI;
    private MovieDao movieDao;
    private JTable movieTable;
    private DefaultTableModel tableModel;
    
    public MovieManagementDisplay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
        this.movieDao = new MovieDao();
        setLayout(new BorderLayout());
        initializeComponents();
        loadMovieData();
    }
    
    private void initializeComponents() {
        // Header
        JLabel header = new JLabel("Movie Management", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Table for displaying movies
        String[] columnNames = {"ID", "Movie Name", "Genre", "Age Rating", "Duration (min)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        movieTable = new JTable(tableModel);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        movieTable.setFont(new Font("Arial", Font.PLAIN, 12));
        movieTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(movieTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Movies List"));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                searchMovies(searchTerm);
            } else {
                loadMovieData(); // Reload all if search is empty
            }
        });
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton addButton = new JButton("Add Movie");
        JButton editButton = new JButton("Edit Movie");
        JButton deleteButton = new JButton("Delete Movie");
        JButton refreshButton = new JButton("Refresh All");
        JButton backButton = new JButton("Back to Admin Panel");
        
        // Style buttons
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        refreshButton.setFont(buttonFont);
        backButton.setFont(buttonFont);
        
        // Add action listeners
        addButton.addActionListener(e -> addMovie());
        editButton.addActionListener(e -> editMovie());
        deleteButton.addActionListener(e -> deleteMovie());
        refreshButton.addActionListener(e -> {
            searchField.setText(""); // Clear search field
            loadMovieData();
        });
        backButton.addActionListener(e -> mainGUI.showScreen("ADMIN PANEL"));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        
        // Add components to panel
        add(header, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Reorganize layout
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.SOUTH);
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private void loadMovieData() {
        tableModel.setRowCount(0); // Clear existing data
        
        ArrayList<Movie> movies = movieDao.getAllMovies();
        for (Movie movie : movies) {
            tableModel.addRow(new Object[]{
                movie.getMovieId(),
                movie.getMovieName(),
                movie.getGenre(),
                movie.getAgeRating(),
                movie.getDuration()
            });
        }
    }
    
    private void searchMovies(String searchTerm) {
        tableModel.setRowCount(0); // Clear existing data
        
        ArrayList<Movie> movies = movieDao.searchMovieName(searchTerm);
        if (movies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No movies found matching: " + searchTerm, "Search Results", JOptionPane.INFORMATION_MESSAGE);
        }
        
        for (Movie movie : movies) {
            tableModel.addRow(new Object[]{
                movie.getMovieId(),
                movie.getMovieName(),
                movie.getGenre(),
                movie.getAgeRating(),
                movie.getDuration()
            });
        }
    }
    
    private void addMovie() {
        JDialog addDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Movie", true);
        addDialog.setLayout(new GridLayout(6, 2, 10, 10));
        addDialog.setSize(400, 300);
        addDialog.setLocationRelativeTo(this);
        
        JTextField nameField = new JTextField();
        JTextField genreField = new JTextField();
        JComboBox<String> ratingCombo = new JComboBox<>(new String[]{"G", "PG", "PG-13", "R"});
        JTextField durationField = new JTextField();
        
        addDialog.add(new JLabel("Movie Name:"));
        addDialog.add(nameField);
        addDialog.add(new JLabel("Genre:"));
        addDialog.add(genreField);
        addDialog.add(new JLabel("Age Rating:"));
        addDialog.add(ratingCombo);
        addDialog.add(new JLabel("Duration (minutes):"));
        addDialog.add(durationField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            if (validateMovieInput(nameField.getText(), genreField.getText(), durationField.getText())) {
                try {
                    Movie newMovie = new Movie(
                        0, // ID will be auto-generated
                        nameField.getText(),
                        genreField.getText(),
                        (String) ratingCombo.getSelectedItem(),
                        Integer.parseInt(durationField.getText())
                    );
                    
                    boolean added = movieDao.addMovie(newMovie);
                    if (added) {
                        JOptionPane.showMessageDialog(addDialog, "Movie added successfully!");
                        loadMovieData();
                        addDialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(addDialog, "Error adding movie!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(addDialog, "Duration must be a number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> addDialog.dispose());
        
        addDialog.add(saveButton);
        addDialog.add(cancelButton);
        
        addDialog.setVisible(true);
    }
    
    private void editMovie() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a movie to edit!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int movieId = (int) tableModel.getValueAt(selectedRow, 0);
        Movie movie = movieDao.getMovieById(movieId);
        
        if (movie == null) {
            JOptionPane.showMessageDialog(this, "Movie not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Movie", true);
        editDialog.setLayout(new GridLayout(6, 2, 10, 10));
        editDialog.setSize(400, 300);
        editDialog.setLocationRelativeTo(this);
        
        JTextField nameField = new JTextField(movie.getMovieName());
        JTextField genreField = new JTextField(movie.getGenre());
        JComboBox<String> ratingCombo = new JComboBox<>(new String[]{"G", "PG", "PG-13", "R"});
        ratingCombo.setSelectedItem(movie.getAgeRating());
        JTextField durationField = new JTextField(String.valueOf(movie.getDuration()));
        
        editDialog.add(new JLabel("Movie Name:"));
        editDialog.add(nameField);
        editDialog.add(new JLabel("Genre:"));
        editDialog.add(genreField);
        editDialog.add(new JLabel("Age Rating:"));
        editDialog.add(ratingCombo);
        editDialog.add(new JLabel("Duration (minutes):"));
        editDialog.add(durationField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            if (validateMovieInput(nameField.getText(), genreField.getText(), durationField.getText())) {
                try {
                    movie.setMovieName(nameField.getText());
                    movie.setGenre(genreField.getText());
                    movie.setAgeRating((String) ratingCombo.getSelectedItem());
                    movie.setDuration(Integer.parseInt(durationField.getText()));
                    
                    boolean updated = movieDao.updateMovie(movie);
                    if (updated) {
                        JOptionPane.showMessageDialog(editDialog, "Movie updated successfully!");
                        loadMovieData();
                        editDialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(editDialog, "Error updating movie!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editDialog, "Duration must be a number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        cancelButton.addActionListener(e -> editDialog.dispose());
        
        editDialog.add(saveButton);
        editDialog.add(cancelButton);
        
        editDialog.setVisible(true);
    }
    
    private void deleteMovie() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a movie to delete!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int movieId = (int) tableModel.getValueAt(selectedRow, 0);
        String movieName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to delete movie: " + movieName + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = movieDao.deleteMovie(movieId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Movie deleted successfully!");
                loadMovieData();
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting movie!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean validateMovieInput(String name, String genre, String duration) {
        if (name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Movie name cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (genre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Genre cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (duration.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Duration cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int dur = Integer.parseInt(duration);
            if (dur <= 0) {
                JOptionPane.showMessageDialog(this, "Duration must be positive!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Duration must be a number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
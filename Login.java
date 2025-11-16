import java.sql.*;
import java.util.Scanner;

public class Login{
    public static void main(String[] args) {

        try {
            Scanner scanner = new Scanner(System.in);

            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to DB
            Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/demo",
                "root",
                "Dankdank21!!2324"
            );

            System.out.println("Connection successful!\n");

            // Ask for login
            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            String command = "SELECT * FROM Customers WHERE username = ? AND password = ?";

            PreparedStatement statement = connection.prepareStatement(command);
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                System.out.println("\nLogin successful!");
                System.out.println("Welcome, " + rs.getString("username") + "!");
            } else {
                System.out.println("\nInvalid username or password.");
            }

            rs.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        }
    }
}

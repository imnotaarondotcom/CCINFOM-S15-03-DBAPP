import java.sql.*;
import java.util.ArrayList;

public class CustomersDao {

    public ArrayList<Customers> getAllCustomers() {
        ArrayList<Customers> customers = new ArrayList<>();
        String command = "SELECT * FROM Customers ORDER BY customer_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(command);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("customer_id");
                String number = rs.getString("phone_number");
                String username = rs.getString("username");
                String accountType = rs.getString("account_type");
                String password = rs.getString("user_password");

                Customers customer = new Customers(id, number, username, accountType, password);
                customers.add(customer);
            }

        } catch (SQLException error) {
            System.out.println("Error getting customers: " + error.getMessage());
        }

        return customers;
    }


    public boolean addCustomer(Customers customer) {
        String command = "INSERT INTO Customers (phone_number, username, account_type, user_password) VALUES (?, ?, ?, ?)";

        try (Connection connect = DBConnection.getConnection();
            PreparedStatement statement = connect.prepareStatement(command, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, customer.getNumber());
            statement.setString(2, customer.getName());
            statement.setString(3, customer.getAccountType());
            statement.setString(4, customer.getPassword());

            int updated = statement.executeUpdate();

            if (updated > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customer.setID(generatedKeys.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException error) {
            System.out.println("Error adding customer: " + error.getMessage());
        }

        return false;
    }

    public boolean updateCustomer(Customers customer) {
        String command = "UPDATE Customers SET phone_number = ?, username = ?, account_type = ?, user_password = ? WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(command)) {

            statement.setString(1, customer.getNumber());
            statement.setString(2, customer.getName());
            statement.setString(3, customer.getAccountType());
            statement.setString(4, customer.getPassword());
            statement.setInt(5, customer.getID());

            int updated = statement.executeUpdate();
            return updated > 0;

        } catch (SQLException error) {
            System.out.println("Error updating customer: " + error.getMessage());
        }

        return false;
    }


    public boolean deleteCustomer(int customerId) {
        String command = "DELETE FROM Customers WHERE customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(command)) {

            statement.setInt(1, customerId);
            int updated = statement.executeUpdate();

            return updated > 0;

        } catch (SQLException error) {
            System.out.println("Error deleting customer: " + error.getMessage());
        }

        return false;
    }


    public Customers getCustomerById(int id) {
        String command = "SELECT * FROM Customers WHERE customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(command)) {

            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return new Customers(
                        rs.getInt("customer_id"),
                        rs.getString("phone_number"),
                        rs.getString("username"),
                        rs.getString("account_type"),
                        rs.getString("user_password")
                );
            }

        } catch (SQLException error) {
            System.out.println("Error getting customer: " + error.getMessage());
        }

        return null;
    }

    public Customers getCustomerByUsername(String username) {
        String command = "SELECT * FROM Customers WHERE username = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(command)) {

            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return new Customers(
                        rs.getInt("customer_id"),
                        rs.getString("phone_number"),
                        rs.getString("username"),
                        rs.getString("account_type"),
                        rs.getString("user_password")
                );
            }

        } catch (SQLException error) {
            System.out.println("Error getting customer: " + error.getMessage());
        }

        return null;
    }
}

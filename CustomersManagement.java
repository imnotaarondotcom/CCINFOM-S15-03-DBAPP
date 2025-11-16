import java.util.ArrayList;
import java.util.Scanner;

public class CustomersManagement {

    private CustomersDao customersDao;
    private Scanner scanner;

    public CustomersManagement(Scanner scanner) {
        this.customersDao = new CustomersDao();
        this.scanner = scanner;
    }

    public void customerMenu() {
        boolean useCustomers = true;

        while (useCustomers) {
            System.out.println("Customer Management:");
            System.out.println("1. View Customers");
            System.out.println("2. Add a Customer");
            System.out.println("3. Edit Customer");
            System.out.println("4. Delete Customer");
            System.out.println("5. Exit");
            System.out.print("Enter number option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // flush newline

            switch (choice) {
                case 1 -> viewCustomers();
                case 2 -> addCustomer();
                case 3 -> editCustomer();
                case 4 -> deleteCustomer();
                case 5 -> useCustomers = false;
                default -> System.out.println("Not an option");
            }
        }
    }


    public void addCustomer() {
        System.out.print("Enter Phone Number: ");
        String number = scanner.nextLine();

        System.out.print("Enter Username: ");
        String name = scanner.nextLine();

        Customers newCustomer = new Customers(0, number, name);
        boolean added = customersDao.addCustomer(newCustomer);

        if (added) System.out.println("Customer added.");
        else System.out.println("Error adding customer.");
    }


    public void deleteCustomer() {
        viewCustomers();
        System.out.print("Enter customer ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        boolean deleted = customersDao.deleteCustomer(id);

        if (deleted) System.out.println("Customer deleted.");
        else System.out.println("Failed to delete customer.");
    }


    public void editCustomer() {
        viewCustomers();
        System.out.print("Enter customer ID to edit: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("New Phone Number: ");
        String number = scanner.nextLine();

        System.out.print("New Username: ");
        String name = scanner.nextLine();

        Customers customer = new Customers(id, number, name);

        boolean updated = customersDao.updateCustomer(customer);

        if (updated) System.out.println("Customer updated.");
        else System.out.println("Error updating customer.");
    }


    public void viewCustomers() {
        ArrayList<Customers> customers = customersDao.getAllCustomers();

        System.out.println("\nDisplaying all Customers:");
        for (Customers customer : customers) {
            System.out.println(customer.formatting());
        }
    }
}

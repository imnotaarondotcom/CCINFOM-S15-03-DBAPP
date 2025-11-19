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
        String username = scanner.nextLine();

        String accountType = getAccountTypeInput();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        Customers newCustomer = new Customers(0, number, username, accountType, password);
        boolean added = customersDao.addCustomer(newCustomer);

        if (added) System.out.println("Customer added.");
        else System.out.println("Error adding customer.");
    }

    public String getAccountTypeInput() {
        while (true) 
        {
            System.out.println("Select account type:");
            System.out.println("[1] Admin");
            System.out.println("[2] Customer");
            System.out.print("Enter: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) 
            {
                case 1: return "Admin";
                case 2: return "Customer";
                default: 
                    System.out.println("Invalid account type. Try again.");
            }
        }
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

        Customers customer = customersDao.getCustomerById(id);
        if (customer == null)
        {
            System.out.println("Customer not found.");
            return;
        } 

        System.out.println(customer.formatting());

        System.out.println("What do you want to edit?");
        System.out.println("1. Username");
        System.out.println("2. Phone number");
        System.out.println("3. Account type");
        System.out.println("4. Password");
        System.out.print("Choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) 
        {
            case 1 -> {
                System.out.print("New username: ");
                customer.setName(scanner.nextLine());
            }
            case 2 -> {
                System.out.print("New phone number: ");
                customer.setNumber(scanner.nextLine());
            }
            case 3 -> customer.setAccountType(getAccountTypeInput());
            case 4 -> {
                System.out.print("Enter old password: ");

                if (!customer.getPassword().equals(scanner.nextLine())) {
                    System.out.println("Incorrect password!");
                    return;
                } else {
                    System.out.print("Enter new password: ");
                    customer.setPassword(scanner.nextLine());
                }
            }
            default -> {
                System.out.println("Invalid choice.");
                return;
            }
        }

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

    public CustomersDao getCustomersDao() {
        return this.customersDao;
    }
}

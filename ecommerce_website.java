
package ecommerce.DS_PROJECT;

import java.time.LocalDate;
package ecommerce.DS_PROJECT;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class JDBC {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/ecommerce?useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "Root123";
    private static LinkedList<Product> cart = new LinkedList<>();
    private static Queue<Order> recentOrders = new LinkedList<>();
    private static final int RECENT_ORDERS_LIMIT = 5;

    public static void userDetails() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("\t\t\t\t\t\t\t=============================");
            System.out.print("\t\t\t\t\t\t\tEnter username: ");
            String username = scanner.nextLine();

            // Check if the user already exists
            if (isUserExists(username)) {
                System.out.println("\t\t\t\t\t\t\tUsername already exists!! Please enter your password.");
                loginUser(username);
                return;
            }

            System.out.print("\t\t\t\t\t\t\tEnter gender: ");
            String gender = scanner.nextLine();

            System.out.print("\t\t\t\t\t\t\tEnter age: ");
            int age = 0;
            try {
                age = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println("\t\t\t\t\t\t\tInvalid input for age. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
                return;
            }

            System.out.print("\t\t\t\t\t\t\tEnter password: ");
            String password = scanner.nextLine(); // Accept the password
            System.out.println("\t\t\t\t\t\t\t=============================");

            insertUserDetails(username, gender, age, password); // Include password in insertion
            userMenu(); // Proceed to the user menu

        } catch (Exception e) {
            System.out.println("\t\t\t\t\t\t\tAn error occurred. Please try again.");
        }
    }

    public static void loginUser(String username) {
        Scanner scanner = new Scanner(System.in);
        int attempts = 3; // Maximum attempts allowed

        while (attempts > 0) {
            try {
                // Prompt for password
                System.out.print("\t\t\t\t\t\t\tEnter password: ");
                String enteredPassword = scanner.nextLine();

                if (isValidUser(username, enteredPassword)) {
                    System.out.println("\t\t\t\t\t\t\tLogin successful.");
                    userMenu(); // Proceed to the user menu after successful login
                    return; // Exit the method upon successful login
                } else {
                    attempts--;
                    System.out.println("\t\t\t\t\t\t\tIncorrect password. " +
                            (attempts > 0 ? "Please try again." : "Login failed."));
                }
            } catch (Exception e) {
                System.out.println("\t\t\t\t\t\t\tAn error occurred. Please try again.");
            }

            if (attempts == 0) {
                System.out.println("\t\t\t\t\t\t\tMaximum login attempts exceeded. Please try later.");
            }
        }
    }


    private static boolean isValidUser(String username, String password) {
        String query = "SELECT password FROM user WHERE Username = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                return storedPassword.equals(password); // Compare stored password with entered password
            } else {
                System.out.println("\t\t\t\t\t\t\t\tUsername not found.");
            }
        } catch (SQLException e) {
            System.out.println("\t\t\t\t\t\t\t\tAn error occurred while accessing the database. Please try again.");
            e.printStackTrace(); // Consider replacing with a logger in production code
        }
        return false; // User not found or incorrect password
    }


    private static void insertUserDetails(String username, String gender, int age, String password) {
        String insertQuery = "INSERT INTO user (Username, gender, age, password) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, gender);
            preparedStatement.setInt(3, age);
            preparedStatement.setString(4, password); // Insert password

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User details have been successfully inserted into the database.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry error code for MySQL
                System.out.println("Error: The username already exists. Please choose a different username.");
            } else {
                System.out.println("An error occurred while inserting user details. Please try again.");
            }
            e.printStackTrace(); // Consider using a logger in production
        }
    }


    private static boolean isUserExists(String username) {
        String query = "SELECT 1 FROM user WHERE Username = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next(); // Return true if the user exists
        } catch (SQLException e) {
            System.out.println("An error occurred while checking if the user exists. Please try again.");
            e.printStackTrace(); // Use a logger instead in production
            return false;
        }
    }

    public static void userMenu() {
        Scanner in = new Scanner(System.in);
        double budget = 0;
        boolean validBudget = false;

        // Loop to ensure a valid budget input
        while (!validBudget) {
            try {
                System.out.print("\nEnter your budget: ");
                budget = in.nextDouble();
                in.nextLine(); // Clear the buffer after reading a double
                validBudget = true;
            } catch (InputMismatchException e) {
                System.out.println("❌ Invalid input. Please enter a valid budget amount (e.g., 100.00).");
                in.nextLine(); // Clear the invalid input
            }
        }

        while (true) {
            System.out.print("\nEnter search keyword (e.g., product name or category): ");
            String searchKeyword = in.nextLine();

            System.out.println("\n🎚️ Sort by Price:");
            System.out.println("1. Ascending 📈");
            System.out.println("2. Descending 📉");
            System.out.println("3. By Price & Rating 🏆");
            System.out.println("4. Exit App 🚪");

            int choice = 0;
            boolean validChoice = false;

            // Loop until a valid integer input is provided
            while (!validChoice) {
                try {
                    System.out.print("Enter your choice (1-4): ");
                    choice = in.nextInt();
                    if (choice < 1 || choice > 4) {
                        System.out.println("❌ Please select a number between 1 and 4.");
                    } else {
                        validChoice = true;
                    }
                } catch (InputMismatchException e) {
                    System.out.println("❌ Invalid input. Please enter a number between 1 and 4.");
                } finally {
                    in.nextLine(); // Clear the invalid input from the buffer
                }
            }

            if (choice == 4) {
                System.out.println("👋 Exiting the app. Thank you for using E-commerce App!");
                break; // Exit the loop to stop the application
            }

            List<Product> filteredProducts;
            try {
                switch (choice) {
                    case 1 -> filteredProducts = getProductsByBudgetAndKeyword(budget, searchKeyword, true);
                    case 2 -> filteredProducts = getProductsByBudgetAndKeyword(budget, searchKeyword, false);
                    case 3 -> filteredProducts = getProductsByBudgetAndRating(budget, searchKeyword);
                    default -> {
                        System.out.println("❌ Invalid choice. Please try again.");
                        continue;
                    }
                }

                if (filteredProducts.isEmpty()) {
                    System.out.println("\t\t\t\t\t😔 No products found within your budget and search criteria.");
                } else {
                    displayProducts(filteredProducts);
                    handleProductActions(in, filteredProducts);
                }

            } catch (Exception e) {
                System.out.println("❌ An unexpected error occurred while retrieving products. Please try again.");
                e.printStackTrace(); // Replace with logging in production
            }

            System.out.print("\n🔄 Would you like to search for more products? (y/n): ");
            String continueSearch = in.next();
            if (!continueSearch.equalsIgnoreCase("y")) {
                System.out.println("👋 Exiting the app. Thank you for using E-commerce App!");
                break;
            }
            in.nextLine(); // Clear the buffer after reading "y/n" input
        }
    }

    public static List<Product> getProductsByBudgetAndKeyword(double budget, String keyword, boolean ascending) {
        String query = "SELECT * FROM products WHERE price <= ? AND (name LIKE ? OR category LIKE ?) ORDER BY price "
                + (ascending ? "ASC" : "DESC");
        List<Product> productList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setDouble(1, budget);
            preparedStatement.setString(2, "%" + keyword + "%");
            preparedStatement.setString(3, "%" + keyword + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                productList.add(mapResultToProduct(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("❌ An error occurred while fetching products: " + e.getMessage());
            e.printStackTrace(); // Optional: Print stack trace for debugging
        } catch (Exception e) {
            System.out.println("❌ An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Optional: Print stack trace for debugging
        }

        return productList;
    }


    public static List<Product> getProductsByBudgetAndRating(double budget, String keyword) {
        String query = "SELECT * FROM products WHERE price <= ? AND (name LIKE ? OR category LIKE ?)";
        PriorityQueue<Product> productQueue = new PriorityQueue<>((p1, p2) -> {
            int ratingComparison = Integer.compare(p2.getStarRating(), p1.getStarRating());
            return ratingComparison != 0 ? ratingComparison : Double.compare(p1.getPrice(), p2.getPrice());
        });

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setDouble(1, budget);
            preparedStatement.setString(2, "%" + keyword + "%");
            preparedStatement.setString(3, "%" + keyword + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                productQueue.add(mapResultToProduct(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("❌ An error occurred while fetching products: " + e.getMessage());
            e.printStackTrace(); // Optional: Print stack trace for debugging
        } catch (Exception e) {
            System.out.println("❌ An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Optional: Print stack trace for debugging
        }

        // Convert the priority queue to a list
        List<Product> sortedProducts = new ArrayList<>();
        while (!productQueue.isEmpty()) {
            sortedProducts.add(productQueue.poll());
        }
        return sortedProducts;
    }

    private static void displayProducts(List<Product> products) {
        try {
            System.out.println("\n✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨ Welcome to the Product Gallery ✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨");

            System.out.printf("🆔 %-5s 🏷️ %-30s 💼 %-25s 💰 %-22s 🔖 %-20s 📂 %-20s 🗂️ %-20s 📦 %-15s ⭐ %-10s 🔗 %-10s\n",
                    "ID", "Name", "Brand", "Price", "Discounted Price", "Category", "Sub-Category", "Stock", "Rating",
                    "Breadcrumbs");
            System.out.println("➖".repeat(120)); // Visual separator

            for (Product product : products) {
                String truncatedName = truncateText(product.getName(), 30);
                String truncatedBreadcrumbs = truncateText(product.getBreadcrumbs(), 10);

                System.out.printf(
                        "🆔 %-5d 🏷️ %-30s 💼 %-25s 💰 %-22.2f 🔖 %-20.2f 📂 %-20s 🗂️ %-20s 📦 %-15d ⭐ %-10s 🔗 %-10s\n",
                        product.getId(), truncatedName, product.getBrand(), product.getPrice(),
                        product.getDiscountedPrice(), product.getCategory(), product.getSubCategory(),
                        product.getStockQuantity(), product.getStarRatingAsStars(), truncatedBreadcrumbs);
            }
            System.out.println("\n✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨ End of Product List ✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨\n");

        } catch (NullPointerException e) {
            System.err.println("Error: One or more products have missing information. Please check the product data.");
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid argument found in product data. Please ensure all data is correct.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while displaying products: " + e.getMessage());
        }
    }


    private static String truncateText(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "..."; // Add ellipsis for truncation
        }
        return text;
    }

    private static void handleProductActions(Scanner scanner, List<Product> products) {
        while (true) {
            try {
                System.out.println("\n🛠️ Options:");
                System.out.println("1️. View a Product");
                System.out.println("2️. View Recently Viewed Products");
                System.out.println("3️. View Order History");
                System.out.println("4️. View Cart 🛒");
                System.out.println("5. Go Back to Search 🔍");
                System.out.println("6. Exit App 🚪");

                int action = scanner.nextInt();
                scanner.nextLine(); // Clear the buffer after reading an integer

                switch (action) {
                    case 1 -> viewProduct(scanner, products); // View a specific product
                    case 2 -> Product.viewRecentlyViewed(); // View recently viewed products
                    case 3 -> viewOrderHistory(); // View order history
                    case 4 -> viewCart(scanner); // New option to view the cart
                    case 5 -> {
                        return; // Go back to the main search menu
                    }
                    case 6 -> {
                        System.out.println("\n👋 Thank you for using E-commerce App! Goodbye!");
                        System.exit(0);
                    }
                    default -> System.out.println("❌ Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("❌ Invalid input. Please enter a number corresponding to your choice.");
                scanner.nextLine(); // Clear the invalid input from the buffer
            } catch (Exception e) {
                System.out.println("❌ An unexpected error occurred: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for debugging
            }
        }
    }

    private static void viewProduct(Scanner scanner, List<Product> products) {
        try {
            System.out.print("Enter the ID of the product you want to view: ");
            int productId = scanner.nextInt();
            scanner.nextLine(); // Clear the buffer after reading an integer

            // Find the selected product based on the provided ID
            Product selectedProduct = products.stream()
                    .filter(p -> p.getId() == productId)
                    .findFirst()
                    .orElse(null);

            if (selectedProduct == null) {
                System.out.println("Product not found.");
                return;
            }

            // View product details
            Product.viewProductDetails(productId);
            addToCartOrBuy(scanner, selectedProduct);
        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid input. Please enter a valid product ID (a number).");
            scanner.nextLine(); // Clear the invalid input from the buffer
        } catch (Exception e) {
            System.out.println("❌ An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    public static Product getProductById(int productId) {
        String query = "SELECT * FROM products WHERE id = ?";
        Product product = null; // Initialize product as null

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, productId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                product = mapResultToProduct(resultSet);
            } else {
                System.out.println("⚠️ No product found with ID: " + productId);
            }
        } catch (SQLException e) {
            System.out.println("❌ An error occurred while fetching the product: " + e.getMessage());
            e.printStackTrace(); // Optional: Print stack trace for debugging
        } catch (Exception e) {
            System.out.println("❌ An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Optional: Print stack trace for debugging
        }

        return product; // Return the product (or null if not found or an error occurred)
    }

    private static void addToCartOrBuy(Scanner scanner, Product product) {
        try {
            System.out.println("\nOptions:");
            System.out.println("1. Add to Cart");
            System.out.println("2. Buy Now");
            System.out.println("3. View Cart");
            System.out.println("4. Back to Product List");
            System.out.println("5. Exit the App");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear the buffer after reading an integer

            switch (choice) {
                case 1 -> addToCart(product);
                case 2 -> placeOrder(scanner, product); // This will handle "Buy Now"
                case 3 -> viewCart(scanner); // New option to view the cart
                case 4 -> System.out.println("Returning to product list...");
                case 5 -> {
                    System.out.println("\n👋 Thank you for using E-commerce App! Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("❌ Invalid choice. Please try again.");
            }
        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid input. Please enter a number corresponding to your choice.");
            scanner.nextLine(); // Clear the invalid input from the buffer
        } catch (Exception e) {
            System.out.println("❌ An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    private static void addToCart(Product product) {
        cart.add(product);
        System.out.println("🛒 Product added to cart successfully!");
    }

    private static void placeOrder(Scanner scanner, Product product) {
        try {
            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine();
            System.out.print("\n👤 Enter your username: ");
            String username = scanner.nextLine();

            if (!isUserExists(username)) {
                System.out.println("❌ User does not exist. Please sign up or check your username.");
                return;
            }


            System.out.println("📍 Please enter delivery address:");
            String address = scanner.nextLine();

            LocalDate orderDate = LocalDate.now();
            String paymentMode = "Cash on Delivery";

            try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement(
                         "INSERT INTO Orders (username, product_id, order_date, delivery_address, payment_mode, status, quantity, order_amount) "
                                 + "VALUES (?, ?, ?, ?, ?, 'Placed', ?, ?)",
                         Statement.RETURN_GENERATED_KEYS)) {
                float orderAmount = (float) (product.getDiscountedPrice() * quantity);
                stmt.setString(1, username);
                stmt.setInt(2, product.getId());
                stmt.setDate(3, java.sql.Date.valueOf(orderDate));
                stmt.setString(4, address);
                stmt.setString(5, paymentMode);
                stmt.setInt(6, quantity);  // Corrected indices
                stmt.setFloat(7, orderAmount);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("✅ Order has been placed successfully.");
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);
                        Order order = new Order(orderId, username, product.getId(), orderDate, address, paymentMode, "Placed", orderAmount, quantity);
                        addOrderToRecent(order);
                    }
                }
            } catch (SQLException e) {
                System.out.println("❌ An error occurred while placing the order: " + e.getMessage());
                e.printStackTrace(); // Debug
            }

            // Next actions:
            System.out.println("\nWould you like to:\n1️⃣ View Order History\n2️⃣ Search for Items 🔍\n3️ Exit the App");
            int nextAction = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            switch (nextAction) {
                case 1 -> viewOrderHistory();
                case 2 -> userMenu();
                case 3 -> {
                    System.out.println("Thank you for shopping!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid input, redirecting.");
            }

        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid input. Try again.");
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("❌ Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewCart(Scanner scanner) {
        try {
            if (cart.isEmpty()) {
                System.out.println("Your cart is empty.");

                // Offer the user an option to shop if the cart is empty
                System.out.println("\nWould you like to:");
                System.out.println("1. Shop Now (Search for Products)");
                System.out.println("2. Go Back to Main Menu");
                System.out.println("3. Exit the App");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear the buffer after reading an integer

                switch (choice) {
                    case 1 -> userMenu(); // Redirect to product search
                    case 2 -> System.out.println("Returning to the main menu...");
                    case 3 -> {
                        System.out.println("\n👋 Thank you for using E-commerce App! Goodbye!");
                        System.exit(0);
                    }
                    default -> System.out.println("❌ Invalid choice. Please try again.");
                }
            } else {
                System.out.println("\n🛒 Your Cart Options:");
                System.out.println("1. View Normal Cart");
                System.out.println("2. View Magic Cart");
                System.out.println("3. Back to Main Menu");
                System.out.println("4. Exit the App");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear the buffer after reading an integer

                switch (choice) {
                    case 1 -> viewNormalCart(scanner); // View standard cart functionality
                    case 2 -> {
                        // Ensure cart is properly initialized and is a LinkedList of Product objects
                        LinkedList<Product> cartCopy = new LinkedList<>(cart);  // Create a copy of the cart

                        // Create a new instance of MagicCart using the copied cart
                        MagicCart magicCart = new MagicCart(cartCopy);

                        // Start the MagicCart functionality, which should trigger the navigation and cart sorting
                        magicCart.start();
                    }
                    case 3 -> System.out.println("🔙 Returning to the main menu...");
                    case 4 -> {
                        System.out.println("\n👋 Thank you for using E-commerce App! Goodbye!");
                        System.exit(0);
                    }
                    default -> System.out.println("❌ Invalid choice. Please try again.");
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid input. Please enter a number corresponding to your choice.");
            scanner.nextLine(); // Clear the invalid input from the buffer
        } catch (Exception e) {
            System.out.println("❌ An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    private static void viewNormalCart(Scanner scanner) {
        System.out.println("\n🛒 Your Normal Cart:");
        for (Product product : cart) {
            System.out.printf("🆔 %-5d 🏷️ %-30s 💰 $%-22.2f\n", product.getId(), product.getName(), product.getPrice());
        }

        System.out.println("\n1. Proceed to Checkout");
        System.out.println("2. Remove Items from Cart");
        System.out.println("3. Back to Product List");
        System.out.println("4. Exit the App");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Clear the buffer after reading an integer

        switch (choice) {
            case 1 -> checkout(scanner); // Proceed to checkout
            case 2 -> removeFromCart(scanner); // Option to remove items
            case 3 -> System.out.println("🔙 Returning to product list...");
            case 4 -> {
                System.out.println("\n👋 Thank you for using E-commerce App! Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("❌ Invalid choice. Please try again.");
        }
    }


    private static void removeFromCart(Scanner scanner) {
        try {
            System.out.print("Enter the ID of the product you want to remove from the cart: ");
            int productId = scanner.nextInt();
            scanner.nextLine(); // Clear the buffer after reading an integer

            Product productToRemove = null;
            for (Product product : cart) {
                if (product.getId() == productId) {
                    productToRemove = product;
                    break;
                }
            }

            if (productToRemove != null) {
                cart.remove(productToRemove);
                System.out.println("✅ Product removed from the cart.");
            } else {
                System.out.println("❌ Product not found in the cart.");
            }
        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid input. Please enter a valid product ID.");
            scanner.nextLine(); // Clear the invalid input from the buffer
        } catch (Exception e) {
            System.out.println("❌ An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Optional: Print stack trace for debugging
        }
    }


    private static void addOrderToRecent(Order order) {
        if (recentOrders.size() == RECENT_ORDERS_LIMIT) {
            recentOrders.poll();
        }
        recentOrders.add(order);
    }

    private static void checkout(Scanner scanner) {
        try {
            System.out.println("\nProceeding to checkout...");
            if (cart.isEmpty()) {
                System.out.println("🛒 Your cart is empty. Cannot proceed to checkout.");
                return;
            }
            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine();
            System.out.print("👤 Enter your username: ");
            String username = scanner.nextLine();

            if (!isUserExists(username)) {
                System.out.println("❌ User does not exist. Please sign up or check your username.");
                return;
            }

            System.out.print("📍 Enter delivery address: ");
            String address = scanner.nextLine();

            List<Product> failedOrders = new ArrayList<>();

            LocalDate orderDate = LocalDate.now();
            String paymentMode = "Cash on Delivery";

            try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement(
                         "INSERT INTO Orders (username, product_id, order_date, delivery_address, payment_mode, status, quantity, order_amount) "
                                 + "VALUES (?, ?, ?, ?, ?, 'Placed', ?, ?)",
                         Statement.RETURN_GENERATED_KEYS)) {

                for (Product product : cart) {
                    try {

                        float orderAmount = (float) (product.getDiscountedPrice() * quantity); // Assuming Product has getPrice() method

                        stmt.setString(1, username);
                        stmt.setInt(2, product.getId());
                        stmt.setDate(3, java.sql.Date.valueOf(orderDate));
                        stmt.setString(4, address);
                        stmt.setString(5, paymentMode);
                        stmt.setInt(6, quantity);
                        stmt.setFloat(7, orderAmount);

                        int rowsInserted = stmt.executeUpdate();
                        if (rowsInserted > 0) {
                            System.out.println("✅ Order for product " + product.getName() + " has been placed successfully.");
                            ResultSet generatedKeys = stmt.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                int orderId = generatedKeys.getInt(1);
                                Order order = new Order(orderId, username, product.getId(), orderDate, address, paymentMode, "Placed", orderAmount, quantity);
                                addOrderToRecent(order);
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("❌ Failed to place order for product " + product.getName() + ": " + e.getMessage());
                        failedOrders.add(product);
                    }
                }
            } catch (SQLException e) {
                System.out.println("❌ Database error: " + e.getMessage());
                e.printStackTrace();
            }

            cart.clear();
            System.out.println("🛒 Thank you for shopping! All items have been processed.");

            if (!failedOrders.isEmpty()) {
                System.out.println("⚠️ Failed to order the following products:");
                for (Product failedProduct : failedOrders) {
                    System.out.println(" - " + failedProduct.getName());
                }
            }

        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid input. Please enter valid information.");
            scanner.nextLine(); // Clear the invalid input from the buffer
        } catch (Exception e) {
            System.out.println("❌ An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewOrderHistory() {
        try {
            System.out.println("\n📜 ===================================================================== Order History ==================================================================== 📜");
            if (recentOrders == null || recentOrders.isEmpty()) {
                System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t\t\t😔 No recent orders found.");
            } else {
                System.out.printf("🛒 %-10s 👤 %-15s 🆔 %-10s 📅 %-15s 📍 %-30s 💳 %-15s 📦 %-10s  💰 %-10s  %-10s\n", "Order ID",
                        "Username", "Product ID", "Order Date", "Delivery Address", "Payment Mode", "Status", "Order Amount", "Quantity");
                System.out.println("➖".repeat(90));

                for (Order order : recentOrders) {
                    if (order != null) { // Check for null order
                        System.out.printf("🛒 %-10d 👤 %-15s 🆔 %-10d 📅 %-15s 📍 %-30s 💳 %-15s 📦 %-10s 💰 %-10s  %-10s\n",
                                order.getOrderId(), order.getUsername(), order.getProductId(), order.getOrderDate(),
                                order.getDeliveryAddress(), order.getPaymentMode(), order.getStatus(), order.getOrder_amount(), order.getQuantity());
                    } else {
                        System.out.println("❌ Encountered a null order in recent orders.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ An error occurred while viewing order history: " + e.getMessage());
            e.printStackTrace(); // Optional: Print stack trace for debugging
        } finally {
            System.out.println("📜 ========================================================================================================================================================= 📜\n");
        }
    }

    private static int getCurrentUserId() {

        return 1;
    }


    private static Product mapResultToProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("brand"),
                resultSet.getDouble("price"),
                resultSet.getDouble("discounted_price"),
                resultSet.getString("category"),
                resultSet.getString("sub_category"),
                resultSet.getString("breadcrumbs"),
                resultSet.getInt("stock_quantity"),
                resultSet.getInt("star_rating")
        );
    }

    //ADMIN LOGIN
    public static void adminDetails() {
        Scanner in = new Scanner(System.in);
        final String correctPassword = "Prishmi@5730";

        System.out.print("Enter admin password: ");
        String enteredPassword = in.nextLine();

        if (!correctPassword.equals(enteredPassword)) {
            System.out.println("❌ Incorrect password. Access denied.");
            return;
        }

        adminoperation();
    }

    public static void revenue() {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("Want to generate\n1.Daily Revenue\n2.Monthly Revenue\n3.Yearly Revenue\n0.Skip\nEnter Your Choice:");
            int Choice = scan.nextInt();
            scan.nextLine(); // Consume the newline character
            if (Choice == 0) {
                break;
            }
            switch (Choice) {
                case 1:
                    System.out.println("Enter the date of which you want the revenue(YYYY-MM-DD):");
                    String date = scan.nextLine();
                    revenuedaily(date);
                    System.out.println("Want to check more details(yes/no):");
                    String ch = scan.nextLine();
                    if (ch.equalsIgnoreCase("yes")) {
                        printDailyRevenueDetails(date);
                    } else {
                        break;
                    }
                    break;
                case 2:
                    System.out.println("Enter the year(YYYY):");
                    String year = scan.nextLine();
                    System.out.println("Enter the month(MM):");
                    String month = scan.nextLine();
                    revenuemonthly(year, month);
                    System.out.println("Want to check more details(yes/no):");
                    String choose = scan.nextLine();
                    if (choose.equalsIgnoreCase("yes")) {
                        printMonthlyRevenueDetails(year, month);
                    }
                    break;
                case 3:
                    System.out.println("Enter year:");
                    String Year = scan.nextLine();
                    revenueyearly(Year);
                    System.out.println("Want to check more details(yes/no):");
                    String choice = scan.nextLine();
                    if (choice.equalsIgnoreCase("yes")) {
                        printYearlyRevenueDetails(Year);
                    }
                    break;
                default:
                    System.out.println("Invalid Choice");
            }
        }
    }

    private static void adminoperation() {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("What operation you want to perform\n1.Want to check all users details?\n2.Check revenue\n0.Skip\nEnter your choice:");
            int choice = scan.nextInt();
            scan.nextLine();  // Consume newline
            if (choice == 0) {
                break;
            }
            switch (choice) {
                case 1:
                    printAllUserDetails();
                    break;
                case 2:
                    revenue();
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }

    private static void revenuedaily(String date) {
        String query = "SELECT SUM(order_amount) AS total_revenue FROM orders WHERE status = 'Placed' AND DATE(order_date) = ?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the date parameter in the query
            pstmt.setString(1, date);

            // Execute the query
            ResultSet rs = pstmt.executeQuery();

            // Retrieve and display total revenue
            if (rs.next()) {
                double totalRevenue = rs.getDouble("total_revenue");
                System.out.println("Total revenue for " + date + " is: Rs." + totalRevenue);
            } else {
                System.out.println("No orders found for the given date.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void revenuemonthly(String year, String month) {
        String query = "SELECT SUM(order_amount) AS total_revenue FROM orders WHERE status = 'Placed' AND YEAR(order_date) = ? AND MONTH(order_date) = ?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the year and month parameters in the query
            pstmt.setString(1, year);
            pstmt.setString(2, month);

            // Execute the query
            ResultSet rs = pstmt.executeQuery();

            // Retrieve and display total revenue
            if (rs.next()) {
                double totalRevenue = rs.getDouble("total_revenue");
                System.out.println("Total revenue for " + year + "-" + month + " is: Rs." + totalRevenue);
            } else {
                System.out.println("No orders found for the given month.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void revenueyearly(String year) {
        String query = "SELECT SUM(order_amount) AS total_revenue FROM orders WHERE status = 'Placed' AND YEAR(order_date) = ?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the year parameter in the query
            pstmt.setString(1, year);

            // Execute the query
            ResultSet rs = pstmt.executeQuery();

            // Retrieve and display total revenue
            if (rs.next()) {
                double totalRevenue = rs.getDouble("total_revenue");
                System.out.println("Total revenue for the year " + year + " is: Rs." + totalRevenue);
            } else {
                System.out.println("No orders found for the given year.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void printDailyRevenueDetails(String date) {
        String query = "SELECT product_id, SUM(quantity) AS total_quantity, SUM(order_amount) AS total_amount " +
                "FROM orders WHERE status = 'Placed' AND DATE(order_date) = ? " +
                "GROUP BY product_id";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the date parameter in the query
            pstmt.setString(1, date);

            // Execute the query
            ResultSet rs = pstmt.executeQuery();

            // Print revenue details header
            System.out.println("Daily Revenue Details for " + date);
            System.out.println("----------------------------------------------------");
            System.out.println("Product ID | Total Quantity | Total Amount (Rs.)");
            System.out.println("----------------------------------------------------");

            // Retrieve and print each product's details
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int totalQuantity = rs.getInt("total_quantity");
                double totalAmount = rs.getDouble("total_amount");

                System.out.printf("%-11d | %-14d | Rs. %.2f%n", productId, totalQuantity, totalAmount);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void printMonthlyRevenueDetails(String year, String month) {
        String query = "SELECT product_id, SUM(quantity) AS total_quantity, SUM(order_amount) AS total_amount " +
                "FROM orders WHERE status = 'Placed' AND YEAR(order_date) = ? AND MONTH(order_date) = ? " +
                "GROUP BY product_id";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the year and month parameters in the query
            pstmt.setString(1, year);
            pstmt.setString(2, month);

            // Execute the query
            ResultSet rs = pstmt.executeQuery();

            // Print revenue details header
            System.out.println("Monthly Revenue Details for " + year + "-" + month);
            System.out.println("----------------------------------------------------");
            System.out.println("Product ID | Total Quantity | Total Amount (Rs.)");
            System.out.println("----------------------------------------------------");

            // Retrieve and print each product's details
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int totalQuantity = rs.getInt("total_quantity");
                double totalAmount = rs.getDouble("total_amount");

                System.out.printf("%-11d | %-14d | Rs. %.2f%n", productId, totalQuantity, totalAmount);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void printYearlyRevenueDetails(String year) {
        String query = "SELECT product_id, SUM(quantity) AS total_quantity, SUM(order_amount) AS total_amount " +
                "FROM orders WHERE status = 'Placed' AND YEAR(order_date) = ? " +
                "GROUP BY product_id";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the year parameter in the query
            pstmt.setString(1, year);

            // Execute the query
            ResultSet rs = pstmt.executeQuery();

            // Print revenue details header
            System.out.println("Yearly Revenue Details for " + year);
            System.out.println("----------------------------------------------------");
            System.out.println("Product ID | Total Quantity | Total Amount (Rs.)");
            System.out.println("----------------------------------------------------");

            // Retrieve and print each product's details
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int totalQuantity = rs.getInt("total_quantity");
                double totalAmount = rs.getDouble("total_amount");

                System.out.printf("%-11d | %-14d | Rs. %.2f%n", productId, totalQuantity, totalAmount);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void printAllUserDetails() {
        BinarySearchTree bst = new BinarySearchTree();

        // Query to get all user details from the users table
        String query = "SELECT Username, address, age FROM user";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            // Insert each user detail into the BST
            while (rs.next()) {
                String username = rs.getString("username");
                String address = rs.getString("address");
                int age = rs.getInt("age");
                bst.insert(username, address, age);
            }

            // Print all user details in sorted order (lexicographically by username)
            System.out.println("User Details in lexicographical order:");
            bst.inOrder();  // In-order traversal will display the user details in sorted order

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}

public class Order {
    private int orderId;
    private String username;  // Changed from int userId to String username
    private int productId;
    private LocalDate orderDate;
    private String deliveryAddress;
    private String paymentMode;
    private String status;


    private int quantity;
    private float order_amount;



    // Constructor with all fields
    public Order(int orderId, String username, int productId, LocalDate orderDate, String deliveryAddress, String paymentMode, String status,float order_amount,int quantity) {
        this.orderId = orderId;
        this.username = username;
        this.productId = productId;
        this.orderDate = orderDate;
        this.deliveryAddress = deliveryAddress;
        this.paymentMode = paymentMode;
        this.status = status;
this.order_amount=order_amount;
this.quantity=quantity;
    }

    // Getter for orderId
    public int getOrderId() {
        return orderId;
    }

    // Getter for username
    public String getUsername() {
        return username;
    }

    // Getter for productId
    public int getProductId() {
        return productId;
    }

    // Getter for orderDate
    public LocalDate getOrderDate() {
        return orderDate;
    }

    // Getter for deliveryAddress
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    // Getter for paymentMode
    public String getPaymentMode() {
        return paymentMode;
    }

    // Getter for status
    public String getStatus() {
        return status;
    }

    public float getOrder_amount() {
        return order_amount;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return String.format("Order ID: %d, Username: %s, Product ID: %d, Order Date: %s, Address: %s, Payment Mode: %s, Status: %s , Quantity: %d, Order Amount: %.2f",
                orderId, username, productId, orderDate, deliveryAddress, paymentMode, status,quantity,order_amount);
    }
}
class BSTNode {
    String username;
    String address;
    int age ;
    BSTNode left, right;

    public BSTNode(String username, String address, int age){
        this.username = username;
        this.address = address;
        this.age = age;
        this.left = this.right = null;
    }
}
class BinarySearchTree {
    private BSTNode root;

    public BinarySearchTree() {
        root = null;
    }

    // Method to insert a new user into the BST
    public void insert(String username, String address, int age) {
        root = insertRec(root, username, address, age);
    }

    private BSTNode insertRec(BSTNode root, String username, String address, int age) {
        // If the tree is empty, create a new node
        if (root == null) {
            root = new BSTNode(username, address, age);
            return root;
        }



        // Otherwise, recur down the tree
        if (username.compareTo(root.username) < 0) {
            root.left = insertRec(root.left, username, address, age);
        } else if (username.compareTo(root.username) > 0) {
            root.right = insertRec(root.right, username, address, age);
        }

        // Return the unchanged root pointer
        return root;
    }

    // In-order traversal of the BST to get sorted user details
    public void inOrder() {
        inOrderRec(root);
    }

    private void inOrderRec(BSTNode root) {
        if (root != null) {
            // Traverse the left subtree
            inOrderRec(root.left);

            // Print the user details in sorted order
            System.out.println("Username: " + root.username);
            System.out.println("Address: " + root.address);
            System.out.println("Age: " + root.age);
            System.out.println("-------------------------------");

            // Traverse the right subtree
            inOrderRec(root.right);
        }
    }
}
public class MagicCart {
    private TreeNode root;
    private Scanner scanner;
    public List<String> pathHistory;
    private LinkedList<Product> magicCart; // Independent MagicCart

    public MagicCart(LinkedList<Product> cart) {
        this.magicCart = cart; // Use a copy of the existing cart
        root = buildTree();    // Builds category tree
        scanner = new Scanner(System.in);
        pathHistory = new ArrayList<>();
    }

    // Builds a sample binary tree of categories
    private TreeNode buildTree() {
        TreeNode grocery = new TreeNode("🌟 Grocery Paradise 😋");
        TreeNode packagedFood = new TreeNode("✨ Packaged Food Delights 🍱");
        TreeNode personalCare = new TreeNode("🛁 Personal Care Essentials 🧴");
        TreeNode beautyCosmetics = new TreeNode("💖 Beauty & Cosmetics Wonderland 💄");
        grocery.left = packagedFood;
        grocery.right = personalCare;

        TreeNode homeKitchen = new TreeNode("Home & Kitchen 🏠");
        TreeNode appliances = new TreeNode("Appliances ⚙");
        TreeNode electronics = new TreeNode("Electronics 📺");
        homeKitchen.left = appliances;
        homeKitchen.right = electronics;

        TreeNode root = new TreeNode("Categories");
        root.left = grocery;
        root.right = homeKitchen;

        return root;
    }

    // Start navigating the MagicCart category tree
    public void start() {
        System.out.println("🎉 Welcome to the Magic Cart! 🛒 Your adventure in shopping starts here!");
        navigateTree(root);
    }

    private void navigateTree(TreeNode node) {
        if (node == null) {
            return;
        }

        while (true) {
            System.out.println("\nCurrent Category: " + node.category);
            prettyPrintPath();

            System.out.println("✨ Ready to explore? Choose your next adventure:");
            if (node.left != null) {
                System.out.println("1. 🌟 Dive into " + node.left.category + "!");
            }
            if (node.right != null) {
                System.out.println("2. 🌈 Venture into " + node.right.category + "!");
            }
            System.out.println("0. Go Back 🔙");
            System.out.println("9. Quit ❌");

            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    if (node.left != null) {
                        pathHistory.add(node.category);
                        navigateTree(node.left);
                    } else {
                        System.out.println("🚫 Oops! No further options.");
                    }
                    break;
                case 2:
                    if (node.right != null) {
                        pathHistory.add(node.category);
                        navigateTree(node.right);
                    } else {
                        System.out.println("🚫 Oops! No further options.");
                    }
                    break;
                case 0:
                    if (!pathHistory.isEmpty()) {
                        pathHistory.remove(pathHistory.size() - 1);
                    }
                    System.out.println("🔙 Going back...");
                    return;
                case 9:
                    System.out.println("Thank you for using the Magic Cart. Goodbye! 👋");
                    sortAndDisplayCart();
                    userMenu();
                default:
                    System.out.println("❌ Invalid choice. Please try again.");
            }
        }
    }

    private void prettyPrintPath() {
        System.out.println("Path History:");
        for (int i = 0; i < pathHistory.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + pathHistory.get(i));
        }
    }

    private int getUserChoice() {
        while (true) {
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void sortAndDisplayCart() {
        List<Product> sortedCart = sortCart(magicCart, pathHistory);
        System.out.println("\nSorted Cart:");
        for (Product product : sortedCart) {
            System.out.println(product.getName() + " - " + product.getCategory());
        }
    }

    public List<Product> sortCart(LinkedList<Product> cart, List<String> pathHistory) {
        Map<String, Integer> categoryPriority = new HashMap<>();
        for (int i = 0; i < pathHistory.size(); i++) {
            categoryPriority.put(pathHistory.get(i).toLowerCase(), i);
        }

        List<Product> sortedList = new ArrayList<>(cart);
        sortedList.sort((p1, p2) -> {
            Integer priority1 = getCategoryPriority(p1.getCategory(), categoryPriority);
            Integer priority2 = getCategoryPriority(p2.getCategory(), categoryPriority);
            return priority1 - priority2;
        });

        return sortedList;
    }

    private Integer getCategoryPriority(String category, Map<String, Integer> categoryPriority) {
        for (String key : categoryPriority.keySet()) {
            if (category.toLowerCase().contains(key)) {
                return categoryPriority.get(key);
            }
        }
        return Integer.MAX_VALUE;
    }
}
public class TreeNode {
    String category;
    TreeNode left;  // Represents one option
    TreeNode right; // Represents another option

    public TreeNode(String category) {
        this.category = category;
        this.left = null;
        this.right = null;
    }
}
public class Product {
    private static Queue<Product> recentlyViewed = new LinkedList<>();
    private static final int RECENTLY_VIEWED_LIMIT = 5;

    private int id;
    private String name;
    private String brand;
    private double price;
    private double discountedPrice;
    private String category;
    private String subCategory;
    private String breadcrumbs;
    private int stockQuantity;
    private int starRating;

    public Product(int id, String name, String brand, double price, double discountedPrice, String category,
                   String subCategory, String breadcrumbs, int stockQuantity, int starRating) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.discountedPrice = discountedPrice;
        this.category = category;
        this.subCategory = subCategory;
        this.breadcrumbs = breadcrumbs;
        this.stockQuantity = stockQuantity;
        this.starRating = starRating;
    }
    public Product(String name, String category,int id,float Price) {
        this.name = name;
        this.category = category;
        this.id=id;
        this.price=price;
    }

    // Getters for all fields
    public int getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public double getPrice() { return price; }
    public double getDiscountedPrice() { return discountedPrice; }
    public String getCategory() { return category; }
    public String getSubCategory() { return subCategory; }
    public String getBreadcrumbs() { return breadcrumbs; }
    public int getStockQuantity() { return stockQuantity; }
    public int getStarRating() { return starRating; }

    public String getStarRatingAsStars() {
        return "⭐".repeat(starRating);
    }

    @Override
    public String toString() {
        return String.format("%-5d %-30s %-15s %-10.2f %-15.2f %-15s %-15s %-25s %-10d %-10s",
                id,
                truncateText(name, 30),
                truncateText(brand, 15),
                price,
                discountedPrice,
                truncateText(category, 15),
                truncateText(subCategory, 15),
                truncateText(breadcrumbs, 25),
                stockQuantity,
                getStarRatingAsStars());
    }

    // Static methods for recently viewed products
    public static void viewProductDetails(int productId) {
        Product product = null;
        try {
            product = JDBC.getProductById(productId);
        } catch (Exception e) {
            System.out.println("❌ An error occurred while retrieving product details: " + e.getMessage());
            e.printStackTrace(); // Optional: Print stack trace for debugging
            return; // Exit if there was an error
        }

        if (product == null) {
            System.out.println("⚠️ Product not found.");
            return;
        }

        System.out.println("\nProduct Details:");
        System.out.println(product);
        addToRecentlyViewed(product);
    }

    private static void addToRecentlyViewed(Product product) {
        if (recentlyViewed.size() == RECENTLY_VIEWED_LIMIT) {
            recentlyViewed.poll();
        }
        recentlyViewed.add(product);
    }

    public static void viewRecentlyViewed() {
        System.out.println("\nRecently Viewed Products:");
        if (recentlyViewed.isEmpty()) {
            System.out.println("No recently viewed products.");
        } else {
            recentlyViewed.forEach(System.out::println);
        }
    }

    // Helper method to truncate text for alignment
    private static String truncateText(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "..."; // Truncate with ellipsis
        }
        return text;
    }
}
public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        displayWelcomeScreen();

        int choice = getUserChoice(scan);
        switch (choice) {
            case 1 -> {
                System.out.println("\n🔍 Redirecting to User Login...");
                JDBC.userDetails();
            }
            case 2 -> {
                System.out.println("\n🔐 Redirecting to Admin Login...");
                JDBC.adminDetails();
            }
            case 3 -> {
                System.out.println("\n👋 Thank you for using E-commerce App! Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("\n❌ Invalid choice. Please try again.");
        }
        scan.close();
    }

    private static void displayWelcomeScreen() {
        System.out.println("\n\t\t\t\t\t\t\t🌟🌟🌟==============================🌟🌟🌟");
        System.out.println("\t\t\t\t\t\t\t    Welcome to the D-MART App 🛍️");
        System.out.println("\t\t\t\t\t\t\t🌟🌟🌟==============================🌟🌟🌟\n");

        // Centered box width
        int boxWidth = 50;

        // Print top border
        printCenteredHorizontalBorder(boxWidth);

        // Print options within the box
        printCenteredText("👤1. Are you a User?", boxWidth);
        printCenteredHorizontalLine(boxWidth);
        printCenteredText("🔑2.Are you an Admin?", boxWidth);
        printCenteredHorizontalLine(boxWidth);
        printCenteredText("🚪3.Exit the App", boxWidth);

        // Print bottom border
        printCenteredHorizontalBorder(boxWidth);

        // Separate line for choice input
        System.out.println("\nPlease enter your choice (1 or 2 or 3):");
    }

    private static void printCenteredHorizontalBorder(int width) {
        System.out.print("\t\t\t\t\t\t╔");
        for (int i = 0; i < width - 2; i++) {
            System.out.print("═");
        }
        System.out.println("╗");
    }

    private static void printCenteredHorizontalLine(int width) {
        System.out.print("\t\t\t\t\t\t╟");
        for (int i = 0; i < width - 2; i++) {
            System.out.print("─");
        }
        System.out.println("╢");
    }

    private static void printCenteredText(String text, int boxWidth) {
        int padding = (boxWidth - text.length() - 2) / 2;
        System.out.print("\t\t\t\t\t\t║");
        for (int i = 0; i < padding; i++) {
            System.out.print(" ");
        }
        System.out.print(text);
        for (int i = 0; i < boxWidth - text.length() - padding - 2; i++) {
            System.out.print(" ");
        }
        System.out.println("║");
    }

    private static int getUserChoice(Scanner scan) {
        int choice = -1;
        while (true) {
            try {
                choice = Integer.parseInt(scan.nextLine());
                if (choice == 1 || choice == 2 || choice ==3) break;
                else System.out.println("❌ Invalid choice. Please enter 1 or 2 or 3.");
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a number (1 or 2 or 3).");
            }
        }
        return choice;
    }
}
/*
"C:\Program Files\Java\jdk-21\bin\java.exe" "-javaagent:C:\Users\AMAN\IntelliJ IDEA Community Edition 2023.3.2\lib\idea_rt.jar=55185:C:\Users\AMAN\IntelliJ IDEA Community Edition 2023.3.2\bin" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath C:\Users\AMAN\Documents\JAVA_PROGRAM\DS_Project\out\production\DS_Project;C:\Users\AMAN\Downloads\mysql-connector-java-8.0.23.jar ecommerce.DS_PROJECT.Main

							🌟🌟🌟==============================🌟🌟🌟
							    Welcome to the D-MART App 🛍️
							🌟🌟🌟==============================🌟🌟🌟

						╔════════════════════════════════════════════════╗
						║              👤1. Are you a User?              ║
						╟────────────────────────────────────────────────╢
						║             🔑2.Are you an Admin?              ║
						╟────────────────────────────────────────────────╢
						║                🚪3.Exit the App                ║
						╔════════════════════════════════════════════════╗

Please enter your choice (1 or 2 or 3):
`1
❌ Invalid input. Please enter a number (1 or 2 or 3).
1

🔍 Redirecting to User Login...
							=============================
							Enter username: shreya
							Username already exists!! Please enter your password.
							Enter password: shreya@123
							Login successful.

Enter your budget: 20000

Enter search keyword (e.g., product name or category): soap

🎚️ Sort by Price:
1. Ascending 📈
2. Descending 📉
3. By Price & Rating 🏆
4. Exit App 🚪
Enter your choice (1-4): 3

✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨ Welcome to the Product Gallery ✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨
🆔 ID    🏷️ Name                           💼 Brand                     💰 Price                  🔖 Discounted Price     📂 Category             🗂️ Sub-Category         📦 Stock           ⭐ Rating     🔗 Breadcrumbs
➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖
🆔 11754 🏷️ Hamam Neem Tulsi and Aloe V... 💼 Hamam                     💰 65.00                  🔖 60.00                📂 Personal Care        🗂️ Skin Care            📦 446             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 12745 🏷️ Himalaya Extra Moisturizing... 💼 Himalaya                  💰 80.00                  🔖 65.00                📂 Personal Care        🗂️ Baby & Kids          📦 313             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11735 🏷️ Khadi Natural Sandalwood Soap  💼 Khadi                     💰 80.00                  🔖 40.00                📂 Personal Care        🗂️ Skin Care            📦 73              ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11756 🏷️ Khadi Natural Jasmine Soap     💼 Khadi Natural             💰 80.00                  🔖 40.00                📂 Personal Care        🗂️ Skin Care            📦 731             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11717 🏷️ Grace Lime Fresh Soap          💼 Grace                     💰 150.00                 🔖 89.00                📂 Personal Care        🗂️ Skin Care            📦 906             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 12748 🏷️ Johnson's Baby Soap            💼 Johnson's                 💰 160.00                 🔖 135.00               📂 Personal Care        🗂️ Baby & Kids          📦 265             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11719 🏷️ Jo Lime Soap                   💼 Jo                        💰 172.00                 🔖 127.00               📂 Personal Care        🗂️ Skin Care            📦 69              ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11736 🏷️ Godrej No. 1 Sandal & Turme... 💼 Godrej                    💰 189.00                 🔖 149.00               📂 Personal Care        🗂️ Skin Care            📦 805             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11762 🏷️ Himalaya Neem Turmeric Soap    💼                           💰 196.00                 🔖 155.00               📂 Personal Care        🗂️ Skin Care            📦 381             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11732 🏷️ Lux Fresh Splash Water Lily... 💼 Lux                       💰 198.00                 🔖 178.00               📂 Personal Care        🗂️ Skin Care            📦 360             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11724 🏷️ Mild & Clear Pure and Fresh... 💼 Mild & Clear              💰 235.00                 🔖 117.00               📂 Personal Care        🗂️ Skin Care            📦 370             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11709 🏷️ Santoor Sandal & Turmeric Soap 💼 Santoor                   💰 250.00                 🔖 206.00               📂 Personal Care        🗂️ Skin Care            📦 383             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11714 🏷️ Medimix Ayurvedic Natural G... 💼 Medimix                   💰 260.00                 🔖 219.00               📂 Personal Care        🗂️ Skin Care            📦 575             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11778 🏷️ Lux Soft Glow Rose & Vitami... 💼 Lux                       💰 260.00                 🔖 235.00               📂 Personal Care        🗂️ Skin Care            📦 929             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11779 🏷️ Lux Velvet Glow Jasmine & V... 💼 Lux                       💰 260.00                 🔖 239.00               📂 Personal Care        🗂️ Skin Care            📦 890             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11728 🏷️ Lifebuoy Lemon Fresh Soap      💼 Lifebuoy                  💰 270.00                 🔖 240.00               📂 Personal Care        🗂️ Skin Care            📦 714             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11751 🏷️ Santoor Glycerine Soap         💼 Santoor                   💰 270.00                 🔖 135.00               📂 Personal Care        🗂️ Skin Care            📦 327             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11740 🏷️ Park Avenue Luxury Men's Soap  💼 Park                      💰 280.00                 🔖 190.00               📂 Personal Care        🗂️ Skin Care            📦 910             ⭐ ⭐⭐⭐⭐⭐      🔗 Persona...
🆔 11764 🏷️ Patanjali Haldi Chandan Kan... 💼 Patanjali                 💰 22.00                  🔖 21.00                📂 Personal Care        🗂️ Skin Care            📦 505             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11777 🏷️ Cinthol Lime Refreshing Deo... 💼 Cinthol                   💰 40.00                  🔖 37.00                📂 Personal Care        🗂️ Skin Care            📦 454             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 12747 🏷️ Johnson's Baby Soap - Blossoms 💼 Johnson's                 💰 68.00                  🔖 60.00                📂 Personal Care        🗂️ Baby & Kids          📦 208             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11738 🏷️ Khadi Natural Lemon Soap       💼 Khadi                     💰 80.00                  🔖 40.00                📂 Personal Care        🗂️ Skin Care            📦 67              ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 10718 🏷️ Dhanthak's BB Soap             💼 BB                        💰 100.00                 🔖 90.00                📂 Home & Kitchen       🗂️ Detergent & Fabric Care 📦 171             ⭐ ⭐⭐⭐⭐       🔗 Home & ...
🆔 11713 🏷️ Lifebuoy Total 10 Soap         💼 Lifebuoy                  💰 140.00                 🔖 126.00               📂 Personal Care        🗂️ Skin Care            📦 40              ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11769 🏷️ Lifebuoy Neem & Aloe Vera Soap 💼 Lifebuoy                  💰 148.00                 🔖 130.00               📂 Personal Care        🗂️ Skin Care            📦 146             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11703 🏷️ Grace Beauty Soap With Jasm... 💼 Grace                     💰 150.00                 🔖 89.00                📂 Personal Care        🗂️ Skin Care            📦 407             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11704 🏷️ Dettol Original Soap           💼 Dettol                    💰 150.00                 🔖 136.00               📂 Personal Care        🗂️ Skin Care            📦 980             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11696 🏷️ Medimix Ayurvedic Classic 1... 💼 Medimix                   💰 155.00                 🔖 130.00               📂 Personal Care        🗂️ Skin Care            📦 224             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11760 🏷️ Jo Rose & Cream Soap           💼 Jo                        💰 172.00                 🔖 130.00               📂 Personal Care        🗂️ Skin Care            📦 749             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11749 🏷️ Godrej No. 1 Kesar & Milk C... 💼 Godrej                    💰 189.00                 🔖 149.00               📂 Personal Care        🗂️ Skin Care            📦 156             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11773 🏷️ Hand Aid Health Plus Soap      💼 Hand Aid                  💰 190.00                 🔖 95.00                📂 Personal Care        🗂️ Skin Care            📦 994             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11702 🏷️ Lux Velvet Glow Jasmine & V... 💼 Lux                       💰 198.00                 🔖 163.00               📂 Personal Care        🗂️ Skin Care            📦 939             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11758 🏷️ Godrej Cinthol Health Soap ... 💼 Godrej                    💰 224.00                 🔖 193.00               📂 Personal Care        🗂️ Skin Care            📦 462             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11691 🏷️ Mild & Clear Soap With Glyc... 💼 Mild & Clear              💰 235.00                 🔖 117.00               📂 Personal Care        🗂️ Skin Care            📦 309             ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11692 🏷️ Mild & Clear Soap With Glyc... 💼 Mild & Clear              💰 315.00                 🔖 157.00               📂 Personal Care        🗂️ Skin Care            📦 63              ⭐ ⭐⭐⭐⭐       🔗 Persona...
🆔 11767 🏷️ Patanjali Aloe Vera Kanti Soap 💼 Patanjali                 💰 23.00                  🔖 21.00                📂 Personal Care        🗂️ Skin Care            📦 679             ⭐ ⭐⭐⭐        🔗 Persona...
🆔 11725 🏷️ Cinthol Original Deodorant ... 💼 Cinthol                   💰 42.00                  🔖 38.00                📂 Personal Care        🗂️ Skin Care            📦 215             ⭐ ⭐⭐⭐        🔗 Persona...
🆔 11737 🏷️ Khadi Natural Aloevera Soap    💼 Khadi Natural             💰 80.00                  🔖 40.00                📂 Personal Care        🗂️ Skin Care            📦 417             ⭐ ⭐⭐⭐        🔗 Persona...
🆔 11698 🏷️ Mysore Sandal Soap             💼 Mysore                    💰 82.00                  🔖 70.00                📂 Personal Care        🗂️ Skin Care            📦 657             ⭐ ⭐⭐⭐        🔗 Persona...
🆔 11753 🏷️ Mysore Sandal Gold Soap        💼 Mysore                    💰 90.00                  🔖 76.00                📂 Personal Care        🗂️ Skin Care            📦 453             ⭐ ⭐⭐⭐        🔗 Persona...
🆔 11718 🏷️ Godrej No. 1 Lime & Aloe Ve... 💼 Godrej                    💰 189.00                 🔖 149.00               📂 Personal Care        🗂️ Skin Care            📦 377             ⭐ ⭐⭐⭐        🔗 Persona...
🆔 12743 🏷️ Himalaya Gentle Baby Soap      💼 Himalaya                  💰 200.00                 🔖 140.00               📂 Personal Care        🗂️ Baby & Kids          📦 871             ⭐ ⭐⭐⭐        🔗 Persona...
🆔 11053 🏷️ Champion 2 In 1 Soap Dispen... 💼                           💰 200.00                 🔖 99.00                📂 Home & Kitchen       🗂️ Cleaning Tools       📦 95              ⭐ ⭐⭐⭐        🔗 Home & ...
🆔 11731 🏷️ Mysore Luxury Bath Soap        💼 Mysore                    💰 220.00                 🔖 195.00               📂 Personal Care        🗂️ Skin Care            📦 705             ⭐ ⭐⭐⭐        🔗 Persona...
🆔 11742 🏷️ Lux Creamy Prefection Soap     💼 Lux                       💰 330.00                 🔖 255.00               📂 Personal Care        🗂️ Skin Care            📦 706             ⭐ ⭐⭐⭐        🔗 Persona...
🆔 11757 🏷️ Patanjali Neem Kanti Soap      💼 Patanjali                 💰 23.00                  🔖 21.00                📂 Personal Care        🗂️ Skin Care            📦 30              ⭐ ⭐⭐         🔗 Persona...
🆔 11763 🏷️ Pears Oil-Clear & Glow Soap... 💼 Pears                     💰 63.00                  🔖 58.00                📂 Personal Care        🗂️ Skin Care            📦 722             ⭐ ⭐⭐         🔗 Persona...
🆔 11766 🏷️ Rexona Coconut & Olive Oil ... 💼 Rexona                    💰 75.00                  🔖 67.00                📂 Personal Care        🗂️ Skin Care            📦 528             ⭐ ⭐⭐         🔗 Persona...
🆔 11759 🏷️ Khadi Natural Strawberry Soap  💼 Khadi                     💰 80.00                  🔖 40.00                📂 Personal Care        🗂️ Skin Care            📦 779             ⭐ ⭐⭐         🔗 Persona...
🆔 11741 🏷️ Khadi Natural Rosewater Soap   💼 Khadi                     💰 80.00                  🔖 40.00                📂 Personal Care        🗂️ Skin Care            📦 36              ⭐ ⭐⭐         🔗 Persona...
🆔 11768 🏷️ Lux Creamy Perfection Soap     💼 Lux                       💰 83.00                  🔖 77.00                📂 Personal Care        🗂️ Skin Care            📦 782             ⭐ ⭐⭐         🔗 Persona...
🆔 11730 🏷️ Dettol Intense Cool 2X Ment... 💼 Dettol                    💰 150.00                 🔖 138.00               📂 Personal Care        🗂️ Skin Care            📦 479             ⭐ ⭐⭐         🔗 Persona...
🆔 11734 🏷️ Godrej Cinthol Lime Soap       💼 Godrej                    💰 155.00                 🔖 144.00               📂 Personal Care        🗂️ Skin Care            📦 675             ⭐ ⭐⭐         🔗 Persona...
🆔 11700 🏷️ Lux Soft Glow Rose & Vitami... 💼 Lux                       💰 198.00                 🔖 163.00               📂 Personal Care        🗂️ Skin Care            📦 265             ⭐ ⭐⭐         🔗 Persona...
🆔 11755 🏷️ Chandan Sparsh Soap            💼 Chandan Sparsh            💰 200.00                 🔖 115.00               📂 Personal Care        🗂️ Skin Care            📦 641             ⭐ ⭐⭐         🔗 Persona...
🆔 11708 🏷️ Santoor Sandal & Turmeric Soap 💼 Santoor                   💰 222.00                 🔖 182.00               📂 Personal Care        🗂️ Skin Care            📦 864             ⭐ ⭐⭐         🔗 Persona...
🆔 11711 🏷️ Cinthol Lime Refreshing Deo... 💼 Cinthol                   💰 243.00                 🔖 215.00               📂 Personal Care        🗂️ Skin Care            📦 320             ⭐ ⭐⭐         🔗 Persona...
🆔 11045 🏷️ Beezy 2 In 1 Soap Dispenser... 💼 Beezy                     💰 249.00                 🔖 99.00                📂 Home & Kitchen       🗂️ Cleaning Tools       📦 836             ⭐ ⭐⭐         🔗 Home & ...
🆔 11710 🏷️ Dettol Intense Cool 2X Ment... 💼 Dettol                    💰 322.00                 🔖 289.00               📂 Personal Care        🗂️ Skin Care            📦 385             ⭐ ⭐⭐         🔗 Persona...
🆔 11720 🏷️ Dettol Skincare Soap           💼 Dettol                    💰 322.00                 🔖 286.00               📂 Personal Care        🗂️ Skin Care            📦 751             ⭐ ⭐⭐         🔗 Persona...
🆔 12744 🏷️ Johnson's Baby Soap            💼 Johnson's                 💰 345.00                 🔖 285.00               📂 Personal Care        🗂️ Baby & Kids          📦 27              ⭐ ⭐⭐         🔗 Persona...
🆔 11750 🏷️ Dove Pink Rosa Bathing Soap    💼 Dove                      💰 490.00                 🔖 425.00               📂 Personal Care        🗂️ Skin Care            📦 790             ⭐ ⭐⭐         🔗 Persona...
🆔 11776 🏷️ Godrej No.1 Lime Aloe Vera ... 💼 Godrej                    💰 10.00                  🔖 9.00                 📂 Personal Care        🗂️ Skin Care            📦 934             ⭐ ⭐          🔗 Persona...
🆔 11771 🏷️ Pears Pure Gentle Soap         💼 Pears                     💰 54.00                  🔖 49.00                📂 Personal Care        🗂️ Skin Care            📦 878             ⭐ ⭐          🔗 Persona...
🆔 11743 🏷️ Khadi Natural Neem Tulsi Soap  💼 Khadi                     💰 80.00                  🔖 40.00                📂 Personal Care        🗂️ Skin Care            📦 531             ⭐ ⭐          🔗 Persona...
🆔 11748 🏷️ Khadi Natural Chandan Haldi... 💼 Khadi                     💰 80.00                  🔖 40.00                📂 Personal Care        🗂️ Skin Care            📦 140             ⭐ ⭐          🔗 Persona...
🆔 11715 🏷️ Cinthol Lime Refreshing Deo... 💼 Cinthol                   💰 109.00                 🔖 101.00               📂 Personal Care        🗂️ Skin Care            📦 394             ⭐ ⭐          🔗 Persona...
🆔 11729 🏷️ Chandrika Ayurvedic Soap       💼 Chandrika                 💰 135.00                 🔖 111.00               📂 Personal Care        🗂️ Skin Care            📦 328             ⭐ ⭐          🔗 Persona...
🆔 11775 🏷️ Godrej No.1 Jasmine Soap       💼 Godrej                    💰 140.00                 🔖 125.00               📂 Personal Care        🗂️ Skin Care            📦 394             ⭐ ⭐          🔗 Persona...
🆔 11761 🏷️ Godrej No.1 Rose Water & Al... 💼 Godrej                    💰 145.00                 🔖 129.00               📂 Personal Care        🗂️ Skin Care            📦 65              ⭐ ⭐          🔗 Persona...
🆔 11721 🏷️ Dettol Skincare Soap           💼 Dettol                    💰 150.00                 🔖 135.00               📂 Personal Care        🗂️ Skin Care            📦 453             ⭐ ⭐          🔗 Persona...
🆔 11765 🏷️ Camay Natural Soap             💼 Camay                     💰 150.00                 🔖 129.00               📂 Personal Care        🗂️ Skin Care            📦 101             ⭐ ⭐          🔗 Persona...
🆔 11707 🏷️ Santoor Sandal & Turmeric Soap 💼 Santoor                   💰 150.00                 🔖 135.00               📂 Personal Care        🗂️ Skin Care            📦 776             ⭐ ⭐          🔗 Persona...
🆔 11701 🏷️ Grace Gentle Touch Soap        💼 Grace                     💰 150.00                 🔖 89.00                📂 Personal Care        🗂️ Skin Care            📦 566             ⭐ ⭐          🔗 Persona...
🆔 11745 🏷️ Margo Original Neem Soap       💼 Margo                     💰 160.00                 🔖 145.00               📂 Personal Care        🗂️ Skin Care            📦 537             ⭐ ⭐          🔗 Persona...
🆔 11726 🏷️ Cinthol Original Deodorant ... 💼 Cinthol                   💰 163.00                 🔖 151.00               📂 Personal Care        🗂️ Skin Care            📦 126             ⭐ ⭐          🔗 Persona...
🆔 11744 🏷️ Jo Almond & Cream Soap         💼 Jo                        💰 172.00                 🔖 132.00               📂 Personal Care        🗂️ Skin Care            📦 927             ⭐ ⭐          🔗 Persona...
🆔 11716 🏷️ Cinthol Lime Refreshing Deo... 💼 Cinthol                   💰 204.00                 🔖 178.00               📂 Personal Care        🗂️ Skin Care            📦 653             ⭐ ⭐          🔗 Persona...
🆔 11746 🏷️ Santoor  Sandal and Almond ... 💼 Santoor                   💰 209.00                 🔖 185.00               📂 Personal Care        🗂️ Skin Care            📦 979             ⭐ ⭐          🔗 Persona...
🆔 11697 🏷️ Medimix Ayurvedic Classic 1... 💼 Medimix                   💰 220.00                 🔖 179.00               📂 Personal Care        🗂️ Skin Care            📦 578             ⭐ ⭐          🔗 Persona...
🆔 11727 🏷️ Medimix Ayurvedic Sandal Soap  💼 Medimix                   💰 220.00                 🔖 198.00               📂 Personal Care        🗂️ Skin Care            📦 965             ⭐ ⭐          🔗 Persona...
🆔 11722 🏷️ Godrej Cinthol Cool Deo Soap   💼 Godrej                    💰 224.00                 🔖 196.00               📂 Personal Care        🗂️ Skin Care            📦 398             ⭐ ⭐          🔗 Persona...
🆔 11706 🏷️ Liril Lime & Tea Tree Oil Soap 💼 Liril                     💰 235.00                 🔖 189.00               📂 Personal Care        🗂️ Skin Care            📦 351             ⭐ ⭐          🔗 Persona...
🆔 11699 🏷️ Mysore Sandal Soap             💼 Mysore                    💰 243.00                 🔖 218.00               📂 Personal Care        🗂️ Skin Care            📦 582             ⭐ ⭐          🔗 Persona...
🆔 11752 🏷️ Godrej Cinthol Original Soap   💼 Godrej                    💰 250.00                 🔖 225.00               📂 Personal Care        🗂️ Skin Care            📦 704             ⭐ ⭐          🔗 Persona...
🆔 11774 🏷️ Lifebuoy Total 10 Silver Sh... 💼 Lifebuoy                  💰 270.00                 🔖 215.00               📂 Personal Care        🗂️ Skin Care            📦 848             ⭐ ⭐          🔗 Persona...
🆔 11705 🏷️ Dettol Original Soap           💼 Dettol                    💰 322.00                 🔖 302.00               📂 Personal Care        🗂️ Skin Care            📦 983             ⭐ ⭐          🔗 Persona...
🆔 11747 🏷️ Park Avenue Cool Blue Fragr... 💼 Park                      💰 340.00                 🔖 259.00               📂 Personal Care        🗂️ Skin Care            📦 984             ⭐ ⭐          🔗 Persona...

✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨ End of Product List ✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨


🛠️ Options:
1️. View a Product
2️. View Recently Viewed Products
3️. View Order History
4️. View Cart 🛒
5. Go Back to Search 🔍
6. Exit App 🚪
1
Enter the ID of the product you want to view: 11706

Product Details:
11706 Liril Lime & Tea Tree Oil Soap Liril           235.00     189.00          Personal Care   Skin Care       Personal Care > Skin Care 351        ⭐

Options:
1. Add to Cart
2. Buy Now
3. View Cart
4. Back to Product List
5. Exit the App
1
🛒 Product added to cart successfully!

🛠️ Options:
1️. View a Product
2️. View Recently Viewed Products
3️. View Order History
4️. View Cart 🛒
5. Go Back to Search 🔍
6. Exit App 🚪
1
Enter the ID of the product you want to view: 11704

Product Details:
11704 Dettol Original Soap           Dettol          150.00     136.00          Personal Care   Skin Care       Personal Care > Skin Care 980        ⭐⭐⭐⭐

Options:
1. Add to Cart
2. Buy Now
3. View Cart
4. Back to Product List
5. Exit the App
2
Enter quantity: 2

👤 Enter your username: shreya
📍 Please enter delivery address:
ayodhya
✅ Order has been placed successfully.

Would you like to:
1️⃣ View Order History
2️⃣ Search for Items 🔍
3️ Exit the App
1

📜 ===================================================================== Order History ==================================================================== 📜
🛒 Order ID   👤 Username        🆔 Product ID 📅 Order Date      📍 Delivery Address               💳 Payment Mode    📦 Status      💰 Order Amount  Quantity
➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖
🛒 12         👤 shreya          🆔 11704      📅 2024-11-15      📍 ayodhya                        💳 Cash on Delivery 📦 Placed     💰 272.0       2
📜 ========================================================================================================================================================= 📜


🛠️ Options:
1️. View a Product
2️. View Recently Viewed Products
3️. View Order History
4️. View Cart 🛒
5. Go Back to Search 🔍
6. Exit App 🚪
2

Recently Viewed Products:
11706 Liril Lime & Tea Tree Oil Soap Liril           235.00     189.00          Personal Care   Skin Care       Personal Care > Skin Care 351        ⭐
11704 Dettol Original Soap           Dettol          150.00     136.00          Personal Care   Skin Care       Personal Care > Skin Care 980        ⭐⭐⭐⭐

🛠️ Options:
1️. View a Product
2️. View Recently Viewed Products
3️. View Order History
4️. View Cart 🛒
5. Go Back to Search 🔍
6. Exit App 🚪
4

🛒 Your Cart Options:
1. View Normal Cart
2. View Magic Cart
3. Back to Main Menu
4. Exit the App
1

🛒 Your Normal Cart:
🆔 11706 🏷️ Liril Lime & Tea Tree Oil Soap 💰 $235.00

1. Proceed to Checkout
2. Remove Items from Cart
3. Back to Product List
4. Exit the App
1

Proceeding to checkout...
Enter quantity: 1
👤 Enter your username: shreya
📍 Enter delivery address: ayodhya
✅ Order for product Liril Lime & Tea Tree Oil Soap has been placed successfully.
🛒 Thank you for shopping! All items have been processed.

🛠️ Options:
1️. View a Product
2️. View Recently Viewed Products
3️. View Order History
4️. View Cart 🛒
5. Go Back to Search 🔍
6. Exit App 🚪
5

🔄 Would you like to search for more products? (y/n): y

Enter search keyword (e.g., product name or category): bottle

🎚️ Sort by Price:
1. Ascending 📈
2. Descending 📉
3. By Price & Rating 🏆
4. Exit App 🚪
Enter your choice (1-4): 3

✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨ Welcome to the Product Gallery ✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨
🆔 ID    🏷️ Name                           💼 Brand                     💰 Price                  🔖 Discounted Price     📂 Category             🗂️ Sub-Category         📦 Stock           ⭐ Rating     🔗 Breadcrumbs
➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖
🆔 11405 🏷️ Aura Water Bottle - Blue       💼                           💰 60.00                  🔖 49.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 356             ⭐ ⭐⭐⭐⭐⭐      🔗 Home & ...
🆔 11410 🏷️ Aura Water Bottle - Grey       💼                           💰 60.00                  🔖 49.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 443             ⭐ ⭐⭐⭐⭐⭐      🔗 Home & ...
🆔 11411 🏷️ DHomes Plastic PET Bottle -... 💼 DHomes                    💰 110.00                 🔖 79.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 427             ⭐ ⭐⭐⭐⭐⭐      🔗 Home & ...
🆔 11402 🏷️ Nayasa Glacier Plastic Bottle  💼                           💰 140.00                 🔖 79.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 525             ⭐ ⭐⭐⭐⭐⭐      🔗 Home & ...
🆔 9354  🏷️ Coca-Cola Bottle               💼 Coca-Cola                 💰 160.00                 🔖 130.00               📂 Dairy & Beverages    🗂️ Beverages            📦 301             ⭐ ⭐⭐⭐⭐⭐      🔗 Dairy &...
🆔 8627  🏷️ RRO Til Dil Premium Til Oil... 💼 RRO                       💰 184.00                 🔖 152.00               📂 Grocery              🗂️ Cooking Oil          📦 135             ⭐ ⭐⭐⭐⭐⭐      🔗 Grocery...
🆔 11398 🏷️ Jolly Aqua Cool Water Bottl... 💼 Jolly                     💰 50.00                  🔖 39.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 111             ⭐ ⭐⭐⭐⭐       🔗 Home & ...
🆔 11412 🏷️ DHomes Plastic PET Bottle -... 💼 DHomes                    💰 110.00                 🔖 79.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 785             ⭐ ⭐⭐⭐⭐       🔗 Home & ...
🆔 11403 🏷️ Cello Diamond Plastic Bottle   💼 Cello                     💰 139.00                 🔖 79.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 665             ⭐ ⭐⭐⭐⭐       🔗 Home & ...
🆔 10115 🏷️ Veeba Truly Tomato Ketchup ... 💼 Veeba                     💰 149.00                 🔖 99.00                📂 Packaged Food        🗂️ Ketchup & Sauce      📦 240             ⭐ ⭐⭐⭐⭐       🔗 Package...
🆔 9352  🏷️ Thums Up Bottle                💼 Thums Up                  💰 160.00                 🔖 145.00               📂 Dairy & Beverages    🗂️ Beverages            📦 923             ⭐ ⭐⭐⭐⭐       🔗 Dairy &...
🆔 11406 🏷️ Milton Prism Plastic Bottle... 💼 Milton                    💰 180.00                 🔖 99.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 14              ⭐ ⭐⭐⭐⭐       🔗 Home & ...
🆔 11394 🏷️ Cello Sportigo Water Bottle... 💼 Cello                     💰 193.00                 🔖 129.00               📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 614             ⭐ ⭐⭐⭐⭐       🔗 Home & ...
🆔 11407 🏷️ Milton Thunder Plastic Bottle  💼 Milton                    💰 199.00                 🔖 99.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 234             ⭐ ⭐⭐⭐⭐       🔗 Home & ...
🆔 11117 🏷️ Stainless Steel Champ Fridg... 💼                           💰 300.00                 🔖 199.00               📂 Home & Kitchen       🗂️ Home & Kitchen       📦 599             ⭐ ⭐⭐⭐⭐       🔗 Home & ...
🆔 11396 🏷️ Nayasa Pineapple PET Bottle... 💼                           💰 400.00                 🔖 229.00               📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 851             ⭐ ⭐⭐⭐⭐       🔗 Home & ...
🆔 11393 🏷️ Dr. Copper Water Bottle        💼 Dr. Copper                💰 1199.00                🔖 999.00               📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 522             ⭐ ⭐⭐⭐⭐       🔗 Home & ...
🆔 8607  🏷️ Figaro Extra Virgin Olive O... 💼 Figaro                    💰 1199.00                🔖 765.00               📂 Grocery              🗂️ Cooking Oil          📦 356             ⭐ ⭐⭐⭐⭐       🔗 Grocery...
🆔 13314 🏷️ Godrej Mr Magic Handwash (1... 💼 Godrej                    💰 40.00                  🔖 35.00                📂 Personal Care        🗂️ Personal Hygiene     📦 901             ⭐ ⭐⭐⭐        🔗 Persona...
🆔 11399 🏷️ Nayasa Turtle Plastic Bottle   💼 Nayasa                    💰 119.00                 🔖 59.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 942             ⭐ ⭐⭐⭐        🔗 Home & ...
🆔 11395 🏷️ Cello H2O Squaremate Water ... 💼 Cello                     💰 206.00                 🔖 149.00               📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 879             ⭐ ⭐⭐⭐        🔗 Home & ...
🆔 11401 🏷️ Ski Gym Shaker Bottle - Ass... 💼                           💰 250.00                 🔖 99.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 131             ⭐ ⭐⭐⭐        🔗 Home & ...
🆔 8628  🏷️ RRO Til Dil Premium Til Oil... 💼 RRO                       💰 360.00                 🔖 299.00               📂 Grocery              🗂️ Cooking Oil          📦 660             ⭐ ⭐⭐⭐        🔗 Grocery...
🆔 8634  🏷️ Idhayam Sesame Oil Bottle      💼 Idhayam                   💰 386.00                 🔖 329.00               📂 Grocery              🗂️ Cooking Oil          📦 406             ⭐ ⭐⭐⭐        🔗 Grocery...
🆔 11404 🏷️ Lock & Lock Aqua Easy Grip ... 💼 Lock & Lock               💰 560.00                 🔖 249.00               📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 130             ⭐ ⭐⭐⭐        🔗 Home & ...
🆔 10633 🏷️ Bottle Gourd (Dudhi)           💼                           💰 50.00                  🔖 46.00                📂 Fruits & Vegetables  🗂️ Fruits & Vegetables  📦 727             ⭐ ⭐⭐         🔗 Fruits ...
🆔 11408 🏷️ Aura Water Bottle - Green      💼                           💰 60.00                  🔖 49.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 73              ⭐ ⭐⭐         🔗 Home & ...
🆔 10850 🏷️ Reflect Dishwash Liquid Bottle 💼 Reflect                   💰 89.00                  🔖 75.00                📂 Specials             🗂️ Specials             📦 428             ⭐ ⭐⭐         🔗 Specials
🆔 8612  🏷️ Fortune Kachi Ghani Mustard... 💼 Fortune                   💰 226.00                 🔖 185.00               📂 Grocery              🗂️ Cooking Oil          📦 953             ⭐ ⭐⭐         🔗 Grocery...
🆔 8620  🏷️ RRO Mastdil Premium Mustard... 💼 RRO                       💰 235.00                 🔖 195.00               📂 Grocery              🗂️ Cooking Oil          📦 939             ⭐ ⭐⭐         🔗 Grocery...
🆔 11069 🏷️ Lock & Lock Water Bottle Cl... 💼 Lock & Lock               💰 255.00                 🔖 229.00               📂 Home & Kitchen       🗂️ Cleaning Tools       📦 458             ⭐ ⭐⭐         🔗 Home & ...
🆔 11680 🏷️ Garden Spray Pump Bottle - ... 💼                           💰 300.00                 🔖 199.00               📂 Home & Kitchen       🗂️ Home Utility         📦 234             ⭐ ⭐⭐         🔗 Home & ...
🆔 8629  🏷️ Tilsona Til Oil Bottle         💼 Tilsona                   💰 365.00                 🔖 315.00               📂 Grocery              🗂️ Cooking Oil          📦 187             ⭐ ⭐⭐         🔗 Grocery...
🆔 11400 🏷️ Crystal Stainless Steel Col... 💼 Crystal                   💰 460.00                 🔖 249.00               📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 364             ⭐ ⭐⭐         🔗 Home & ...
🆔 11409 🏷️ Cello Venice Plastic Bottle    💼 Cello                     💰 129.00                 🔖 99.00                📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 970             ⭐ ⭐          🔗 Home & ...
🆔 8630  🏷️ Tilsona Til Oil Bottle         💼 Tilsona                   💰 194.00                 🔖 167.00               📂 Grocery              🗂️ Cooking Oil          📦 370             ⭐ ⭐          🔗 Grocery...
🆔 10746 🏷️ Godrej Ezee Liquid Detergen... 💼 Ezee                      💰 205.00                 🔖 190.00               📂 Home & Kitchen       🗂️ Detergent & Fabric Care 📦 924             ⭐ ⭐          🔗 Home & ...
🆔 8618  🏷️ Tez Mustard Kachchi Ghani O... 💼 Tez                       💰 225.00                 🔖 202.00               📂 Grocery              🗂️ Cooking Oil          📦 976             ⭐ ⭐          🔗 Grocery...
🆔 11397 🏷️ Crystal Stainless Steel Sil... 💼 Crystal                   💰 460.00                 🔖 249.00               📂 Home & Kitchen       🗂️ Cookware & Serveware 📦 945             ⭐ ⭐          🔗 Home & ...
🆔 8608  🏷️ Figaro Extra Virgin Olive O... 💼 Figaro                    💰 649.00                 🔖 405.00               📂 Grocery              🗂️ Cooking Oil          📦 382             ⭐ ⭐          🔗 Grocery...

✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨ End of Product List ✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨


🛠️ Options:
1️. View a Product
2️. View Recently Viewed Products
3️. View Order History
4️. View Cart 🛒
5. Go Back to Search 🔍
6. Exit App 🚪
6

👋 Thank you for using E-commerce App! Goodbye!

Process finished with exit code 0



/// ADMIN SIDE
"C:\Program Files\Java\jdk-21\bin\java.exe" "-javaagent:C:\Users\AMAN\IntelliJ IDEA Community Edition 2023.3.2\lib\idea_rt.jar=55249:C:\Users\AMAN\IntelliJ IDEA Community Edition 2023.3.2\bin" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath C:\Users\AMAN\Documents\JAVA_PROGRAM\DS_Project\out\production\DS_Project;C:\Users\AMAN\Downloads\mysql-connector-java-8.0.23.jar ecommerce.DS_PROJECT.Main

							🌟🌟🌟==============================🌟🌟🌟
							    Welcome to the D-MART App 🛍️
							🌟🌟🌟==============================🌟🌟🌟

						╔════════════════════════════════════════════════╗
						║              👤1. Are you a User?              ║
						╟────────────────────────────────────────────────╢
						║             🔑2.Are you an Admin?              ║
						╟────────────────────────────────────────────────╢
						║                🚪3.Exit the App                ║
						╔════════════════════════════════════════════════╗

Please enter your choice (1 or 2 or 3):
2

🔐 Redirecting to Admin Login...
Enter admin password: Prishmi@5730
What operation you want to perform
1.Want to check all users details?
2.Check revenue
0.Skip
Enter your choice:
1
User Details in lexicographical order:
Username: Bhumi
Address: null
Age: 19
-------------------------------
Username: Pritee Pardeshi
Address: null
Age: 19
-------------------------------
Username: shreya
Address: null
Age: 18
-------------------------------
What operation you want to perform
1.Want to check all users details?
2.Check revenue
0.Skip
Enter your choice:
2
Want to generate
1.Daily Revenue
2.Monthly Revenue
3.Yearly Revenue
0.Skip
Enter Your Choice:
2
Enter the year(YYYY):
2024
Enter the month(MM):
11
Total revenue for 2024-11 is: Rs.2378.0
Want to check more details(yes/no):
y
Want to generate
1.Daily Revenue
2.Monthly Revenue
3.Yearly Revenue
0.Skip
Enter Your Choice:
3
Enter year:
2024
Total revenue for the year 2024 is: Rs.2378.0
Want to check more details(yes/no):
y
Want to generate
1.Daily Revenue
2.Monthly Revenue
3.Yearly Revenue
0.Skip
Enter Your Choice:
0
What operation you want to perform
1.Want to check all users details?
2.Check revenue
0.Skip
Enter your choice:
0

Process finished with exit code 0
 */
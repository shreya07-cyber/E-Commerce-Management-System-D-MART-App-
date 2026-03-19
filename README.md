# 🛍️ E-Commerce Management System (D-MART App)

A Java-based console e-commerce application with personalized product recommendations, budget-based filtering, dynamic cart management, and full purchase history — backed by a MySQL database via JDBC.

---

## 📌 Overview

Online shoppers are often overwhelmed by too many product choices. This system addresses that by offering a personalized recommendation engine powered by user preferences and budget constraints, combined with a dynamic cart and persistent order history.

---

## ✨ Key Features

- **User Authentication** — Register/login with password validation and limited login attempts
- **Personalized Recommendations** — Suggests products based on user preferences and past behavior
- **Budget-Based Filtering** — Filters product listings to match the user's specified budget
- **Dynamic Shopping Cart** — Add, view, and manage cart items using a LinkedList
- **Recent Orders Queue** — Tracks the last 5 orders using a Queue (FIFO)
- **Purchase History** — Stores all completed orders in MySQL via JDBC
- **Admin Panel** — View all users (sorted lexicographically) and generate daily/monthly/yearly revenue reports
- **Binary Search Tree** — Used for efficient product search and retrieval
- **Recently Viewed Products** — Tracked using a Stack (LIFO)

---

## 🧰 Data Structures Used

| Data Structure | Usage |
|----------------|-------|
| `Array` | Product catalog storage |
| `LinkedList` | Shopping cart management |
| `Queue` | Recent orders (last 5) |
| `Stack` | Recently viewed products |
| `Binary Tree` | Product search and sorting |

---

## 🗂️ Tech Stack

- **Language:** Java (JDK 21)
- **Database:** MySQL
- **Connectivity:** JDBC (`mysql-connector-java-8.0.23`)
- **IDE:** IntelliJ IDEA

---

## 🗄️ Database Schema

**Table: `user`**
```sql
CREATE TABLE user (
    Username VARCHAR(50) PRIMARY KEY,
    gender   VARCHAR(10),
    age      INT,
    password VARCHAR(100),
    address  VARCHAR(255)
);
```

**Table: `orders`** *(stores purchase history)*
```sql
CREATE TABLE orders (
    order_id    INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50),
    product_id  INT,
    quantity    INT,
    total_price DECIMAL(10,2),
    order_date  DATE
);
```

---

## ⚙️ Setup & Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd DS_Project
   ```

2. **Configure MySQL**
   - Create a database named `ecommerce`
   - Run the schema SQL above to create the required tables
   - Import the product dataset

3. **Update DB credentials** in `JDBC.java`
   ```java
   private static final String JDBC_URL = "jdbc:mysql://localhost:3306/ecommerce";
   private static final String USER = "root";
   private static final String PASSWORD = "your_password";
   ```

4. **Add the JDBC driver** to your classpath:
   ```
   mysql-connector-java-8.0.23.jar
   ```

5. **Run the application**
   ```bash
   javac -cp .;mysql-connector-java-8.0.23.jar ecommerce/DS_PROJECT/Main.java
   java  -cp .;mysql-connector-java-8.0.23.jar ecommerce.DS_PROJECT.Main
   ```

---

## 🔄 Application Workflow

```
Launch App
    ├── User Login / Register
    │       ↓
    │   Enter Preferences + Budget
    │       ↓
    │   Browse Filtered Products
    │       ↓
    │   View Recommendations
    │       ↓
    │   Add to Cart → Checkout → Order stored in DB
    │
    └── Admin Login
            ↓
        View All Users (sorted)
            ↓
        Generate Revenue Reports (Daily / Monthly / Yearly)
```

---

## 👤 My Contribution

- Designed and implemented the **recommendation logic** in Java
- Wrote the **JDBC integration** for all database operations
- Designed the **MySQL schema** for users and order history

---

## 🚀 Future Plans

- Integrate **deep learning** for smarter, context-aware recommendations
- Add **real-time inventory sync** with the database
- Build a **GUI frontend** (JavaFX or web-based)
- Implement **password hashing** for secure authentication

---


---

*Built as a group mini-project to demonstrate core Data Structures concepts in a real-world Java application.*

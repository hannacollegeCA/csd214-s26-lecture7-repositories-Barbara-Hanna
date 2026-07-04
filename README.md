***

## 🛑 CSD214 S26 Lecture 7: The Repository Pattern & Abstraction Layers
This repository serves as the official base for the [**CSD214 Lecture 7 In-Class Exercise and Lab 5 Preparation**](https://docs.google.com/document/d/1naWgzt2X1AGIbOvj9BiYaqkcTuNx5Z33HAvuHc7JQRA/edit?usp=sharing). Building upon the JPA and Hibernate mappings established in Lecture 6, we are now abstracting our data-access logic behind a clean **Repository Layer** to decouple our database queries from our core CLI business logic [6, 7].

### **Repository Implementation Overview**
The `bookstore.repositories` package contains the boundary layers designed to isolate our database plumbing (the `EntityManager` and transactions) from the user interface [6, 7]:

*   **Decoupled Architecture (Outcome 4.2):** We define an `IRepository<T>` interface acting as a generic contract [6]. The CLI controller (`App.java`) is completely isolated from database transactions and Hibernate sessions [6].
*   **The "Upsert" Pattern:** `ProductRepository.java` handles both creation (`persist`) and updating (`merge`) dynamically inside its `save()` method, hiding persistence complexities [7].
*   **Optimized Aggregate Operations:** Includes an optimized `count()` query to verify stock size [7] and a bulk `deleteAll()` operation to drop database records in a single transactional query, avoiding the performance penalties of the N+1 Query Problem.
*   **Polymorphic Persistence:** Safely persists and retrieves books, magazines, tickets, tires, and batteries polymorphically inside a Single-Table inheritance structure.

***
***

# Bookstore CLI Application
- for the csd214 course summer 26 delivery: [course outline](https://welearn.saultcollege.ca/shared/CourseOutlines/csd214_Course_Outline.pdf)
> - [git repository](https://github.com/fcarella/lab1-exercise-fred-carella-csd214-s26)
> - [based on bookstore (git repository): ](https://github.com/fcarella/bookstore-2026-01-30)
> - [this exercise is covered in this lecture...](https://docs.google.com/document/d/1naWgzt2X1AGIbOvj9BiYaqkcTuNx5Z33HAvuHc7JQRA/edit?usp=sharing)
- A console-based Java application for managing a bookstore inventory, performing sales, and tracking cash flow. This project demonstrates object-oriented programming concepts including inheritance, polymorphism, and interface implementation in Java 25.

## Features

*   **Inventory Management (Polymorphic DTO Layer):**
    *   **Books:** Manage items with Title, Author, Price, and Copies.
    *   **Magazines:** Manage periodicals with Order Quantity and Issue Date.
    *   **Disc Magazines:** Specialized magazines that include a disc.
    *   **Tickets:** Simple saleable items with a description and price.
    *   **Vehicle Parts (Lab 1 Exercise):** Expanded inventory to include **Tires** (Diameter) and **Batteries** (Cold Cranking Amps) via a new Automotive hierarchy.
*   **Data Access Abstraction (Repository Pattern):**
    *   Clean separation of presentation logic (DTOs) from database schemas (Entities) [6].
    *   A generic repository contract (`IRepository`) to standardise CRUD lifecycle operations [6].
    *   Encapsulated Hibernate transactions and "Upsert" logic within the data layer [6].
*   **CRUD Operations:** Create, Read, Update, and Delete items.
*   **Sales System:** Sell items to decrement inventory count and increase the Cash Till total.
*   **Data Generation:** Uses `JavaFaker` to populate the database with realistic dummy data for both Bookstore and Auto-Shop departments.
*   **Menu System:** Interactive console menu for navigation.

## Class Hierarchy

![Class Diagram](documentation/bookstore-2026-01-30-142617.png)

The hierarchy implements the following structure:
*   **SaleableItem (Interface):** Defines `sellItem()` and `getPrice()`.
*   **Editable (Abstract):** Handles console input/output and parsing.
*   **Publication:** Base class for Books and Magazines (Title, Price, Copies).
*   **VehiclePart (Lab 1 Exercise):** Base abstract class for the automotive department (Manufacturer, Price).
    *   `Tire` and `Battery` concrete classes implement specific automotive attributes.

## Prerequisites

*   **Java JDK:** Version 25
*   **Maven:** 3.6+
*   **Docker Desktop:** To host containerized MySQL

## Dependencies

*   [JavaFaker](https://github.com/DiUS/java-faker) (1.0.2): For generating random test data.
*   [JUnit 5](https://junit.org/junit5/) (5.10.0): For unit testing.
*   [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) (8.2.0): To establish connection driver channels.
*   [Hibernate Core](https://hibernate.org/) (6.6.1.Final): JPA Implementation engine.

## How to Run

1.  **Start your database container:**
    ```bash
    docker-compose up -d
    ```

2.  **Compile the project:**
    ```bash
    mvn clean compile
    ```

3.  **Run the application:**
    ```bash
    mvn exec:java -Dexec.mainClass="bookstore.Main"
    ```

## Usage

Upon starting, the application will automatically populate the database if it detects an empty schema. You will see the following menu:

```text
***********************
 1. Add Items (Repository Save)
 2. Edit Items (Repository Save/Update)
 3. Delete Items (Repository Delete)
 4. Sell item(s) (Logic & Repo Sync)
 5. List items (Polymorphic Filtering)
99. Quit
***********************
```

## Lab 5 Reflection

### 1. The Power of the Interface

When I swapped the `InMemoryListRepository` for the `InMemoryMapRepository`, and later for the SQL-based repositories (`H2Repository` and `MySqlRepository`), I did **not** modify a single line of code inside `App.java`.  
This is the direct result of using the `IRepository<T>` interface as an abstraction boundary.

The application interacts only with the interface:

`IRepository<ProductEntity> repo;`

Because all repository implementations share the same method signatures (`save`, `findAll`, `findById`, `delete`, `count`, etc.), the CLI logic remains unchanged regardless of the underlying storage engine.

#### How this enables multi‑developer productivity

Interfaces allow multiple developers to work independently:

- One developer can implement MySQL.
- Another can implement H2.
- Another can optimize the HashMap engine.
- Another can extend the ArrayList engine.

None of them need to coordinate changes inside the application layer.  
This reduces merge conflicts, increases parallel development, and ensures that new features or engines can be added without breaking existing functionality.

Polymorphism is not just a language feature. it is a productivity multiplier in real software teams.

---

### 2. Algorithmic Complexity (O(n) vs O(1))

Implementing the `InMemoryListRepository` made it clear that searching for a product by ID requires scanning the entire list:

`
for (ProductEntity p : list) {
    if (p.getProductId().equals(id)) return p;
}`

This is **O(n)** — the time grows linearly with the number of items.

In contrast, the `InMemoryMapRepository` uses:

`map.get(productId);`

This is **O(1)** — constant time, regardless of dataset size.

#### Why O(1) matters in enterprise systems

On small datasets, both approaches feel identical to the user.  
But in real enterprise environments:

- Inventory systems may contain millions of products.
- APIs may receive thousands of requests per second.
- Slow lookups cascade into slow pages, slow reports, and slow user experiences.

An O(n) lookup becomes a bottleneck.  
An O(1) lookup scales effortlessly.

- demonstrates that algorithmic complexity is not an academic detail. it is a core architectural concern that directly impacts performance, scalability, and user satisfaction.

---

### 3. Volatility vs Persistence

The RAM-based repositories (ArrayList and HashMap) behave very differently from the SQL-based repositories (H2 and MySQL).

#### RAM Engines (ArrayList / HashMap)
**Advantages:**
- Extremely fast
- No external dependencies
- Perfect for early development
- Ideal for unit testing
- Zero configuration

**Disadvantages:**
- Data disappears when the program ends
- Not suitable for multi-user environments
- No durability or backup

#### SQL Engines (H2 / MySQL)
**Advantages:**
- Persistent storage
- Supports large datasets
- Enables integration testing
- Suitable for production
- Provides ACID guarantees

**Disadvantages:**
- Slower than RAM
- Requires configuration
- Depends on drivers and dialects

#### Conclusion

- **In-memory repositories** are superior during early development, prototyping, and fast iteration.
- **SQL repositories** are superior when reliability, persistence, and real-world behavior matter.

- shows how both can coexist under the same interface, enabling flexible development workflows.

---

### 4. The Manual IoC Experience

Before this lab, classes instantiated their own dependencies using `new`, which tightly couples the application to specific implementations.

In Lab 5, all instantiation logic was moved into `Main.java`:

`switch (choice) {
    case 1 -> repo = new InMemoryListRepository();
    case 2 -> repo = new InMemoryMapRepository();
    case 3 -> repo = new H2Repository();
    case 4 -> repo = new MySqlRepository();
}`

This is **manual Inversion of Control (IoC)**.

#### Why this design improves testing

- The application no longer decides which repository to use.
- The repository can be swapped easily for testing.
- Unit tests can inject mock or in-memory repositories.
- Integration tests can inject H2.
- Production can inject MySQL.

The application becomes more modular, more testable, and easier to maintain.

#### How this mirrors Spring Boot

Spring Boot performs this same process automatically:

- It detects repository implementations.
- It injects dependencies.
- It manages lifecycle and configuration.





    
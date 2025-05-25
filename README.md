# Habitual: JavaFX Habit Tracker

Habitual is a modern habit and task tracker desktop application built with Java, JavaFX, and MySQL. It features a clean UI, habit streak tracking, statistics dashboard, reminders, password reset with security questions, and both dark and light modes. The project is structured for easy extension and university-level code review.

## Features

- **User Registration & Login** (with security question/answer)
- **Password Reset** (via security question)
- **Add/Edit/Delete Habits and Tasks**
- **Habit Streak Tracking**
- **Statistics Dashboard** (pie/bar charts for streaks)
- **Reminders/Notifications** (for due habits/tasks)
- **Dark/Light Mode Toggle**
- **DAO Pattern** for database access
- **MVC Structure** for UI and logic separation

## Project Structure

```
Habitual/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/habitual/
│   │   │       ├── App.java
│   │   │       ├── auth/...
│   │   │       ├── dao/...
│   │   │       ├── db/...
│   │   │       ├── habits/...
│   │   │       ├── schedule/...
│   │   │       └── ui/javafx/...
│   │   └── resources/
│   │       └── com/habitual/ui/javafx/
│   │           ├── *.fxml
│   │           ├── styles.css
│   │           └── styles-light.css
│   └── test/java/com/habitual/AppTest.java
└── target/
```

- **App.java**: Main entry point
- **auth/**: Authentication, user model, password reset
- **dao/**: Data access objects (DAO pattern)
- **db/**: Database connection logic
- **habits/**: Habit logic and models
- **schedule/**: Task/schedule logic
- **ui/javafx/**: JavaFX controllers and FXML views
- **resources/ui/javafx/**: FXML files and CSS

## Prerequisites

- Java 17 or later
- Maven
- MySQL Server

## Setup Instructions

1. **Clone the repository**
   ```powershell
   git clone https://github.com/yourusername/Habitual.git
   cd Habitual
   ```

2. **Configure the Database**
   - Create a MySQL database (e.g., `habitual_db`).
   - Update your DB credentials in `src/main/java/com/habitual/db/DatabaseConnector.java`:
     ```java
     private static final String URL = "jdbc:mysql://localhost:3306/habitual_db";
     private static final String USER = "your_mysql_user";
     private static final String PASSWORD = "your_mysql_password";
     ```
   - Run the provided SQL schema (see below) to create the required tables.

3. **Build the Project**
   ```powershell
   mvn clean install
   ```

4. **Run the Application**
   ```powershell
   mvn javafx:run
   ```
   Or, run the `App.java` class from your IDE.

## Database Schema Example

```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    security_question VARCHAR(255) NOT NULL,
    security_answer_hash VARCHAR(255) NOT NULL
);

CREATE TABLE habits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    streak INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    habit_id INT,
    name VARCHAR(100) NOT NULL,
    due_date DATE NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (habit_id) REFERENCES habits(id)
);
```

## Making Enhancements

- All UI logic is in `src/main/java/com/habitual/ui/javafx/` and FXML in `src/main/resources/com/habitual/ui/javafx/`.
- Add new features by extending the DAO/service classes and updating the controllers/views.
- Use the DAO pattern for all database access.
- For new UI, create FXML files and corresponding controllers.
- For styling, update `styles.css` and `styles-light.css`.

## Credits

Developed for university review. Contributions welcome!

## License

MIT License (add your license if needed)

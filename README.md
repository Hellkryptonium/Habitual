# Habitual: JavaFX Habit Tracker

Habitual is a modern habit and task tracker desktop application built with Java, JavaFX, and MySQL. It features a clean UI, habit streak tracking, statistics dashboard, reminders, password reset with security questions, and both dark and light modes. This project is designed to be a comprehensive example of a JavaFX application, suitable for learning, extension, and personal use.

## Features

- **User Registration & Login** (with security question/answer)
- **Password Reset** (via security question)
- **Add/Edit/Delete Habits and Tasks**
- **Habit Streak Tracking**
- **Statistics Dashboard** (pie/bar charts for streaks)
- **Notes System** (for journaling and personal notes)
- **Reminders/Notifications** (for due habits/tasks)
- **Dark/Light Mode Toggle**
- **DAO Pattern** for database access
- **MVC Structure** for UI and logic separation
- **AI Integration** (Google Gemini AI for personalized habit suggestions, note analysis, and motivational messages)

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
│   │   │       ├── notes/...
│   │   │       ├── schedule/...
│   │   │       ├── ui/javafx/...
│   │   │       └── utils/
│   │   │           ├── GeminiAIService.java
│   │   │           ├── MotivationalQuotes.java
│   │   │           └── NotificationUtil.java
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
- **notes/**: Note-taking system models and data access
- **schedule/**: Task/schedule logic
- **ui/javafx/**: JavaFX controllers and FXML views
- **utils/**: Utility classes including AI services
- **resources/ui/javafx/**: FXML files and CSS

## Prerequisites

- Java 17 or later
- Maven
- MySQL Server

## Setup Instructions

To get Habitual running on your local machine, follow these steps:

1.  **Fork and Clone the Repository**
    *   Fork this repository to your own GitHub account.
    *   Clone your forked repository:
    ```powershell
    git clone https://github.com/YOUR_USERNAME/Habitual.git
    cd Habitual
    ```
    Replace `YOUR_USERNAME` with your GitHub username.

2.  **Configure the Database**
    *   Ensure you have MySQL Server installed and running.
    *   Create a MySQL database (e.g., `habitual_db`).
    *   Update your database credentials in `src/main/java/com/habitual/db/DatabaseConnector.java`. Open the file and modify the following lines:
        ```java
        private static final String URL = "jdbc:mysql://localhost:3306/habitual_db"; // Ensure 'habitual_db' matches your DB name
        private static final String USER = "your_mysql_user"; // Replace with your MySQL username
        private static final String PASSWORD = "your_mysql_password"; // Replace with your MySQL password
        ```
    *   Execute the SQL statements provided in the [Database Schema](#database-schema-example) section below to create the necessary tables in your database. You can use a MySQL client like MySQL Workbench, DBeaver, or the command line client.

3.  **Configure Gemini AI API Key (Optional but Recommended for Full Functionality)**
    *   This application uses the Google Gemini AI for features like personalized motivation, habit suggestions, and note analysis.
    *   Obtain an API key from [Google AI Studio](https://aistudio.google.com/app/apikey).
    *   Create a file named `config.properties` in the `src/main/resources/` directory.
    *   Add your API key to this file:
        ```properties
        GEMINI_API_KEY=YOUR_GEMINI_API_KEY
        ```
    *   Replace `YOUR_GEMINI_API_KEY` with your actual key.
    *   **Note:** If you choose not to configure the API key, AI-related features will not work, but the rest of the application should function. The `GeminiAIService.java` has basic error handling for missing keys or API issues.

4.  **Build the Project**
    *   Open a terminal or command prompt in the root directory of the project (`Habitual/`).
    *   Run the following Maven command to compile the project and download dependencies:
    ```powershell
    mvn clean install
    ```

5.  **Run the Application**
    *   After a successful build, run the application using Maven:
    ```powershell
    mvn javafx:run
    ```
    *   Alternatively, you can run the `com.habitual.App` class directly from your IDE (like IntelliJ IDEA, Eclipse, or VS Code with Java extensions).

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

CREATE TABLE notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Making Enhancements & Development

Habitual is designed to be extensible. If you want to add new features or modify existing ones:

-   **UI Logic:** Located in `src/main/java/com/habitual/ui/javafx/`. FXML views are in `src/main/resources/com/habitual/ui/javafx/`.
-   **Business Logic:** Service classes (e.g., `HabitService`, `NoteService`) are in their respective packages (e.g., `com.habitual.habits`, `com.habitual.notes`).
-   **Data Access:** The DAO (Data Access Object) pattern is used for all database interactions. DAOs are located in `src/main/java/com/habitual/dao/`.
-   **Styling:** Update `futuristic-styles.css` (for the dark theme) and `styles-light.css` in `src/main/resources/com/habitual/ui/javafx/`.
-   **New UI Views:** Create new FXML files and their corresponding JavaFX controller classes.
-   **Models:** Data models (e.g., `Habit.java`, `User.java`) are typically located within their relevant feature packages.

## AI Features Implementation

Habitual integrates Google Gemini AI to enhance user experience with intelligent features:

### 1. Habit Suggestions
- AI analyzes your existing habits and suggests complementary ones
- Access via the "AI Habit Suggestions" button in the Habits view
- Suggestions are personalized based on your current habits and goals

### 2. Note Analysis
- **Summarize** - Condenses long notes into concise summaries
- **Extract Actions** - Identifies actionable items from your notes
- Access these features from the Notes view

### 3. Personalized Motivation
- AI generates personalized motivational messages based on your habit completion and streaks
- Displayed on the dashboard, replacing generic quotes with context-aware encouragement

For detailed technical implementation, see `AI_Features_Documentation.md`.

## Error Handling & Validation

Habitual implements comprehensive error handling and validation:

- Client-side validation in all form inputs
- Server-side validation in service layers
- Descriptive error messages
- SQL injection prevention
- Confirmation dialogs for destructive actions
- Graceful handling of database connectivity issues

The validation utilities (`ValidationUtils.java`) provide consistent validation across the application.

## Contributing

Contributions are welcome and greatly appreciated! If you'd like to contribute to Habitual:

1.  **Fork the Project:** Create your own fork of the repository on GitHub.
2.  **Create a Feature Branch:**
    ```powershell
    git checkout -b feature/AmazingFeature
    ```
3.  **Commit your Changes:**
    ```powershell
    git commit -m 'Add some AmazingFeature'
    ```
4.  **Push to the Branch:**
    ```powershell
    git push origin feature/AmazingFeature
    ```
5.  **Open a Pull Request:** Go to your forked repository on GitHub and open a pull request to the `main` branch of the original repository.

Please make sure to update tests as appropriate and follow the existing code style.

## Credits

This project was initially developed for university review and has been structured to serve as a learning resource.
We encourage you to fork, modify, and learn from this codebase.

## License

This project is licensed under the MIT License. See the `LICENSE.md` file for details (if you add one).

**To add an MIT License:**
1.  Create a file named `LICENSE.md` in the root of your project.
2.  Go to [choosealicense.com/licenses/mit/](https://choosealicense.com/licenses/mit/)
3.  Copy the license text and paste it into your `LICENSE.md` file. Replace `[year]` with the current year and `[fullname]` with your name or your organization's name.

This makes it clear how others can use and contribute to your project.

# Habitual Project Documentation - Evaluation Criteria

## Introduction

Habitual is a comprehensive desktop application designed to help users build and maintain positive habits, manage tasks, take notes, and stay motivated. This document outlines how the project addresses key software development evaluation criteria.

---

## 1. Core Feature Implementation (5 marks)

The Habitual application implements a range of core features essential for its purpose:

*   **User Authentication (Login & Registration):**
    *   Secure user login (`LoginViewController`, `UserService`, `UserDAO`).
    *   New user registration with password hashing (`RegisterViewController`, `UserService`, `UserDAO`).
    *   Password recovery and security question mechanisms (`ForgotPasswordViewController`, `ChangeSecurityQuestionViewController`).
    *   Session management using a `UserSession` class to maintain logged-in user state across the application.
*   **Habit Management:**
    *   **CRUD Operations:**
        *   Adding new habits with name, description, frequency, and start date (`AddHabitViewController`, `HabitService`, `HabitDAO`).
        *   Viewing a list of all habits for the logged-in user (`HabitsViewController`, `MainDashboardViewController`).
        *   Editing existing habits (functionality present in `HabitsViewController`, `HabitService`, `HabitDAO`).
        *   Deleting habits (`HabitsViewController`, `HabitService`, `HabitDAO`).
    *   **Tracking:**
        *   Logging habit completions for specific dates (`HabitsViewController`, `HabitService`, `HabitCompletionDAO`).
        *   Undoing habit completions.
        *   Calculating and displaying current and longest streaks for habits.
*   **Note-Taking Functionality:**
    *   **CRUD Operations:**
        *   Creating new notes with title, content, and tags (`NotesViewController`, `NoteService`, `NoteDAO`).
        *   Viewing a list of notes.
        *   Editing existing notes.
        *   Deleting notes.
    *   Displaying last modified timestamps.
*   **Task Scheduling & Management:**
    *   Adding tasks with descriptions and due dates (`AddTaskViewController`, `ScheduleService`, `ScheduledTaskDAO`).
    *   Viewing scheduled tasks, potentially in a calendar or list format (`ScheduleViewController`).
    *   Marking tasks as complete.
*   **Statistics and Progress Visualization:**
    *   Displaying overall progress, habit streaks, and completion rates (`StatsDashboardViewController`, `HabitsViewController`).
    *   Use of progress bars and labels to visually represent data.
*   **AI-Powered Motivational Messages:**
    *   Integration with Gemini AI (`GeminiAIService`) to generate and display motivational messages on the dashboard (`MainDashboardViewController`).
*   **User Interface (JavaFX):**
    *   A multi-view application structure managed by `MainDashboardViewController` loading different FXML views into a central content area.
    *   Clear separation of UI (FXML files) and controller logic (JavaFX Controller classes).
    *   Customizable theme with Dark/Light mode toggle (`MainDashboardViewController`, `futuristic-styles.css`, `styles-light.css`).

---

## 2. Error Handling and Robustness (5 marks)

The application incorporates various mechanisms to handle errors and ensure robust operation:

*   **Database Operations:**
    *   `DatabaseUtil` class likely handles initial connection setup and may include basic error checking.
    *   DAO (Data Access Object) methods (e.g., `UserDAO`, `HabitDAO`, `NoteDAO`) use try-catch blocks to handle `SQLException`s that might occur during database interactions (e.g., connection issues, query syntax errors, constraint violations).
    *   Error messages are often logged or displayed to the user (e.g., "Login failed," "Could not save habit").
*   **FXML Loading and UI Initialization:**
    *   `MainFxApp` (or similar entry point) typically wraps scene loading in try-catch blocks to handle `IOException` if FXML files are not found or are malformed.
    *   Controllers' `initialize` methods or event handlers may include null checks for `@FXML` injected components before use, although FXML loader exceptions are more common for missing components.
    *   Specific FXML loading errors (e.g., missing controller methods, incorrect `fx:id`s) are caught by the JavaFX FXMLLoader and reported, which the application handles by failing to load the view, with stack traces printed to the console.
*   **User Input Issues:**
    *   Basic checks for empty fields in forms (e.g., login, registration, adding habits/notes).
    *   Alert dialogs are used to inform users about input errors or operation failures (e.g., `Alerts.showError`, `Alerts.showInfo`).
*   **External Service Integration (Gemini AI):**
    *   `GeminiAIService` includes error handling for API calls:
        *   Catches `IOException` for network issues or unexpected responses.
        *   Checks HTTP response codes (e.g., 404 for "Not Found" when the API key/endpoint was an issue).
        *   Uses `CompletableFuture` for asynchronous operations, allowing the UI to remain responsive. Errors in the async task are logged.
*   **Null Pointer Prevention:**
    *   Checks for `null` objects before accessing their methods or properties, especially for objects retrieved from the database or selected in UI lists (e.g., `selectedHabit` in `HabitsViewController`).
*   **File Handling (Stylesheets):**
    *   The theme toggling logic includes checks for the existence of stylesheet files and logs an error if they are not found, preventing the application from crashing.
*   **Concurrency:**
    *   Use of `Platform.runLater()` for UI updates from non-UI threads (e.g., after AI service calls) to prevent threading issues.

---

## 3. Integration of Components (5 marks)

The application demonstrates effective integration of its various software components:

*   **Layered Architecture:**
    *   **Presentation Layer (UI):** JavaFX FXML files for layout and JavaFX Controller classes (`LoginViewController`, `HabitsViewController`, etc.) for handling user interaction and UI logic.
    *   **Service Layer:** Business logic is encapsulated in service classes (`UserService`, `HabitService`, `NoteService`, `ScheduleService`, `GeminiAIService`). These services act as intermediaries between the UI controllers and the DAOs.
    *   **Data Access Layer (DAO):** DAO classes (`UserDAO`, `HabitDAO`, `NoteDAO`, `ScheduledTaskDAO`, `HabitCompletionDAO`) are responsible for all database interactions, abstracting SQL operations from the service layer.
    *   **Database:** MySQL database stores all persistent application data.
*   **Centralized UI Management:**
    *   `MainDashboardViewController` acts as a central hub, dynamically loading different views (Habits, Notes, Stats, Schedule, etc.) into a `contentArea` (BorderPane center). This promotes modularity and a single-window application feel.
*   **User Session Management:**
    *   The `UserSession` class provides a global way to access the currently logged-in user's ID and other relevant session data, facilitating data retrieval and operations specific to that user across different components.
*   **Styling Integration:**
    *   CSS files (`futuristic-styles.css`, `styles-light.css`) are integrated with FXML views to provide a consistent and customizable look and feel. The theme can be dynamically changed.
*   **External API Integration:**
    *   `GeminiAIService` is integrated to fetch motivational content, showcasing interaction with external web services. This component is called from the `MainDashboardViewController`.
*   **Data Flow:**
    *   User actions in the UI trigger methods in Controllers.
    *   Controllers call methods in Service classes.
    *   Service classes orchestrate business logic and call methods in DAO classes.
    *   DAO classes execute SQL queries against the database and return results.
    *   Data flows back up the layers to update the UI.

---

## 4. Event Handling and Processing (5 marks)

The application effectively uses JavaFX's event handling mechanisms:

*   **FXML Event Handlers:**
    *   `@FXML` annotation is used extensively in controller classes to link UI elements (defined in FXML) to event handler methods.
    *   Methods like `handleLoginButtonAction()`, `handleAddHabitAction()`, `handleSaveNoteAction()`, `handleToggleTheme()` are triggered by user interactions (e.g., button clicks).
*   **Types of Events Handled:**
    *   **ActionEvents:** Primarily from `Button` clicks (e.g., login, save, add, delete, toggle theme).
    *   **MouseEvents:** Implicitly handled by JavaFX controls, but could be explicitly used for custom interactions.
    *   **SelectionEvents:** `ListView` or `TableView` selections trigger updates in detail panes or enable/disable context-specific buttons (e.g., selecting a habit to view/edit/log completion, selecting a note to view/edit).
    *   **InputEvents:** `TextField` or `TextArea` changes could trigger validation or live updates (though direct examples are less prominent in snippets, it's a standard JavaFX capability).
*   **UI Updates in Response to Events:**
    *   Event handlers frequently update the UI by:
        *   Loading new FXML views into the `contentArea`.
        *   Populating `ListViews` or `TableViews` with data (e.g., `loadHabits()`, `loadNotes()`).
        *   Updating `Label`s, `ProgressBar`s, and other display elements.
        *   Showing `Alert` dialogs.
        *   Clearing input fields.
*   **Asynchronous Event Processing:**
    *   For long-running tasks like AI API calls, `CompletableFuture` is used, and UI updates are performed using `Platform.runLater()` to ensure they happen on the JavaFX Application Thread.
*   **Event Propagation/Bubbling:** While not explicitly customized, the application relies on JavaFX's default event dispatching mechanisms.

---

## 5. Data Validation (5 marks)

Data validation is implemented at the presentation layer to ensure data integrity before processing or storage:

*   **Client-Side Validation (in JavaFX Controllers):**
    *   **Presence Checks:** Verifying that required fields are not empty (e.g., username and password in `LoginViewController`, habit name in `AddHabitViewController`, note title/content in `NotesViewController`).
    *   **String Length/Format (Implicit/Potential):** While not explicitly detailed for all fields, prompt texts and UI design guide users. More complex format validation (e.g., email format) could be added.
    *   **Password Confirmation:** Ensuring that "password" and "confirm password" fields match during user registration (`RegisterViewController`).
*   **User Feedback on Validation Errors:**
    *   `Alert` dialogs (e.g., `Alerts.showError()`) are used to inform the user about validation failures, prompting them to correct the input.
    *   Error labels or visual cues near input fields could also be used (e.g., `errorLabel` in some views).
*   **Service Layer Validation (Assumed Best Practice):**
    *   While not always visible in UI controller snippets, service layers (`UserService`, `HabitService`) should ideally perform further validation or business rule checks before passing data to DAOs, providing a second layer of defense.
*   **Database Constraints:**
    *   The database schema itself (e.g., `NOT NULL` constraints, unique keys, foreign keys) provides the ultimate layer of data integrity. Errors from these constraints are caught as `SQLException`s by the DAOs.

---

## 6. Code Quality and Innovative Features (3 marks)

*   **Code Quality:**
    *   **Modularity:** The project is structured into distinct layers (UI/Controllers, Services, DAOs), promoting separation of concerns and maintainability.
    *   **Readability:** Generally consistent naming conventions for classes, methods, and variables. Use of FXML for UI definition separates view from logic.
    *   **Reusability:**
        *   `DatabaseUtil` for managing database connections.
        *   `UserSession` for shared user state.
        *   `Alerts` utility class for showing common dialogs.
        *   Service classes encapsulate business logic reusable by different UI parts or potentially other clients.
    *   **Comments:** In-code comments are present to explain parts of the logic, though this can always be expanded.
    *   **Object-Oriented Principles:** Encapsulation is used (e.g., private fields with public getters/setters in model classes like `Habit`, `Note`).
*   **Innovative Features:**
    *   **AI-Powered Motivational Messages:** Integration with the Gemini AI API (`GeminiAIService`) to provide users with dynamic motivational content is a modern and engaging feature.
    *   **Dynamic Theme Toggling:** The ability to switch between a custom "futuristic" dark theme and a light theme enhances user experience and personalization.
    *   **Comprehensive Feature Set:** The combination of habit tracking, detailed note-taking with potential AI assistance (summarization, action extraction - though implementation details of these AI note features are less clear), task scheduling, and progress statistics within a single application provides a holistic personal productivity tool.
    *   **Custom CSS Styling:** Significant effort in creating a unique "futuristic" look and feel through `futuristic-styles.css` rather than relying solely on default JavaFX styles.

---

## 7. Project Documentation (3 marks)

*   **This Document:** The current `Project_Documentation_Habitual.md` file serves as a detailed overview of the project's features, architecture, and how it meets various evaluation criteria.
*   **In-Code Comments:** Java code across controllers, services, DAOs, and utility classes contains comments explaining the purpose of methods, complex logic, and important variables.
*   **FXML Comments:** FXML files may contain comments to describe UI sections or specific configurations.
*   **Commit Messages (if using Version Control):** (User should mention if their Git commit messages are descriptive and track project evolution).
*   **README File (User to Add/Verify):** A well-structured README file would typically include:
    *   Project overview and purpose.
    *   Features list.
    *   Technologies used (Java, JavaFX, MySQL, Maven, Gemini AI API).
    *   Setup and installation instructions (JDK version, Maven setup, database schema setup, API key configuration).
    *   How to run the application.
    *   (Optionally) Known issues or future improvements.

---
This documentation should provide a solid basis for your presentation. Remember to elaborate on these points with specific code examples or live demonstrations where appropriate.

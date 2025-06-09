package com.habitual.ui.javafx;

import com.habitual.notes.Note;
import com.habitual.notes.NoteService;
import com.habitual.utils.GeminiAIService;
import com.habitual.db.DatabaseConnector;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NotesViewController {

    @FXML
    private ListView<Note> notesListView;
    @FXML
    private TextField titleTextField;
    @FXML
    private TextArea contentTextArea;
    @FXML
    private Button saveNoteButton;    @FXML
    private Button deleteNoteButton;
    @FXML
    private Label errorLabel;
    @FXML
    private TextArea aiResultsTextArea;
    @FXML
    private VBox aiResultsBox;
    @FXML
    private Button aiSummarizeButton;
    @FXML
    private Button aiExtractButton;
    @FXML private TextField tagsTextField;
    @FXML private Label lastModifiedLabel;

    private NoteService noteService;
    private GeminiAIService aiService;
    private ObservableList<Note> observableNotesList;
    private Note currentSelectedNote;
    private UserSession userSession;
    private enum AIAction { SUMMARIZE, EXTRACT_ACTIONS }
    private AIAction currentAIAction;    public void initialize() {
        userSession = UserSession.getInstance();
        noteService = new NoteService(DatabaseConnector.getConnection());
        aiService = new GeminiAIService();
        observableNotesList = FXCollections.observableArrayList();
        notesListView.setItems(observableNotesList);
        
        // Hide AI results box initially
        aiResultsBox.setVisible(false);

        // Disable AI buttons if Gemini API is not available
        boolean aiAvailable = aiService.isAIAvailable();
        if (aiSummarizeButton != null) {
            aiSummarizeButton.setDisable(!aiAvailable);
            if (!aiAvailable) aiSummarizeButton.setTooltip(new Tooltip("Google Gemini AI is not configured. Set GEMINI_API_KEY in .env."));
        }
        if (aiExtractButton != null) {
            aiExtractButton.setDisable(!aiAvailable);
            if (!aiAvailable) aiExtractButton.setTooltip(new Tooltip("Google Gemini AI is not configured. Set GEMINI_API_KEY in .env."));
        }

        loadNotes();

        notesListView.setCellFactory(param -> new ListCell<Note>() {
            @Override
            protected void updateItem(Note item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getTitle() == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });

        notesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentSelectedNote = newValue;
                populateNoteDetails(newValue);
                errorLabel.setText("");
                deleteNoteButton.setDisable(false);
            } else {
                currentSelectedNote = null;
                clearNoteDetails();
                deleteNoteButton.setDisable(true);
            }
        });
        deleteNoteButton.setDisable(true); // Disable delete initially
        errorLabel.setText("");
    }

    private void loadNotes() {
        if (!userSession.isLoggedIn()) {
            observableNotesList.clear();
            clearNoteDetails();
            errorLabel.setText("User not logged in.");
            return;
        }
        List<Note> notes = noteService.getNotes(userSession.getUserId());
        observableNotesList.setAll(notes);
        if (!notes.isEmpty()) {
            notesListView.getSelectionModel().selectFirst();
        } else {
            clearNoteDetails();
            currentSelectedNote = null;
        }
    }

    private void populateNoteDetails(Note note) {
        titleTextField.setText(note.getTitle());
        contentTextArea.setText(note.getContent());
    }

    private void clearNoteDetails() {
        titleTextField.clear();
        contentTextArea.clear();
        errorLabel.setText("");
    }

    @FXML
    private void handleNewNoteAction() {
        currentSelectedNote = null; // Indicate new note
        clearNoteDetails();
        titleTextField.requestFocus();
        notesListView.getSelectionModel().clearSelection();
        deleteNoteButton.setDisable(true);
        errorLabel.setText("");
    }

    @FXML
    private void handleSaveNoteAction() {
        String title = titleTextField.getText();
        String content = contentTextArea.getText();
        if (!userSession.isLoggedIn()) {
            errorLabel.setText("User not logged in.");
            return;
        }
        if (title == null || title.trim().isEmpty()) {
            errorLabel.setText("Title cannot be empty.");
            return;
        }
        if (content == null || content.trim().isEmpty()) {
            errorLabel.setText("Content cannot be empty.");
            return;
        }
        try {
            if (currentSelectedNote != null) { // Update existing note
                boolean updated = noteService.updateNote(currentSelectedNote.getId(), title, content, null);
                if (updated) {
                    currentSelectedNote.setTitle(title);
                    currentSelectedNote.setContent(content);
                    errorLabel.setText("Note updated successfully.");
                } else {
                    errorLabel.setText("Failed to update note.");
                }
            } else { // Add new note
                boolean added = noteService.addNote(userSession.getUserId(), title, content, null);
                if (added) {
                    errorLabel.setText("Note saved successfully.");
                } else {
                    errorLabel.setText("Failed to save note.");
                }
            }
            loadNotes();
            if (currentSelectedNote != null) {
                notesListView.getSelectionModel().select(currentSelectedNote);
            } else {
                Optional<Note> addedNote = observableNotesList.stream().filter(n -> n.getTitle().equals(title) && n.getContent().equals(content)).findFirst();
                addedNote.ifPresent(note -> notesListView.getSelectionModel().select(note));
                if(!addedNote.isPresent()) handleNewNoteAction();
            }
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Error saving note: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteNoteAction() {
        if (currentSelectedNote == null) {
            errorLabel.setText("No note selected to delete.");
            return;
        }
        if (!userSession.isLoggedIn()) {
            errorLabel.setText("User not logged in.");
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Note: " + currentSelectedNote.getTitle());
        alert.setContentText("Are you sure you want to delete this note?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = noteService.deleteNote(currentSelectedNote.getId());
                if (deleted) {
                    loadNotes();
                    clearNoteDetails();
                    errorLabel.setText("Note deleted successfully.");
                    currentSelectedNote = null;
                    deleteNoteButton.setDisable(true);
                } else {
                    errorLabel.setText("Failed to delete note.");
                }
            } catch (Exception e) {
                errorLabel.setText("Error deleting note: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleSummarizeAction() {
        if (currentSelectedNote == null && contentTextArea.getText().trim().isEmpty()) {
            errorLabel.setText("Please write or select a note to summarize.");
            return;
        }
        
        String content = contentTextArea.getText();
        errorLabel.setText("Generating summary...");
        
        // Use async call to avoid freezing the UI
        CompletableFuture.supplyAsync(() -> aiService.summarizeNote(content))
            .thenAccept(summary -> Platform.runLater(() -> {
                aiResultsTextArea.setText(summary);
                aiResultsBox.setVisible(true);
                errorLabel.setText("");
                currentAIAction = AIAction.SUMMARIZE;
            }))
            .exceptionally(e -> {
                Platform.runLater(() -> errorLabel.setText("Error generating summary: " + e.getMessage()));
                return null;
            });
    }
    
    @FXML
    private void handleExtractActionsAction() {
        if (currentSelectedNote == null && contentTextArea.getText().trim().isEmpty()) {
            errorLabel.setText("Please write or select a note to extract actions from.");
            return;
        }
        
        String content = contentTextArea.getText();
        errorLabel.setText("Extracting action items...");
        
        // Use async call to avoid freezing the UI
        CompletableFuture.supplyAsync(() -> aiService.extractActionItems(content))
            .thenAccept(actions -> Platform.runLater(() -> {
                aiResultsTextArea.setText(actions);
                aiResultsBox.setVisible(true);
                errorLabel.setText("");
                currentAIAction = AIAction.EXTRACT_ACTIONS;
            }))
            .exceptionally(e -> {
                Platform.runLater(() -> errorLabel.setText("Error extracting actions: " + e.getMessage()));
                return null;
            });
    }
    
    @FXML
    private void handleApplyAIResultAction() {
        String aiResult = aiResultsTextArea.getText();
        if (aiResult == null || aiResult.trim().isEmpty()) {
            return;
        }
        
        if (currentAIAction == AIAction.SUMMARIZE) {
            // For summary, we replace the content
            contentTextArea.setText(aiResult);
        } else if (currentAIAction == AIAction.EXTRACT_ACTIONS) {
            // For action items, we append to the content
            String currentContent = contentTextArea.getText();
            contentTextArea.setText(currentContent + "\n\n=== Action Items ===\n" + aiResult);
        }
        
        // Hide the AI results box after applying
        aiResultsBox.setVisible(false);
    }
    
    @FXML
    private void handleDismissAIResultAction() {
        // Just hide the AI results box
        aiResultsBox.setVisible(false);
    }
    
    @FXML private void handleBoldTextAction() {}
    @FXML private void handleItalicTextAction() {}
}

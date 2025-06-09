# AI Feature Implementation in Habitual

This document outlines the implementation of Google Gemini AI integration in the Habitual application, providing personalized habit suggestions, note summarization, action item extraction, and motivational messages.

## Overview

The Habitual application leverages Google Gemini AI to enhance user experience through several AI-powered features:

1. **Personalized Habit Suggestions** - AI analyzes existing habits and suggests new complementary habits
2. **Note Summarization** - AI condenses long notes into concise summaries
3. **Action Item Extraction** - AI identifies actionable items from note content
4. **Personalized Motivational Messages** - AI generates context-aware motivational content based on user's habit completion and streaks

## Technical Implementation

### 1. GeminiAIService

The core AI functionality is implemented in `GeminiAIService.java`, which handles communication with the Google Gemini API:

```java
package com.habitual.utils;

/**
 * Service class for interacting with the Google Gemini AI API.
 * This class provides methods to generate AI-powered content for habit suggestions,
 * note summarization, and motivational messages.
 */
public class GeminiAIService {
    // Core AI methods:
    public String generateContent(String prompt)
    public CompletableFuture<String> generateContentAsync(String prompt)
    public String generateHabitSuggestions(String existingHabits, String userGoals)
    public String summarizeNote(String noteContent) 
    public String generateMotivationalMessage(int completedHabits, String streakInfo)
    public String extractActionItems(String noteContent)
}
```

The service uses asynchronous processing to prevent UI freezing during API calls.

### 2. Note Analysis Features

AI-powered note features are integrated in `NotesViewController.java`:

- **Summarize** - Condenses selected note content into a brief summary
- **Extract Actions** - Identifies and extracts action items from note content

These features utilize async processing and provide options to either apply the AI results to the note or dismiss them.

### 3. Habit Suggestions

Implemented in `HabitsViewController.java`, this feature:

1. Collects the user's existing habits
2. Sends them to the Gemini AI with appropriate context
3. Displays AI-generated habit suggestions in a dedicated UI panel

### 4. Personalized Motivation

Implemented in `MainDashboardViewController.java`, this feature:

1. Analyzes the user's habit completion patterns and streaks
2. Generates a personalized motivational message
3. Displays it on the dashboard, replacing generic quotes

## API Configuration

The AI service requires a Google Gemini API key to be configured in the `GeminiAIService.java` file:

```java
private static final String API_KEY = "YOUR_GEMINI_API_KEY";
```

For production deployments, this key should be stored securely (e.g., environment variables) rather than hard-coded.

## Error Handling

The AI integration includes robust error handling:

- **API Failures** - Falls back to pre-defined content if AI calls fail
- **Asynchronous Processing** - Prevents UI freezing during API calls
- **Retry Logic** - Implements basic retry for transient network issues
- **User Feedback** - Provides clear status updates during AI processing

## Future Improvements

Potential enhancements for the AI integration:

1. **User Preference Learning** - Adapt suggestions based on which AI recommendations the user implements
2. **Scheduling Optimization** - AI analysis of task completion patterns to suggest optimal task scheduling
3. **Progress Insights** - Deeper AI analysis of habit patterns and correlations
4. **Voice Integration** - Add speech-to-text for note creation and command processing

## References

- [Google Gemini AI Documentation](https://ai.google.dev/docs)
- [Java HTTP Client Documentation](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html)
- [JavaFX Concurrency](https://openjfx.io/javadoc/17/javafx.graphics/javafx/concurrent/Task.html)

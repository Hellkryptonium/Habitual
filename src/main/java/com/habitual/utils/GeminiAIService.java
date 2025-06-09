package com.habitual.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service class for interacting with the Google Gemini AI API.
 * This class provides methods to generate AI-powered content for habit suggestions,
 * note summarization, and motivational messages.
 * 
 * @author Habitual Team
 */
public class GeminiAIService {
    // Replace with your actual API key when testing
    private static final String API_KEY = "YOUR_GEMINI_API_KEY";
    // Use the correct Gemini API endpoint (v1, not v1beta)
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    
    private final Gson gson = new Gson();

    private static String loadApiKeyFromEnvFile() {
        try {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(".env");
            props.load(fis);
            fis.close();
            String key = props.getProperty("GEMINI_API_KEY");
            if (key != null && !key.trim().isEmpty() && !key.equals("YOUR_GEMINI_API_KEY_HERE")) {
                return key.trim();
            }
        } catch (IOException ignored) {}
        return null;
    }

    private static String getApiKey() {
        // 1. Check .env file
        String envFileKey = loadApiKeyFromEnvFile();
        if (envFileKey != null) return envFileKey;
        // 2. Check environment variable
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty() && !envKey.equals("YOUR_GEMINI_API_KEY")) {
            return envKey;
        }
        // 3. Check system property
        String sysProp = System.getProperty("GEMINI_API_KEY");
        if (sysProp != null && !sysProp.trim().isEmpty() && !sysProp.equals("YOUR_GEMINI_API_KEY")) {
            return sysProp;
        }
        // 4. Fallback to hardcoded (for dev only)
        if (API_KEY != null && !API_KEY.equals("YOUR_GEMINI_API_KEY")) {
            return API_KEY;
        }
        return null;
    }

    private static boolean isApiKeyConfigured() {
        return getApiKey() != null;
    }

    public boolean isAIAvailable() {
        return isApiKeyConfigured();
    }

    /**
     * Sends a prompt to the Gemini AI API and returns the response.
     * 
     * @param prompt The text prompt to send to Gemini
     * @return The AI-generated response text
     * @throws IOException If an error occurs during API communication
     */
    public String generateContent(String prompt) throws IOException {
        String apiKey = getApiKey();
        if (apiKey == null) {
            throw new IOException("Google Gemini API key is not configured. Set GEMINI_API_KEY as an environment variable or in the code.");
        }
        // Create the JSON payload
        JsonObject contentPart = new JsonObject();
        contentPart.addProperty("text", prompt);
        
        JsonArray parts = new JsonArray();
        parts.add(contentPart);
        
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        
        JsonArray contents = new JsonArray();
        contents.add(content);
        
        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);
        
        // Create the HTTP request
        String url = ENDPOINT + "?key=" + apiKey;
        RequestBody body = RequestBody.create(
            requestBody.toString(), 
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        
        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            
            // Parse the response
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            // Extract the text from the response
            if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
                if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                    JsonArray responseParts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                    if (responseParts.size() > 0) {
                        return responseParts.get(0).getAsJsonObject().get("text").getAsString();
                    }
                }
            }
            
            return "Sorry, I couldn't generate a response.";
        }
    }
    
    /**
     * Asynchronously sends a prompt to the Gemini AI API.
     * 
     * @param prompt The text prompt to send to Gemini
     * @return A CompletableFuture containing the AI-generated response
     */
    public CompletableFuture<String> generateContentAsync(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return generateContent(prompt);
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        });
    }

    /**
     * Generates habit suggestions based on existing habits and user preferences.
     * 
     * @param existingHabits Description of existing habits
     * @param userGoals User's stated goals
     * @return Habit suggestions
     */
    public String generateHabitSuggestions(String existingHabits, String userGoals) {
        try {
            String prompt = "Based on these existing habits: '" + existingHabits + 
                            "' and user goals: '" + userGoals + 
                            "', suggest 3 new habits that would be beneficial to add. " + 
                            "Format each suggestion with a name and brief description.";
                            
            return generateContent(prompt);
        } catch (IOException e) {
            e.printStackTrace();
            return "Unable to generate habit suggestions at this time.";
        }
    }

    /**
     * Summarizes a note using AI.
     * 
     * @param noteContent The content of the note to summarize
     * @return A summarized version of the note
     */
    public String summarizeNote(String noteContent) {
        try {
            String prompt = "Summarize the following note in 2-3 sentences: " + noteContent;
            return generateContent(prompt);
        } catch (IOException e) {
            e.printStackTrace();
            return "Unable to summarize note at this time.";
        }
    }

    /**
     * Generates a personalized motivational message.
     * 
     * @param completedHabits Number of completed habits
     * @param streakInfo Information about current streak
     * @return A personalized motivational message
     */
    public String generateMotivationalMessage(int completedHabits, String streakInfo) {
        try {
            String prompt = "Generate a personalized, uplifting motivational message for someone who " + 
                            "has completed " + completedHabits + " habits today. " + 
                            "Their streak information is: " + streakInfo + ". " + 
                            "Keep it brief (under 150 characters) and inspiring.";
                            
            return generateContent(prompt);
        } catch (IOException e) {
            e.printStackTrace();
            return "You're doing great! Keep up the good work.";
        }
    }

    /**
     * Extracts potential action items from note content.
     * 
     * @param noteContent The content to analyze for action items
     * @return List of identified action items
     */
    public String extractActionItems(String noteContent) {
        try {
            String prompt = "Extract action items from the following note. Return only the action items " + 
                            "as a numbered list: " + noteContent;
            return generateContent(prompt);
        } catch (IOException e) {
            e.printStackTrace();
            return "Unable to extract action items at this time.";
        }
    }
}

package com.habitual.utils;

public class GeminiApiKeyTest {
    public static void main(String[] args) {
        GeminiAIService ai = new GeminiAIService();
        if (!ai.isAIAvailable()) {
            System.out.println("Gemini API key is NOT available (not loaded from .env or env vars).");
            System.exit(1);
        }
        try {
            String result = ai.summarizeNote("This is a test note for Gemini AI. Please summarize.");
            System.out.println("Gemini API test result: " + result);
        } catch (Exception e) {
            System.out.println("Gemini API test error: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}

package com.habitual.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MotivationalQuotes {
    private static final List<String> QUOTES = Arrays.asList(
        "Success is the sum of small efforts, repeated day in and day out.",
        "Don’t watch the clock; do what it does. Keep going.",
        "The secret of getting ahead is getting started.",
        "It always seems impossible until it’s done.",
        "You don’t have to be great to start, but you have to start to be great.",
        "Small deeds done are better than great deeds planned.",
        "Discipline is the bridge between goals and accomplishment.",
        "Motivation gets you going, but discipline keeps you growing.",
        "The future depends on what you do today.",
        "Well done is better than well said."
    );
    private static final Random RANDOM = new Random();

    public static String getRandomQuote() {
        return QUOTES.get(RANDOM.nextInt(QUOTES.size()));
    }
}

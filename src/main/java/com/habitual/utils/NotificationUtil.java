package com.habitual.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.TrayIcon.MessageType;

public class NotificationUtil {
    public static void showNotification(String title, String message) {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported!");
            return;
        }
        try {
            SystemTray tray = SystemTray.getSystemTray();
            // Create a blank 1x1 image for the tray icon
            Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            TrayIcon trayIcon = new TrayIcon(image, "Habitual");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Habitual Notification");
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, MessageType.INFO);
            // Remove the icon after showing notification
            Thread.sleep(2000); // Wait a bit so the notification shows
            tray.remove(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

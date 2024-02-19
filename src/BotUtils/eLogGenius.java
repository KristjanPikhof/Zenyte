package BotUtils;

import net.runelite.api.ChatMessageType;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.*;
import simple.robot.api.ClientContext;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class eLogGenius {

    private final ClientContext ctx;
    private long previousTime;

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public eLogGenius(ClientContext ctx) {
        this.ctx = ctx;
        previousTime = System.currentTimeMillis();
    }


    // Print functions
    public void print(String message) {
        System.out.println(getCurrentTimeFormatted() + " | " + message);
    }
    public void log(String formattedMessage) { // Will log String message to a file
        logToFile(formattedMessage);
    }

    public void updateStatus(String message) {
        ctx.log(getCurrentTimeFormatted() + " | " + message);
    }

    public void printElapsedTime(String functionName) { // Add to the start and finish of your function / add in flow at the end of code each block to track elapsed time
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - previousTime;
        previousTime = currentTime;
        System.out.println("Elapsed time (ms): " + elapsedTime + " at " + functionName);
    }


    // isValid check functions - these will return boolean and System.out.println as colored message.
    public boolean isValid(SimpleNpc npc) {
        boolean isValid = npc != null && npc.validateInteractable();
        String message = (isValid ? npc.getName() : "npc") + " is " + (isValid ? "valid" : "not valid");
        printColoredMessage(message, isValid);
        return isValid;
    }

    public boolean isValid(SimpleObject object) {
        boolean isValid = object != null && object.validateInteractable();
        String message = (isValid ? object.getName() : "object") + " is " + (isValid ? "valid" : "not valid");
        printColoredMessage(message, isValid);
        return isValid;
    }

    public boolean isValid(SimpleGroundItem groundItem) {
        boolean isValid = groundItem != null && groundItem.validateInteractable();
        String message = (isValid ? groundItem.getName() : "ground object") + " is " + (isValid ? "valid" : "not valid");
        printColoredMessage(message, isValid);
        return isValid;
    }

    public boolean isValid(SimpleItem item) {
        boolean isValid = item != null && item.getId() != -1;
        String message = (isValid ? item.getName() : "item") + " is " + (isValid ? "valid" : "not valid");
        printColoredMessage(message, isValid);
        return isValid;
    }

    public boolean isValid(SimpleWidget widget) {
        boolean isValid = widget != null && !widget.isHidden();
        String message = (isValid ? widget.getName() : "widget") + " is " + (isValid ? "valid" : "not valid");
        printColoredMessage(message, isValid);
        return isValid;
    }


    // ChatMessages functions
    public void printAllChats(ChatMessage m, boolean logMessage) { // Will print all chat types and messages / boolean logMessage will log message to a file.
        ChatMessageType messageType = m.getType();
        String sender = m.getChatEvent().getName();
        String message = m.getChatEvent().getMessage();
        String formattedMessage = formatChatMessage(messageType, sender, message);
        if (logMessage) {
            log(formattedMessage);
        }
        System.out.println(formattedMessage);
    }

    public boolean printChatType(ChatMessage m, ChatMessageType targetType, boolean logMessage) { // Will print selected chat type messages / boolean logMessage will log message to a file.
        ChatMessageType messageType = m.getType();
        if (messageType == targetType) {
            printAllChats(m, logMessage);
            return true;
        }
        return false;
    }

    public boolean printChatContaining(ChatMessage m, String searchString, boolean logMessage) { // Will return boolean and print messages containing String / boolean logMessage will log message to a file.
        String message = m.getMessage().toLowerCase();
        searchString = searchString.toLowerCase();
        if (message.contains(searchString)) {
            printAllChats(m, logMessage);
            return true;
        }
        return false;
    }


    // Utility - Do not change
    private void printColoredMessage(String message, boolean isValid) { // to change color for isValid() messages
        if (isValid) {
            System.out.println("\u001B[32m" + getCurrentTimeFormatted() + " | " + message + "\u001B[0m"); // Red text
        } else {
            System.out.println("\u001B[31m" + getCurrentTimeFormatted() + " | " + message + "\u001B[0m"); // Green text
        }
    }

    private String formatChatMessage(ChatMessageType messageType, String sender, String message) {
        StringBuilder formattedMessage = new StringBuilder();
        formattedMessage.append(getCurrentTimeFormatted()).append(" | ");
        formattedMessage.append("Message via ").append(messageType).append(" ");

        if (sender != null && !sender.isEmpty()) {
            sender = sender.replaceAll("\\[|\\]", "");
            sender = sender.replaceAll("<[^>]*>", "");
            formattedMessage.append("by ").append(sender).append(" ");
        }

        message = message.replaceAll("<[^>]*>", "");
        formattedMessage.append("says: ").append(message);

        return formattedMessage.toString();
    }

    private void logToFile(String message) {
        String fileName = "eLogGenius_Logs.txt";
        try {
            FileWriter fileWriter = new FileWriter(fileName, true); // 'true' for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(message);
            printWriter.close();
        } catch (IOException e) {
            print("There has been problem with logging to text file: " + e);
        }
    }

}
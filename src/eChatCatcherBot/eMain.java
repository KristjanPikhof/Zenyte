package eChatCatcherBot;

import Utility.Trivia.eTriviaInfo;
import net.runelite.api.ChatMessageType;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static eApiAccess.eAutoResponser.randomSleeping;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.OTHER,
        description = "<html>"
                + "<p>The purpose of bot is to catch chat messages!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Start anywhere.</li>"
                + "<li>Do anything.</li>"
                + "<li>Bot will catch only game messages & broadcasts.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eChatCatcherBot",
        servers = {"Zenyte"},
        version = "0.1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static String playerGameName;
    private static final Logger logger = Logger.getLogger(eMain.class.getName());
    private FileWriter triviaQuestionsFileWriter;
    private FileWriter triviaAnswersFileWriter;
    private static final String TRIVIA_QUESTIONS_FILE_NAME = "trivia_questions.txt";
    private static final String TRIVIA_ANSWERS_FILE_NAME = "trivia_answers.txt";
    private static String triviaAnswer;


    // Tasks
    private final List<Task> tasks = new ArrayList<>();

    @Override
    public boolean prioritizeTasks() {
        return true;
    }

    @Override
    public List<Task> tasks() {
        return tasks;
    }

    @Override
    public void onExecute() {

        tasks.addAll(Arrays.asList());// Adds tasks to our {task} list for execution

        triviaAnswer = null;

        // Other vars
        System.out.println("Started eChatCatcherBot!");
        this.ctx.updateStatus("--------------- " + getCurrentTimeFormatted() + " ---------------");
        this.ctx.updateStatus("------------------------------------");
        this.ctx.updateStatus("              eChatCatcherBot       ");
        this.ctx.updateStatus("------------------------------------");
    }

    @Override
    public void onProcess() {
        super.onProcess();

        try {
            triviaQuestionsFileWriter = new FileWriter(TRIVIA_QUESTIONS_FILE_NAME, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            triviaAnswersFileWriter = new FileWriter(TRIVIA_ANSWERS_FILE_NAME, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }



    // Utility
    public static String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getPlayerName() {
        if (playerGameName == null) {
            playerGameName = ctx.players.getLocal().getName();
        }
        return playerGameName;
    }

    private void updateStatus(String newStatus, String messageLocation) {
        ctx.updateStatus(getCurrentTimeFormatted() + " " + messageLocation + " was written down.");
        System.out.println(getCurrentTimeFormatted() + " " + messageLocation + " was written down. The message: " + newStatus);
    }

    private void printStatus(String newStatus) {
        ctx.updateStatus(newStatus);
        logger.info(newStatus);
    }

    // Trivia
    private void sendAnswer(String answer) {
        if (answer == null) {
            return;
        }

        if (ctx.dialogue.dialogueOpen()) {
            ctx.dialogue.clickContinue();
        }

        StringBuilder writeAnswer = new StringBuilder("::ans ");
        writeAnswer.append(answer);

        Thread thread = new Thread(() -> {
            try {
                int sleepTime = randomSleeping(5000, 10000);
                printStatus(getCurrentTimeFormatted() + " [Trivia] Sleeping for " + sleepTime + "ms");
                Thread.sleep(sleepTime); // Randomized delay
                ctx.keyboard.sendKeys(writeAnswer.toString());
                triviaAnswer = null;
                printStatus(getCurrentTimeFormatted() + " [Trivia] Question answered");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void handleTriviaQuestion(String gameMessageTrimmed) {
        for (eTriviaInfo.TriviaQuestion triviaQuestion : eTriviaInfo.TriviaQuestion.values()) {
            if (gameMessageTrimmed.contains(triviaQuestion.getQuestion())) {
                triviaAnswer = triviaQuestion.getAnswer();
                sendAnswer(triviaAnswer);
                break;
            }
        }
    }

    @Override
    public void onTerminate() {

        try {
            triviaQuestionsFileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            triviaAnswersFileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Other vars
        this.ctx.updateStatus("-------------- " + getCurrentTimeFormatted() + " --------------");
        this.ctx.updateStatus("----------------------");
        this.ctx.updateStatus("Thank You & Good Luck!");
        this.ctx.updateStatus("----------------------");
    }

    @Override
    public void onChatMessage(ChatMessage m) {
        ChatMessageType getType = m.getType();
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        playerGameName = getPlayerName();

        if (m.getMessage() == null) {
            return;
        }

        String eventToStringTrimmed = getEvent.toString().replaceAll("<[^>]+>", "").trim();
        logger.info(eventToStringTrimmed); // to debug (returns chat type, text, sender)

        if (getType == ChatMessageType.BROADCAST) {
            String gameMessage = getEvent.getMessage();
            String gameMessageTrimmed = gameMessage.replaceAll("<[^>]+>", "").trim();
            if (gameMessageTrimmed.contains("Trivia")) {
                try {
                    triviaQuestionsFileWriter = new FileWriter(TRIVIA_QUESTIONS_FILE_NAME, true);
                    triviaQuestionsFileWriter.write(gameMessageTrimmed + System.lineSeparator());
                    triviaQuestionsFileWriter.flush();
                    updateStatus(gameMessageTrimmed, "BROADCAST");
                } catch (IOException e) {
                    throw new RuntimeException("Error writing trivia questions to file", e);
                } finally {
                    try {
                        triviaQuestionsFileWriter.close();
                    } catch (IOException e) {
                        // Handle the exception if closing the file writer fails
                        e.printStackTrace();
                    }
                }

                handleTriviaQuestion(gameMessageTrimmed);
            }
        }

        if (getType == ChatMessageType.GAMEMESSAGE || getType == ChatMessageType.SPAM) {
            String gameMessage = getEvent.getMessage();
            String gameMessageTrimmed = gameMessage.replaceAll("<[^>]+>", "").trim();
            if (gameMessageTrimmed.contains("Trivia")) {
                try {
                    triviaAnswersFileWriter = new FileWriter(TRIVIA_ANSWERS_FILE_NAME, true);
                    triviaAnswersFileWriter.write(gameMessageTrimmed + System.lineSeparator());
                    triviaAnswersFileWriter.flush();
                    updateStatus(gameMessageTrimmed, "GAMEMESSAGE");
                } catch (IOException e) {
                    throw new RuntimeException("Error writing trivia answers to file", e);
                } finally {
                    try {
                        triviaAnswersFileWriter.close();
                    } catch (IOException e) {
                        // Handle the exception if closing the file writer fails
                        e.printStackTrace();
                    }
                }
            }
        }
    }



    @Override
    public int loopDuration() {
        return 150;
    }

    @Override
    public void paint(Graphics g) {

    }
}

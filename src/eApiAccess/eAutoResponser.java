package eApiAccess;

import eAutoFighterZenyte.AutoFighterUI;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import simple.hooks.scripts.task.Task;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimplePlayer;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class eAutoResponser extends Task {

    // Constants
    private static final Logger logger = Logger.getLogger(eAutoResponser.class.getName());
    public static final WorldArea EDGE_HOME_AREA = new WorldArea(new WorldPoint(3110, 3474, 0), new WorldPoint(3074, 3516, 0));

    // Variables
    public static boolean botStarted;
    public static boolean chillingActivity;
    private static ClientContext ctx;
    public static boolean gptActiveAtHome;
    public static boolean gptIsActive;
    public static boolean gptStarted;
    private boolean isProcessingPrompt = false;
    public static String messageSaved;
    public static String messageToGPT;
    public static boolean moneyMakingActivity;
    public static String otherPlayerName = null;
    public static String playerGameName = null;
    public static boolean properGrammarActive;
    public static boolean pvmActivity;
    public static String scriptPurpose = null;
    public static boolean scriptPurposeActivity;
    public static String scriptPurposeCustom  = null;
    public static boolean scriptPurposeCustomActivity;
    public static boolean skillingActivity;
    public static boolean slayerActivity;
    public static int tokensUsed;
    public static boolean useGPT3;
    public static boolean useGPT4;


    public eAutoResponser(ClientContext ctx) {
        super(ctx);
        eAutoResponser.ctx = ctx;
    }

    @Override
    public boolean condition() { // Condition to run this task
        return !isProcessingPrompt && messageSaved != null && gptStarted && gptIsActive;
    }

    @Override
    public void run() {
        if (!botStarted || !gptStarted) return;
        Thread gptThread = new Thread(this::gptRadar);
        gptThread.start();
    }

    // ChatGPT
    public void gptRadar() {
        if (!isProcessingPrompt && messageSaved != null) {
            isProcessingPrompt = true; // Set to indicate prompt processing
            try {
                String response = chatGPT(messageSaved);
                if (response != null) {
                    writeTask(response);
                }
            } catch (JSONException e) {
                logger.info("A JSON exception occurred while processing chatGPT: " + e);
            } catch (Exception e) {
                logger.info("An exception occurred while processing chatGPT: " + e);
            } finally {
                isProcessingPrompt = false; // Reset after prompt processing
            }
        }
    }

    public String chatGPT(String text) {
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = getApiKey();

        if (apiKey.isEmpty()) {
            ctx.log("GPT API Key is missing.");
            return null;
        }

        try {
            URL apiUrl = new URL(url);
            HttpURLConnection con = (HttpURLConnection) apiUrl.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);

            JSONObject data = new JSONObject();
            if (useGPT3) {
                data.put("model", "gpt-3.5-turbo");
            } else if (useGPT4) {
                data.put("model", "gpt-4");
            }

            JSONArray messagesArray = new JSONArray();
            try {
                messagesArray.put(gptSystemSettings());
                messagesArray.put(gptUserMessage(text));
            } catch (JSONException e) {
                logger.log(Level.SEVERE, "JSONException occurred", e);
            }

            data.put("messages", messagesArray);
            data.put("max_tokens", 2000);
            data.put("temperature", 0.2);

            con.setDoOutput(true);
            con.getOutputStream().write(data.toString().getBytes());

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                // Extracting the number of tokens used in the prompt
                int tokenOutput = jsonResponse.getJSONObject("usage").getInt("prompt_tokens");
                // Logging the token count
                tokensUsed += tokenOutput;
                logger.info("Number of tokens used in the prompt: " + tokenOutput + " (Total usage: " + tokensUsed + ")");

                JSONArray choicesArray = jsonResponse.getJSONArray("choices");
                if (!choicesArray.isEmpty()) {
                    JSONObject firstChoice = choicesArray.getJSONObject(0);
                    return firstChoice.getJSONObject("message").getString("content");
                }
            } else {
                String errorResponse;
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
                    StringBuilder errorResponseBuilder = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponseBuilder.append(errorLine);
                    }
                    errorResponse = errorResponseBuilder.toString();
                }

                JSONObject errorJson;
                try {
                    errorJson = new JSONObject(errorResponse);
                    String errorMessage = errorJson.getJSONObject("error").getString("message");
                    logger.warning("API request failed with response code: " + responseCode + " - Error: " + errorMessage);
                } catch (JSONException e) {
                    logger.warning("API request failed with response code: " + responseCode + " - Error: " + errorResponse);
                }
            }
        } catch (IOException | JSONException e) {
            logger.log(Level.SEVERE, "Exception occurred", e);
        }

        return null; // Return null if no valid response is obtained
    }


    private JSONObject gptSystemSettings() throws JSONException {
        String termsForChatStart = "You are currently implemented in an Old School RuneScape (OSRS) bot script on the " + gameType() + " private server. Your in-game name is " + getPlayerName(ctx) + ". Your objective is to be a normal player and engage in natural conversations with other players so they do not get suspicious while the bot is doing tasks. When responding to other players, provide concise and direct answers without mentioning names or using unnecessary dialogue tags like \"Response:\" or \"\". Do not mention botting, bot, Simplebot, OpenAI, AI or language models. Your output must be only a response to what the player said. Keep your responses within 70 characters. Do not be chatty or too helpful. Do not ask questions. ";
        String termsForActivityStart = "If asked what you're doing, ";
        String termsForActivitySkilling = "mention your focus on skilling. ";
        String termsForActivitySlayer = "you're doing slayer. ";
        String termsForActivityPVM = "mention your engagement in PvM activities. ";
        String termsForActivityMoney = "you're trying to make some money. ";
        String termsForActivityChilling = "say you're chilling. ";
        String termsForChatFinal = "The other player name is " + otherPlayerName;

        StringBuilder messageBuilder = new StringBuilder(termsForChatStart);

        if (!properGrammarActive) {
            String termsForChatGrammar = "Do not use proper grammar, instead use Runescape related slang and abbreviation. ";
            messageBuilder.append(termsForChatGrammar);
        }

        messageBuilder.append(termsForActivityStart);

        if (chillingActivity) {
            messageBuilder.append(termsForActivityChilling);
        } else if (skillingActivity) {
            messageBuilder.append(termsForActivitySkilling);
        } else if (slayerActivity) {
            messageBuilder.append(termsForActivitySlayer);
        } else if (pvmActivity) {
            messageBuilder.append(termsForActivityPVM);
        } else if (moneyMakingActivity) {
            messageBuilder.append(termsForActivityMoney);
        } else if (scriptPurposeActivity) {
            messageBuilder.append(scriptPurpose);
        } else if (scriptPurposeCustomActivity) {
            messageBuilder.append(scriptPurposeCustom);
        }

        messageBuilder.append(termsForChatFinal);

        JSONObject systemObject = new JSONObject();
        systemObject.put("role", "system");
        systemObject.put("content", messageBuilder.toString());
        logger.info(messageBuilder.toString());

        return systemObject;
    }

    private static JSONObject gptUserMessage(String text) throws JSONException {
        JSONObject userObject = new JSONObject();
        userObject.put("role", "user");
        userObject.put("content", text);

        return userObject;
    }

    private String gameType() {
        Game.ClientType clientType = ctx.game.clientType();

        switch (clientType) {
            case ALORA:
                return "Alora";
            case ATLAS:
                return "Atlas";
            case BATTLESCAPE:
                return "Battlescape";
            case KODAI:
                return "Kodai";
            case NOVEA:
                return "Novea";
            case OSRSPS:
                return "Osrsp";
            case VITALITY:
                return "Vitality";
            case ZENYTE:
                return "Zenyte";
            default:
                return "Normal";
        }
    }

    public static String trimGameMessage(String gameMessage) {
        return gameMessage.replaceAll("<[^>]+>", "").trim();
    }

    public static void handleGptMessages(ChatMessageType type, String senderName, String formattedMessage) {
        if (type == ChatMessageType.PUBLICCHAT) {
            // Remove any text within angle brackets and trim
            senderName = trimGameMessage(senderName);

            if (senderName.equals(getPlayerName(ctx))) {
                if (formattedMessage.toLowerCase().contains("---")) {
                    messageSaved = null;
                } else {
                    logger.info("You wrote: " + formattedMessage);
                    return;
                }
            }

            if (!senderName.equals(getPlayerName(ctx))) {
                if (ctx.pathing.inArea(EDGE_HOME_AREA) && !gptActiveAtHome) { ctx.log("GPT not active at home"); return; }
                otherPlayerName = senderName;
                messageSaved = formattedMessage;
                logger.info("Player " + otherPlayerName + " wrote: " + messageSaved);
            }
        }
    }


    public static String getPlayerName(ClientContext ctx) {
        if (ctx == null) {
            logger.severe("ctx = null");
            return null; // Or some default value or throw an exception
        }

        if (ctx.players == null) {
            logger.severe("ctx.player = null");
            return null; // Or some default value or throw an exception
        }

        SimplePlayer localPlayer = ctx.players.getLocal();

        if (playerGameName == null) {
            playerGameName = localPlayer.getName();
        }

        return playerGameName;
    }

    private void writeTask (String response) {
        logger.info("GPT response: " + response);
        int randomSleep = randomSleeping(3600, 12000);
        logger.info("Sleeping for " + randomSleep + "ms");
        ctx.sleep(randomSleep);
        ctx.keyboard.sendKeys(response);
        messageSaved = null;
        messageToGPT = null;
    }

    public static int randomSleeping(int minimum, int maximum) {
        return ThreadLocalRandom.current().nextInt(minimum, maximum + 1);
    }

    public static void gptDeactivation() {
        botStarted = false;
        gptStarted = false;
        gptIsActive = false;
        messageSaved = null;
    }

    public static String getApiKey() {
        char[] apiKeyChars =
                (AutoFighterUI.apiKeyPasswordField != null && AutoFighterUI.apiKeyPasswordField.getPassword().length > 0)
                        ? AutoFighterUI.apiKeyPasswordField.getPassword()
                        : eAutoResponderGui.apiKeyPasswordField.getPassword();
        String apiKey = new String(apiKeyChars);
        Arrays.fill(apiKeyChars, ' ');
        return apiKey;
    }

    @Override
    public String status() {
        return "GPT processing dialogue";
    }
}

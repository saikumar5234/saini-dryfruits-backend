package com.example.demo.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class PushNotificationHelper {
    
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    
    /**
     * Send push notification to a single device
     * @param pushToken Expo push token
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data (can be null)
     * @return Response from Expo API
     * @throws IOException If network error occurs
     */
    public static String sendPushNotification(String pushToken, String title, String body, JSONObject data) throws IOException {
        JSONObject message = new JSONObject();
        message.put("to", pushToken);
        message.put("sound", "default");
        message.put("title", title);
        message.put("body", body);
        message.put("priority", "high");
        message.put("channelId", "default");
        
        if (data != null) {
            message.put("data", data);
        }
        
        JSONArray messages = new JSONArray();
        messages.put(message);
        
        return sendToExpo(messages.toString());
    }
    
    /**
     * Send push notification to multiple devices
     * @param pushTokens List of Expo push tokens
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data (can be null)
     * @return Response from Expo API
     * @throws IOException If network error occurs
     */
    public static String sendPushNotificationToMultiple(List<String> pushTokens, String title, String body, JSONObject data) throws IOException {
        JSONArray messages = new JSONArray();
        
        for (String pushToken : pushTokens) {
            JSONObject message = new JSONObject();
            message.put("to", pushToken);
            message.put("sound", "default");
            message.put("title", title);
            message.put("body", body);
            message.put("priority", "high");
            message.put("channelId", "default");
            
            if (data != null) {
                message.put("data", data);
            }
            
            messages.put(message);
        }
        
        return sendToExpo(messages.toString());
    }
    
    /**
     * Send notification for new product
     * @param pushToken Expo push token
     * @param productName Product name
     * @param productId Product ID
     * @return Response from Expo API
     * @throws IOException If network error occurs
     */
    public static String sendNewProductNotification(String pushToken, String productName, Long productId) throws IOException {
        JSONObject data = new JSONObject();
        data.put("type", "new_product");
        data.put("productId", productId);
        
        return sendPushNotification(
            pushToken,
            "New Product Launched! 🎉",
            productName + " is now available",
            data
        );
    }
    
    /**
     * Send notification for price change
     * @param pushToken Expo push token
     * @param productName Product name
     * @param productId Product ID
     * @param oldPrice Old price
     * @param newPrice New price
     * @param changePercent Change percentage
     * @return Response from Expo API
     * @throws IOException If network error occurs
     */
    public static String sendPriceChangeNotification(String pushToken, String productName, Long productId, 
                                                     Double oldPrice, Double newPrice, Double changePercent) throws IOException {
        JSONObject data = new JSONObject();
        data.put("type", "price_change");
        data.put("productId", productId);
        data.put("oldPrice", oldPrice);
        data.put("newPrice", newPrice);
        data.put("change", newPrice - oldPrice);
        
        String changeDirection = changePercent > 0 ? "increased" : "decreased";
        String emoji = changePercent > 0 ? "📈" : "📉";
        String changeSign = changePercent > 0 ? "+" : "";
        
        String body = String.format("%s: ₹%.2f → ₹%.2f (%s%.2f%%)", 
                                   productName, oldPrice, newPrice, changeSign, changePercent);
        
        return sendPushNotification(
            pushToken,
            "Price " + changeDirection + " " + emoji,
            body,
            data
        );
    }
    
    /**
     * Internal method to send request to Expo Push API
     * @param jsonBody JSON string with messages array
     * @return Response from Expo API
     * @throws IOException If network error occurs
     */
    private static String sendToExpo(String jsonBody) throws IOException {
        URL url = new URL(EXPO_PUSH_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        
        // Read response
        java.io.BufferedReader in = new java.io.BufferedReader(
            new java.io.InputStreamReader(
                responseCode >= 200 && responseCode < 300 
                    ? connection.getInputStream() 
                    : connection.getErrorStream()
            )
        );
        
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        if (responseCode >= 200 && responseCode < 300) {
            return response.toString();
        } else {
            throw new IOException("Expo API returned error: " + responseCode + " - " + response.toString());
        }
    }
}
